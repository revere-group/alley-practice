package dev.revere.alley.feature.tournament.internal;

import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.common.TaskUtil;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.enums.ProfileState;
import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.arena.ArenaService;
import dev.revere.alley.feature.cooldown.Cooldown;
import dev.revere.alley.feature.cooldown.CooldownService;
import dev.revere.alley.feature.cooldown.CooldownType;
import dev.revere.alley.feature.hotbar.HotbarService;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.match.Match;
import dev.revere.alley.feature.match.MatchService;
import dev.revere.alley.feature.match.MatchState;
import dev.revere.alley.feature.match.internal.types.DefaultMatch;
import dev.revere.alley.feature.match.model.GameParticipant;
import dev.revere.alley.feature.match.model.TeamGameParticipant;
import dev.revere.alley.feature.match.model.internal.MatchGamePlayer;
import dev.revere.alley.feature.party.Party;
import dev.revere.alley.feature.party.PartyService;
import dev.revere.alley.feature.spawn.SpawnService;
import dev.revere.alley.feature.tournament.TournamentService;
import dev.revere.alley.feature.tournament.formation.TournamentTeamFormationService;
import dev.revere.alley.feature.tournament.internal.helpers.TournamentMessageBuilder;
import dev.revere.alley.feature.tournament.internal.helpers.TournamentValidator;
import dev.revere.alley.feature.tournament.model.Tournament;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;
import dev.revere.alley.feature.tournament.model.TournamentState;
import dev.revere.alley.feature.tournament.model.TournamentType;
import dev.revere.alley.feature.tournament.task.TournamentBroadcastTask;
import dev.revere.alley.feature.tournament.task.TournamentRoundStartTask;
import dev.revere.alley.feature.tournament.task.TournamentStartTask;
import lombok.Getter;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Remi
 * @project alley-practice
 * @date 6/08/2025
 */
@Getter
@Service(provides = TournamentService.class, priority = 1200)
public class TournamentServiceImpl implements TournamentService {
    private final Map<UUID, Tournament> activeTournaments = new ConcurrentHashMap<>();
    private static final UUID GLOBAL_COOLDOWN_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static final AtomicInteger tournamentCounter = new AtomicInteger(1);

    private static final long BROADCAST_TASK_INTERVAL = 200L;
    private static final long INACTIVITY_TIMEOUT_TICKS = 2 * 60 * 20L;

    @Getter
    private TournamentStartTask tournamentStartTask;
    @Getter
    private TournamentRoundStartTask tournamentRoundStartTask;

    private final TournamentTeamFormationService teamFormationService;
    private final CooldownService cooldownService;
    private final ProfileService profileService;
    private final HotbarService hotbarService;
    private final ArenaService arenaService;
    private final MatchService matchService;
    private final PartyService partyService;
    private final SpawnService spawnService;

    public TournamentServiceImpl(TournamentTeamFormationService teamFormationService, CooldownService cooldownService, ProfileService profileService, HotbarService hotbarService, ArenaService arenaService, MatchService matchService, PartyService partyService, SpawnService spawnService) {
        this.teamFormationService = teamFormationService;
        this.cooldownService = cooldownService;
        this.profileService = profileService;
        this.hotbarService = hotbarService;
        this.arenaService = arenaService;
        this.matchService = matchService;
        this.partyService = partyService;
        this.spawnService = spawnService;
    }

    @Override
    public void hostTournament(Player host, TournamentType type, Kit kit) {
        if (!activeTournaments.isEmpty()) {
            host.sendMessage(CC.translate("&cAnother tournament is already in progress. Please wait for it to finish."));
            return;
        }

        if (isHostOnCooldown(host)) {
            return;
        }

        Tournament tournament = createAndRegisterTournament(host.getName(), kit, type.getDisplayName(), type.getTeamSize(), type.getMaxTeams(), type.getMinTeams());
        setupTournamentTasks(tournament);
        applyHostCooldown();
        joinTournament(host, tournament);
    }

    @Override
    public void adminHostTournament(Player host, Kit kit, int teamSize, int maxTeams) {
        String displayName = teamSize + "v" + teamSize;
        Tournament tournament = createAndRegisterTournament(host.getName(), kit, displayName, teamSize, maxTeams, 2);
        setupTournamentTasks(tournament);
        joinTournament(host, tournament);
    }

    @Override
    public void joinTournament(Player player, Tournament tournament) {
        if (!TournamentValidator.canPlayerJoin(player, tournament, this)) {
            return;
        }

        TournamentParticipant newParticipant = createParticipant(player, tournament);
        if (newParticipant == null) {
            return;
        }

        addParticipantToTournament(tournament, newParticipant);
        checkForAutoStart(tournament);
    }

    @Override
    public void handlePlayerDeparture(Player player) {
        Tournament tournament = getPlayerTournament(player);
        if (tournament == null) return;

        Optional<TournamentParticipant> participantOpt = findParticipantGroupForPlayer(tournament, player.getUniqueId());
        if (!participantOpt.isPresent()) return;

        TournamentParticipant participant = participantOpt.get();
        TournamentState currentState = tournament.getState();

        if (currentState == TournamentState.WAITING || currentState == TournamentState.STARTING) {
            String leaveMessage = TournamentMessageBuilder.generateParticipantBroadcast(participant, "has left the tournament", "have left the tournament");
            tournament.broadcast(leaveMessage);
            removeGroupFromTournament(participant);
            handleEarlyDepartureStateChange(tournament);
        } else {
            handleMidGameDeparture(player, tournament, participant);
        }
    }

    @Override
    public void handleMatchEnd(Match match) {
        Tournament tournament = match.getTournament();
        if (tournament == null || tournament.getState() != TournamentState.IN_PROGRESS) return;
        if (!(match instanceof DefaultMatch)) return;

        DefaultMatch defaultMatch = (DefaultMatch) match;
        GameParticipant<MatchGamePlayer> winner = defaultMatch.getWinner();
        GameParticipant<MatchGamePlayer> loser = defaultMatch.getLoser();

        updateWinnerState(tournament, winner);
        if (loser != null) {
            handleLoserElimination(tournament, winner, loser);
        }

        tournament.removeActiveMatch(match);
        if (tournament.getActiveMatches().isEmpty()) {
            checkForNextRound(tournament);
        }
    }

    @Override
    public void forceStartTournament(Tournament tournament) {
        if (tournament.getState() != TournamentState.WAITING) {
            return;
        }
        if (tournament.getWaitingPool().size() >= tournament.getMinTeams()) {
            startCountdown(tournament);
        } else {
            sendTournamentMessage(tournament, "&cCannot start the tournament, minimum of " + tournament.getMinTeams() + " teams required.");
        }
    }

    @Override
    public void cancelTournament(Tournament tournament, String reason) {
        if (tournament.getState() == TournamentState.ENDED) return;

        handleInactivityCancel(tournament, reason);

        cancelMatches(tournament);

        cancelAllTasks(tournament);
        cleanupParticipants(tournament);

        activeTournaments.remove(tournament.getTournamentId());
        tournament.setState(TournamentState.ENDED);
        sendTournamentMessage(tournament, "&cThe tournament has been cancelled. Reason: " + reason);
    }

    @Override
    public Tournament getTournament(UUID tournamentId) {
        return activeTournaments.get(tournamentId);
    }

    @Override
    public Tournament getPlayerTournament(Player player) {
        if (player == null) return null;
        Profile profile = profileService.getProfile(player.getUniqueId());
        return profile.getTournament();
    }

    @Override
    public List<Tournament> getTournaments() {
        return new ArrayList<>(activeTournaments.values());
    }

    /**
     * Cancels all active matches belonging to a specific tournament.
     * This method simulates the end of each match to trigger proper cleanup,
     * ensuring all players are returned to the lobby safely.
     *
     * @param tournament The tournament whose matches should be canceled.
     */
    private void cancelMatches(Tournament tournament) {
        matchService.getMatches().stream()
                .filter(m -> m.getTournament() == tournament)
                .forEach(match -> {
                    match.handleRoundEnd();
                    match.setState(MatchState.ENDING_MATCH);
                    if (match.getRunnable() != null) {
                        match.getRunnable().setStage(4);
                    }
                });
    }

    /**
     * Handles the logic for a player's departure mid-tournament.
     *
     * @param player      The player leaving.
     * @param tournament  The tournament they are in.
     * @param participant The participant group they belong to.
     */
    private void handleMidGameDeparture(Player player, Tournament tournament, TournamentParticipant participant) {
        sendTournamentMessage(tournament, "&e" + player.getName() + " &fhas left their team.");

        participant.removeMember(player.getUniqueId());
        resetPlayerStateToLobby(player);

        if (participant.isEmpty()) {
            sendTournamentMessage(tournament, "&e" + getTeamDisplayName(participant) + " &fhas disbanded.");
            removeGroupFromTournament(participant);
        }
        matchService.getMatches().stream()
                .filter(m -> m.getTournament() == tournament && m.getParticipant(player) != null)
                .findFirst()
                .ifPresent(match -> match.handleDisconnect(player));
    }

    /**
     * Handles a tournament's state change when a participant leaves during the waiting or starting phase.
     *
     * @param tournament The tournament to check.
     */
    private void handleEarlyDepartureStateChange(Tournament tournament) {
        if (tournament.getState() == TournamentState.STARTING) {
            tournament.getStartingTask().cancel();
            tournament.setStartingTask(null);
            tournament.setState(TournamentState.WAITING);
            sendTournamentMessage(tournament, "&cThe countdown has been cancelled as a player left.");
        }
    }

    /**
     * Updates the state for a tournament winner, moving them back to a safe playing state.
     *
     * @param tournament The tournament.
     * @param winner     The winning game participant.
     */
    private void updateWinnerState(Tournament tournament, GameParticipant<MatchGamePlayer> winner) {
        if (winner != null) {
            Optional<TournamentParticipant> tournamentWinnerOpt = findParticipantGroupForPlayer(tournament, winner.getLeader().getUuid());
            tournamentWinnerOpt.ifPresent(participant -> participant.getOnlinePlayers().forEach(player -> {
                Profile profile = profileService.getProfile(player.getUniqueId());
                profile.setState(ProfileState.PLAYING_TOURNAMENT);
                hotbarService.applyHotbarItems(player);
            }));
        }
    }

    /**
     * Handles the elimination of a losing participant from a tournament.
     *
     * @param tournament The tournament.
     * @param winner     The winning game participant.
     * @param loser      The losing game participant.
     */
    private void handleLoserElimination(Tournament tournament, GameParticipant<MatchGamePlayer> winner, GameParticipant<MatchGamePlayer> loser) {
        Optional<TournamentParticipant> tournamentLoserOpt = findParticipantGroupForPlayer(tournament, loser.getLeader().getUuid());
        Optional<TournamentParticipant> tournamentWinnerOpt = findParticipantGroupForPlayer(tournament, winner.getLeader().getUuid());

        if (tournamentLoserOpt.isPresent() && tournamentWinnerOpt.isPresent()) {
            TournamentParticipant tournamentLoser = tournamentLoserOpt.get();
            TournamentParticipant tournamentWinner = tournamentWinnerOpt.get();

            String eliminationMessage = CC.translate("&c" + getTeamDisplayName(tournamentLoser) + " &fwas eliminated by &a" + getTeamDisplayName(tournamentWinner) + "&f.");
            sendTournamentMessage(tournament, eliminationMessage);
            removeGroupFromTournament(tournamentLoser);
        }
    }

    /**
     * Creates a new `TournamentParticipant` object for a player or their party.
     *
     * @param player     The player joining.
     * @param tournament The tournament they are joining.
     * @return The new `TournamentParticipant`, or {@code null} if creation fails due to party size or space.
     */
    private TournamentParticipant createParticipant(Player player, Tournament tournament) {
        int currentPlayers = tournament.getWaitingPool().stream().mapToInt(TournamentParticipant::getSize).sum();
        int maxPlayers = tournament.getMaxTeams() * tournament.getTeamSize();

        Party party = partyService.getParty(player);
        if (tournament.getTeamSize() > 1 && party != null) {
            return createPartyParticipant(player, party, tournament, currentPlayers, maxPlayers);
        } else {
            return createSoloParticipant(player, currentPlayers, maxPlayers);
        }
    }

    /**
     * Creates a new participant from a party, checking for size constraints.
     *
     * @param player         The player, who must be the party leader.
     * @param party          The player's party.
     * @param tournament     The tournament.
     * @param currentPlayers The current number of players in the tournament.
     * @param maxPlayers     The maximum number of players allowed.
     * @return A new `TournamentParticipant` for the party, or {@code null} if invalid.
     */
    private TournamentParticipant createPartyParticipant(Player player, Party party, Tournament tournament, int currentPlayers, int maxPlayers) {
        if (!party.isLeader(player)) {
            player.sendMessage(CC.translate("&cOnly your party leader can join the tournament."));
            return null;
        }
        if (party.getMembers().size() > tournament.getTeamSize()) {
            player.sendMessage(CC.translate("&cYour party is too large for this tournament (" + party.getMembers().size() + "/" + tournament.getTeamSize() + ")."));
            return null;
        }
        if (currentPlayers + party.getMembers().size() > maxPlayers) {
            player.sendMessage(CC.translate("&cThere is not enough space for your party."));
            return null;
        }

        List<Player> partyMembers = party.getMembers().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).collect(Collectors.toList());
        return new TournamentParticipant(player, partyMembers);
    }

    /**
     * Creates a new participant for a solo player, checking for space constraints.
     *
     * @param player         The solo player.
     * @param currentPlayers The current number of players in the tournament.
     * @param maxPlayers     The maximum number of players allowed.
     * @return A new `TournamentParticipant` for the solo player, or {@code null} if invalid.
     */
    private TournamentParticipant createSoloParticipant(Player player, int currentPlayers, int maxPlayers) {
        if (currentPlayers + 1 > maxPlayers) {
            player.sendMessage(CC.translate("&cThe tournament is full."));
            return null;
        }
        return new TournamentParticipant(player);
    }

    /**
     * Adds a `TournamentParticipant` to the tournament's waiting pool and updates their state.
     * It also broadcasts a join message and checks if the tournament is ready to auto-start.
     *
     * @param tournament  The tournament to add the participant to.
     * @param participant The participant to be added.
     */
    private void addParticipantToTournament(Tournament tournament, TournamentParticipant participant) {
        tournament.addToWaitingPool(participant);
        updatePlayerStates(participant, tournament);
        broadcastJoinMessage(participant, tournament);
    }

    /**
     * Checks if the tournament has reached its maximum player capacity,
     * and if so, starts the countdown to begin the tournament.
     *
     * @param tournament The tournament to check.
     */
    private void checkForAutoStart(Tournament tournament) {
        int totalPlayers = tournament.getWaitingPool().stream().mapToInt(TournamentParticipant::getSize).sum();
        int maxPlayers = tournament.getMaxTeams() * tournament.getTeamSize();

        if (totalPlayers >= maxPlayers) {
            startCountdown(tournament);
        }
    }

    /**
     * Updates the `ProfileState` and `Tournament` reference for a participant group.
     *
     * @param participant The participant group.
     * @param tournament  The tournament they are joining.
     */
    private void updatePlayerStates(TournamentParticipant participant, Tournament tournament) {
        participant.getOnlinePlayers().forEach(member -> setPlayerTournamentState(member, tournament));
    }

    /**
     * Broadcasts a message to the tournament with the new player/party count.
     *
     * @param newParticipant The participant group that just joined.
     * @param tournament     The tournament.
     */
    private void broadcastJoinMessage(TournamentParticipant newParticipant, Tournament tournament) {
        int newTotalPlayers = tournament.getWaitingPool().stream().mapToInt(TournamentParticipant::getSize).sum();
        int maxPlayers = tournament.getMaxTeams() * tournament.getTeamSize();
        String joinMessage = TournamentMessageBuilder.generateParticipantBroadcast(newParticipant, "has joined", "have joined");

        sendTournamentMessage(tournament, joinMessage + " (" + newTotalPlayers + "/" + maxPlayers + " Players)");
    }

    /**
     * Checks if the host is currently on cooldown from hosting a tournament.
     *
     * @param host The player to check.
     * @return True if the host is on cooldown, false otherwise.
     */
    private boolean isHostOnCooldown(Player host) {
        Cooldown cooldown = cooldownService.getCooldown(GLOBAL_COOLDOWN_UUID, CooldownType.TOURNAMENT_HOST);
        if (cooldown != null && cooldown.isActive()) {
            host.sendMessage(CC.translate("&cYou must wait " + cooldown.remainingTimeInMinutes() + " minutes to host another tournament."));
            return true;
        }
        return false;
    }

    /**
     * Creates and registers a new tournament object.
     *
     * @param hostName    The name of the host.
     * @param kit         The kit for the tournament.
     * @param displayName The display name of the tournament.
     * @param teamSize    The number of players per team.
     * @param maxTeams    The maximum number of teams.
     * @param minTeams    The minimum number of teams.
     * @return The newly created Tournament object.
     */
    private Tournament createAndRegisterTournament(String hostName, Kit kit, String displayName, int teamSize, int maxTeams, int minTeams) {
        int id = tournamentCounter.getAndIncrement();
        Tournament tournament = new Tournament(id, hostName, kit, displayName, teamSize, maxTeams, minTeams);
        activeTournaments.put(tournament.getTournamentId(), tournament);
        return tournament;
    }

    /**
     * Sets up and starts all necessary scheduled tasks for a new tournament.
     *
     * @param tournament The tournament to set up tasks for.
     */
    private void setupTournamentTasks(Tournament tournament) {
        TournamentBroadcastTask broadcastTask = new TournamentBroadcastTask(tournament);
        broadcastTask.run();
        tournament.setBroadcastTask(TaskUtil.runTimer(broadcastTask, BROADCAST_TASK_INTERVAL, BROADCAST_TASK_INTERVAL));

        BukkitTask inactivityTask = TaskUtil.runLater(() -> {
            if (tournament.getState() == TournamentState.WAITING && activeTournaments.containsKey(tournament.getTournamentId())) {
                cancelTournament(tournament, "Not enough players joined in time.");
            }
        }, INACTIVITY_TIMEOUT_TICKS);
        tournament.setInactivityTask(inactivityTask);
    }

    /**
     * Applies a new hosting cooldown for the tournament host.
     */
    private void applyHostCooldown() {
        Cooldown newCooldown = new Cooldown(CooldownType.TOURNAMENT_HOST, () -> sendTournamentMessage(null, CC.translate("&6[Tournament] &aA tournament can now be hosted!")));
        newCooldown.resetCooldown();
        cooldownService.addCooldown(GLOBAL_COOLDOWN_UUID, CooldownType.TOURNAMENT_HOST, newCooldown);
    }

    /**
     * Cancels all scheduled Bukkit tasks associated with a tournament.
     *
     * @param tournament The tournament whose tasks to cancel.
     */
    private void cancelAllTasks(Tournament tournament) {
        if (tournament.getBroadcastTask() != null) tournament.getBroadcastTask().cancel();
        if (tournament.getStartingTask() != null) tournament.getStartingTask().cancel();
        if (tournament.getInactivityTask() != null) tournament.getInactivityTask().cancel();
        if (tournament.getRoundStartTask() != null) tournament.getRoundStartTask().cancel();
    }

    /**
     * Removes all participants from the tournament's various lists and resets their player state.
     *
     * @param tournament The tournament to clean up.
     */
    private void cleanupParticipants(Tournament tournament) {
        new ArrayList<>(tournament.getWaitingPool()).forEach(this::removeGroupFromTournament);
        new ArrayList<>(tournament.getParticipants()).forEach(this::removeGroupFromTournament);
        new ArrayList<>(tournament.getRoundParticipants()).forEach(this::removeGroupFromTournament);
    }

    /**
     * Handles the special case where a tournament is cancelled due to inactivity,
     * which clears the host's cooldown.
     *
     * @param tournament The tournament.
     * @param reason     The cancellation reason.
     */
    private void handleInactivityCancel(Tournament tournament, String reason) {
        boolean wasInactivityCancel = "Not enough players joined in time.".equals(reason);
        if (wasInactivityCancel) {
            cooldownService.removeCooldown(GLOBAL_COOLDOWN_UUID, CooldownType.TOURNAMENT_HOST);
            Player host = Bukkit.getPlayer(tournament.getHostName());
            if (host != null) {
                host.sendMessage(CC.translate("&a[Tournament] Your hosting cooldown has been cleared because the tournament timed out."));
            }
        }
    }

    /**
     * Starts the tournament countdown.
     *
     * @param tournament The tournament to start the countdown for.
     */
    private void startCountdown(Tournament tournament) {
        if (tournament.getState() != TournamentState.WAITING) return;

        if (tournament.getBroadcastTask() != null) {
            tournament.getBroadcastTask().cancel();
        }

        tournament.setState(TournamentState.STARTING);
        this.tournamentStartTask = new TournamentStartTask(tournament, this);
        tournament.setStartingTask(TaskUtil.runTimer(this.tournamentStartTask, 0L, 20L));
    }

    /**
     * Starts the actual tournament, forming teams and beginning the first round.
     *
     * @param tournament The tournament to start.
     */
    public void startTournament(Tournament tournament) {
        tournament.setState(TournamentState.IN_PROGRESS);
        tournament.broadcast(CC.translate("&6[Tournament] &aThe tournament is starting! Forming teams..."));

        formTeams(tournament);

        if (tournament.getParticipants().size() < 2) {
            cancelTournament(tournament, "Not enough players to start.");
            return;
        }
        startRound(tournament);
    }

    /**
     * Forms the final teams for the tournament by merging solo players into parties
     * if space allows and sorting them.
     *
     * @param tournament The tournament for which to form teams.
     */
    private void formTeams(Tournament tournament) {
        tournament.getParticipants().clear();
        tournament.getRoundParticipants().clear();

        List<TournamentParticipant> formedTeams = teamFormationService.formTeamsForTournament(tournament);

        formedTeams.forEach(tournament::addFinalizedParticipant);
    }

    /**
     * Starts a new round of the tournament by creating matches for participants.
     *
     * @param tournament The tournament to start the round for.
     */
    private void startRound(Tournament tournament) {
        tournament.setCurrentRound(tournament.getCurrentRound() + 1);
        tournament.broadcast(CC.translate("&6[Tournament] &fStarting Round " + tournament.getCurrentRound() + "!"));

        List<TournamentParticipant> participants = new ArrayList<>(tournament.getRoundParticipants());
        Collections.shuffle(participants);

        if (participants.size() % 2 != 0) {
            TournamentParticipant byeParticipant = participants.remove(participants.size() - 1);
            byeParticipant.getOnlinePlayers().forEach(p -> p.sendMessage(CC.translate("&6[Tournament] &aYou have received a bye and advance to the next round!")));
        }

        for (int i = 0; i < participants.size(); i += 2) {
            createTournamentMatch(tournament, participants.get(i), participants.get(i + 1));
        }
    }

    /**
     * Creates a new match for two tournament participant groups.
     *
     * @param tournament The tournament the match belongs to.
     * @param pA         The first participant group.
     * @param pB         The second participant group.
     */
    private void createTournamentMatch(Tournament tournament, TournamentParticipant pA, TournamentParticipant pB) {
        Arena arena = arenaService.getRandomArena(tournament.getKit());
        if (arena == null) {
            cancelTournament(tournament, "No available arenas found.");
            return;
        }

        GameParticipant<MatchGamePlayer> matchParticipantA = createMatchParticipant(pA, tournament.getKit());
        GameParticipant<MatchGamePlayer> matchParticipantB = createMatchParticipant(pB, tournament.getKit());

        matchService.createTournamentMatch(tournament, tournament.getKit(), arena, matchParticipantA, matchParticipantB);
    }

    /**
     * Creates a match participant object from a tournament participant group.
     * This includes creating `MatchGamePlayer` objects for all members.
     *
     * @param tournyGroup The tournament participant group.
     * @param kit         The kit for the match.
     * @return A `TeamGameParticipant` representing the group for a match.
     */
    private GameParticipant<MatchGamePlayer> createMatchParticipant(TournamentParticipant tournyGroup, Kit kit) {
        Player leader = Bukkit.getPlayer(tournyGroup.getLeaderUuid());
        Profile leaderProfile = profileService.getProfile(leader.getUniqueId());
        int elo = leaderProfile.getProfileData().getRankedKitData().get(kit.getName()).getElo();

        MatchGamePlayer leaderGamePlayer = new MatchGamePlayer(leader.getUniqueId(), leader.getName(), elo);

        TeamGameParticipant<MatchGamePlayer> teamParticipant = new TeamGameParticipant<>(leaderGamePlayer);

        tournyGroup.getMemberUuids().stream()
                .filter(uuid -> !uuid.equals(leader.getUniqueId()))
                .map(Bukkit::getPlayer).filter(Objects::nonNull)
                .forEach(member -> {
                    Profile memberProfile = profileService.getProfile(member.getUniqueId());
                    int memberElo = memberProfile.getProfileData().getRankedKitData().get(kit.getName()).getElo();
                    teamParticipant.addPlayer(new MatchGamePlayer(member.getUniqueId(), member.getName(), memberElo));
                });

        return teamParticipant;
    }

    /**
     * Checks if the tournament can proceed to the next round or if it's over.
     *
     * @param tournament The tournament to check.
     */
    public void checkForNextRound(Tournament tournament) {
        if (tournament.getRoundParticipants().size() == 1) {
            endTournament(tournament, tournament.getRoundParticipants().get(0));
        } else if (tournament.getRoundParticipants().size() > 1) {
            this.tournamentRoundStartTask = new TournamentRoundStartTask(tournament, this);
            tournament.setRoundStartTask(TaskUtil.runTimer(this.tournamentRoundStartTask, 0L, 20L));
        } else {
            cancelTournament(tournament, "No participants remaining.");
        }
    }

    /**
     * Ends the tournament, declares a winner, and cleans up.
     *
     * @param tournament The tournament that has ended.
     * @param winner     The winning `TournamentParticipant`.
     */
    private void endTournament(Tournament tournament, TournamentParticipant winner) {
        tournament.setState(TournamentState.ENDED);

        String winnerName = winner.getLeaderName();
        String teamText = winner.getSize() > 1 ? "'s Team" : "";

        TextComponent title = new TextComponent(CC.translate("&6&lTournament Winner"));
        TextComponent winnerComponent = new TextComponent(CC.translate("&6&l│ &fWinner: &a" + winnerName + teamText));
        TextComponent kitComponent = new TextComponent(CC.translate("&6&l│ &fKit: &a" + tournament.getKit().getDisplayName()));
        TextComponent roundComponent = new TextComponent(CC.translate("&6&l│ &fRounds: &a" + tournament.getCurrentRound()));

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage("");
            player.spigot().sendMessage(title);
            player.spigot().sendMessage(winnerComponent);
            player.spigot().sendMessage(kitComponent);
            player.spigot().sendMessage(roundComponent);
            player.sendMessage("");
        }

        winner.getOnlinePlayers().forEach(this::resetPlayerStateToLobby);
        activeTournaments.remove(tournament.getTournamentId());
    }

    /**
     * Removes a participant group from a tournament and resets the state of all its members.
     *
     * @param participant The participant group to remove.
     */
    private void removeGroupFromTournament(TournamentParticipant participant) {
        Tournament tournament = getTournamentByParticipant(participant);
        if (tournament == null) return;

        tournament.removeParticipant(participant);
        participant.getMemberUuids().forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) resetPlayerStateToLobby(p);
        });
    }

    /**
     * Finds the `TournamentParticipant` group for a given player's UUID.
     *
     * @param tournament The tournament to search within.
     * @param playerUuid The UUID of the player.
     * @return An `Optional` containing the participant group if found, otherwise empty.
     */
    private Optional<TournamentParticipant> findParticipantGroupForPlayer(Tournament tournament, UUID playerUuid) {
        return tournament.getWaitingPool().stream()
                .filter(p -> p.containsPlayer(playerUuid))
                .findFirst()
                .map(Optional::of)
                .orElseGet(() -> tournament.getParticipants().stream()
                        .filter(p -> p.containsPlayer(playerUuid))
                        .findFirst());
    }

    /**
     * Generates a display name for a participant group
     *
     * @param participant The participant to generate a display name for
     * @return The generated team display name
     */
    private String getTeamDisplayName(TournamentParticipant participant) {
        if (participant == null) return "An unknown team";
        if (participant.getSize() > 1) {
            return participant.getLeaderName() + "'s team";
        } else {
            return participant.getLeaderName();
        }
    }

    /**
     * Retrieves the tournament a participant group belongs to.
     *
     * @param participant The participant group.
     * @return The tournament, or {@code null} if not found.
     */
    private Tournament getTournamentByParticipant(TournamentParticipant participant) {
        return activeTournaments.values().stream()
                .filter(t -> t.getWaitingPool().contains(participant) || t.getParticipants().contains(participant))
                .findFirst().orElse(null);
    }

    /**
     * Sets a player's profile state to `PLAYING_TOURNAMENT` and applies their tournament hotbar.
     *
     * @param player     The player to update.
     * @param tournament The tournament they are joining.
     */
    private void setPlayerTournamentState(Player player, Tournament tournament) {
        Profile profile = profileService.getProfile(player.getUniqueId());
        profile.setState(ProfileState.PLAYING_TOURNAMENT);
        profile.setTournament(tournament);
        hotbarService.applyHotbarItems(player);
    }

    /**
     * Resets a player's profile state back to `LOBBY` and teleports them to spawn.
     *
     * @param player The player to reset.
     */
    private void resetPlayerStateToLobby(Player player) {
        if (player == null || !player.isOnline()) return;
        Profile profile = profileService.getProfile(player.getUniqueId());

        profile.setState(ProfileState.LOBBY);
        profile.setTournament(null);
        hotbarService.applyHotbarItems(player);
    }

    /**
     * Sends a consistent, formatted message to all tournament participants.
     * If the tournament parameter is null, it broadcasts to the entire server.
     *
     * @param tournament The tournament to send the message to, or null to broadcast globally.
     * @param message    The message content.
     */
    private void sendTournamentMessage(Tournament tournament, String message) {
        String prefix = CC.translate("&6[Tournament] &f");
        String formattedMessage = prefix + CC.translate(message);

        if (tournament != null) {
            tournament.broadcast(formattedMessage);
        } else {
            Bukkit.broadcastMessage(formattedMessage);
        }
    }
}
