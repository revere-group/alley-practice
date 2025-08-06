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
import dev.revere.alley.feature.match.internal.types.DefaultMatch;
import dev.revere.alley.feature.match.model.GameParticipant;
import dev.revere.alley.feature.match.model.TeamGameParticipant;
import dev.revere.alley.feature.match.model.internal.MatchGamePlayer;
import dev.revere.alley.feature.party.Party;
import dev.revere.alley.feature.party.PartyService;
import dev.revere.alley.feature.spawn.SpawnService;
import dev.revere.alley.feature.tournament.TournamentService;
import dev.revere.alley.feature.tournament.model.Tournament;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;
import dev.revere.alley.feature.tournament.model.TournamentState;
import dev.revere.alley.feature.tournament.model.TournamentType;
import dev.revere.alley.feature.tournament.task.TournamentBroadcastTask;
import dev.revere.alley.feature.tournament.task.TournamentRoundStartTask;
import dev.revere.alley.feature.tournament.task.TournamentStartTask;
import lombok.Getter;
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

    @Getter
    private TournamentStartTask tournamentStartTask;
    @Getter
    private TournamentRoundStartTask tournamentRoundStartTask;

    private final ProfileService profileService;
    private final ArenaService arenaService;
    private final MatchService matchService;
    private final PartyService partyService;
    private final CooldownService cooldownService;
    private final HotbarService hotbarService;
    private final SpawnService spawnService;

    public TournamentServiceImpl(ProfileService profileService, ArenaService arenaService, MatchService matchService, PartyService partyService, CooldownService cooldownService, HotbarService hotbarService, SpawnService spawnService) {
        this.profileService = profileService;
        this.arenaService = arenaService;
        this.matchService = matchService;
        this.partyService = partyService;
        this.cooldownService = cooldownService;
        this.hotbarService = hotbarService;
        this.spawnService = spawnService;
    }

    @Override
    public void hostTournament(Player host, TournamentType type, Kit kit) {
        if (!activeTournaments.isEmpty()) {
            host.sendMessage(CC.translate("&cAnother tournament is already in progress. Please wait for it to finish."));
            return;
        }

        Cooldown cooldown = cooldownService.getCooldown(GLOBAL_COOLDOWN_UUID, CooldownType.TOURNAMENT_HOST);
        if (cooldown != null && cooldown.isActive()) {
            host.sendMessage(CC.translate("&cYou must wait " + cooldown.remainingTimeInMinutes() + " to host another tournament."));
            return;
        }

        int id = tournamentCounter.getAndIncrement();
        Tournament tournament = new Tournament(id, host.getName(), kit, type.getDisplayName(), type.getTeamSize(), type.getMaxTeams(), type.getMinTeams());
        activeTournaments.put(tournament.getTournamentId(), tournament);

        BukkitTask broadcastTask = TaskUtil.runTimer(new TournamentBroadcastTask(tournament), 200L, 200L);
        tournament.setBroadcastTask(broadcastTask);

        BukkitTask inactivityTask = TaskUtil.runLater(() -> {
            if (tournament.getState() == TournamentState.WAITING && activeTournaments.containsKey(tournament.getTournamentId())) {
                cancelTournament(tournament, "Not enough players joined in time.");
            }
        }, 2 * 60 * 20L);
        tournament.setInactivityTask(inactivityTask);

        Cooldown newCooldown = new Cooldown(CooldownType.TOURNAMENT_HOST, () -> Bukkit.broadcastMessage(CC.translate("&6[Tournament] &aA tournament can now be hosted!")));
        newCooldown.resetCooldown();
        cooldownService.addCooldown(GLOBAL_COOLDOWN_UUID, CooldownType.TOURNAMENT_HOST, newCooldown);

        joinTournament(host, tournament);
    }

    @Override
    public void adminHostTournament(Player host, Kit kit, int teamSize, int maxTeams) {
        String displayName = teamSize + "v" + teamSize;

        int id = tournamentCounter.getAndIncrement();
        Tournament tournament = new Tournament(id, host.getName(), kit, displayName, teamSize, maxTeams, 2);
        activeTournaments.put(tournament.getTournamentId(), tournament);

        BukkitTask broadcastTask = TaskUtil.runTimer(new TournamentBroadcastTask(tournament), 200L, 200L);
        tournament.setBroadcastTask(broadcastTask);

        BukkitTask inactivityTask = TaskUtil.runLater(() -> {
            if (tournament.getState() == TournamentState.WAITING && activeTournaments.containsKey(tournament.getTournamentId())) {
                cancelTournament(tournament, "Not enough players joined in time.");
            }
        }, 2 * 60 * 20L);
        tournament.setInactivityTask(inactivityTask);

        joinTournament(host, tournament);
    }

    @Override
    public void joinTournament(Player player, Tournament tournament) {
        if (tournament.getState() != TournamentState.WAITING) {
            player.sendMessage(CC.translate("&cThis tournament is no longer accepting players."));
            return;
        }
        if (getPlayerTournament(player) != null) {
            player.sendMessage(CC.translate("&cYou are already in a tournament."));
            return;
        }

        int currentPlayers = tournament.getWaitingPool().stream().mapToInt(TournamentParticipant::getSize).sum();
        int maxPlayers = tournament.getMaxTeams() * tournament.getTeamSize();

        Party party = partyService.getParty(player);
        TournamentParticipant newParticipant;

        if (tournament.getTeamSize() > 1 && party != null) {
            if (!party.isLeader(player)) {
                player.sendMessage(CC.translate("&cOnly your party leader can join the tournament."));
                return;
            }
            if (party.getMembers().size() > tournament.getTeamSize()) {
                player.sendMessage(CC.translate("&cYour party is too large for this tournament (" + party.getMembers().size() + "/" + tournament.getTeamSize() + ")."));
                return;
            }
            if (currentPlayers + party.getMembers().size() > maxPlayers) {
                player.sendMessage(CC.translate("&cThere is not enough space for your party."));
                return;
            }
            List<Player> partyMembers = party.getMembers().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).collect(Collectors.toList());
            newParticipant = new TournamentParticipant(player, partyMembers);
            partyMembers.forEach(member -> setPlayerTournamentState(member, tournament));
        } else {
            if (currentPlayers + 1 > maxPlayers) {
                player.sendMessage(CC.translate("&cThe tournament is full."));
                return;
            }
            newParticipant = new TournamentParticipant(player);
            setPlayerTournamentState(player, tournament);
        }

        tournament.addToWaitingPool(newParticipant);

        int newTotalPlayers = tournament.getWaitingPool().stream().mapToInt(TournamentParticipant::getSize).sum();
        String joinMessage = generateParticipantBroadcast(newParticipant, "has joined", "have joined");

        tournament.broadcast(joinMessage + " &7(" + newTotalPlayers + "/" + maxPlayers + " Players)");

        if (newTotalPlayers >= maxPlayers) {
            startCountdown(tournament);
        }
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
            String leaveMessage = generateParticipantBroadcast(participant, "has left the tournament", "have left the tournament");
            tournament.broadcast(leaveMessage);
            removeGroupFromTournament(participant);
        } else {
            tournament.broadcast(CC.translate("&6[Tournament] &e" + player.getName() + " &fhas left their team."));

            participant.removeMember(player.getUniqueId());
            resetPlayerStateToLobby(player);

            if (participant.isEmpty()) {
                tournament.broadcast(CC.translate("&6[Tournament] &e" + participant.getLeaderName() + "'s team has disbanded."));
                removeGroupFromTournament(participant);
            }
            matchService.getMatches().stream()
                    .filter(m -> m.getTournament() == tournament && m.getParticipant(player) != null)
                    .findFirst()
                    .ifPresent(match -> match.handleDisconnect(player));
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

        if (winner != null) {
            Optional<TournamentParticipant> tournamentWinnerOpt = findParticipantGroupForPlayer(tournament, winner.getLeader().getUuid());
            tournamentWinnerOpt.ifPresent(participant -> participant.getOnlinePlayers().forEach(player -> {
                Profile profile = profileService.getProfile(player.getUniqueId());
                profile.setState(ProfileState.PLAYING_TOURNAMENT);
                hotbarService.applyHotbarItems(player);
            }));
        }

        if (loser != null && winner != null) {
            Optional<TournamentParticipant> tournamentLoserOpt = findParticipantGroupForPlayer(tournament, loser.getLeader().getUuid());
            Optional<TournamentParticipant> tournamentWinnerOpt = findParticipantGroupForPlayer(tournament, winner.getLeader().getUuid());

            if (tournamentLoserOpt.isPresent() && tournamentWinnerOpt.isPresent()) {
                TournamentParticipant tournamentLoser = tournamentLoserOpt.get();
                TournamentParticipant tournamentWinner = tournamentWinnerOpt.get();

                String eliminationMessage = CC.translate("&6[Tournament] &c" + getTeamDisplayName(tournamentLoser) + " &fwas eliminated by &a" + getTeamDisplayName(tournamentWinner) + "&f.");
                tournament.broadcast(eliminationMessage);
                removeGroupFromTournament(tournamentLoser);
            }
        }

        tournament.removeActiveMatch(match);

        if (tournament.getActiveMatches().isEmpty()) {
            if (tournament.getRoundParticipants().size() <= 1) {
                checkForNextRound(tournament);
            } else {
                this.tournamentRoundStartTask = new TournamentRoundStartTask(tournament, this);
                tournament.setRoundStartTask(TaskUtil.runTimer(this.tournamentRoundStartTask, 0L, 20L));
            }
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
            tournament.broadcast(CC.translate("&cCannot start the tournament, minimum of " + tournament.getMinTeams() + " teams required."));
        }
    }

    @Override
    public void cancelTournament(Tournament tournament, String reason) {
        if (tournament.getState() == TournamentState.ENDED) return;

        boolean wasInactivityCancel = "Not enough players joined in time.".equals(reason);
        if (wasInactivityCancel) {
            cooldownService.removeCooldown(GLOBAL_COOLDOWN_UUID, CooldownType.TOURNAMENT_HOST);
            Player host = Bukkit.getPlayer(tournament.getHostName());
            if (host != null) {
                host.sendMessage(CC.translate("&a[Tournament] Your hosting cooldown has been cleared because the tournament timed out."));
            }
        }

        if (tournament.getBroadcastTask() != null) tournament.getBroadcastTask().cancel();
        if (tournament.getStartingTask() != null) tournament.getStartingTask().cancel();
        if (tournament.getInactivityTask() != null) tournament.getInactivityTask().cancel();
        if (tournament.getRoundStartTask() != null) tournament.getRoundStartTask().cancel();

        new ArrayList<>(tournament.getWaitingPool()).forEach(this::removeGroupFromTournament);
        new ArrayList<>(tournament.getParticipants()).forEach(this::removeGroupFromTournament);

        activeTournaments.remove(tournament.getTournamentId());
        tournament.setState(TournamentState.ENDED);
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
     * Generates a natural language message for a participant group joining or leaving.
     *
     * @param participant  The group in question.
     * @param verbSingular The verb for a single player (e.g., "has joined").
     * @param verbPlural   The verb for multiple players (e.g., "have joined").
     * @return A formatted string for broadcasting.
     */
    private String generateParticipantBroadcast(TournamentParticipant participant, String verbSingular, String verbPlural) {
        List<String> memberNames = participant.getMemberUuids().stream()
                .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        int size = memberNames.size();
        if (size == 0) return "";

        String nameList;
        String verb;

        if (size == 1) {
            nameList = memberNames.get(0);
            verb = verbSingular;
        } else {
            verb = verbPlural;
            if (size == 2) {
                nameList = memberNames.get(0) + " and " + memberNames.get(1);
            } else {
                String almostAll = String.join(", ", memberNames.subList(0, size - 1));
                nameList = almostAll + " and " + memberNames.get(size - 1);
            }
        }

        return CC.translate("&6[Tournament] &e" + nameList + " &f" + verb + "!");
    }

    private void startCountdown(Tournament tournament) {
        if (tournament.getState() != TournamentState.WAITING) return;

        if (tournament.getBroadcastTask() != null) {
            tournament.getBroadcastTask().cancel();
        }

        tournament.setState(TournamentState.STARTING);

        this.tournamentStartTask = new TournamentStartTask(tournament, this);
        tournament.setStartingTask(TaskUtil.runTimer(this.tournamentStartTask, 0L, 20L));
    }

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

    private void formTeams(Tournament tournament) {
        int teamSize = tournament.getTeamSize();
        List<TournamentParticipant> processingPool = new ArrayList<>(tournament.getWaitingPool());

        tournament.getParticipants().clear();
        tournament.getRoundParticipants().clear();

        processingPool.sort(Comparator.comparingInt(TournamentParticipant::getSize).reversed());

        while (!processingPool.isEmpty()) {
            TournamentParticipant currentTeam = processingPool.remove(0);
            while (currentTeam.getSize() < teamSize && !processingPool.isEmpty()) {
                int spaceLeft = teamSize - currentTeam.getSize();

                Optional<TournamentParticipant> bestFitOpt = processingPool.stream()
                        .filter(candidate -> candidate.getSize() <= spaceLeft)
                        .max(Comparator.comparingInt(TournamentParticipant::getSize));

                if (bestFitOpt.isPresent()) {
                    TournamentParticipant bestFit = bestFitOpt.get();
                    currentTeam.merge(bestFit);
                    processingPool.remove(bestFit);
                } else {
                    break;
                }
            }
            tournament.addFinalizedParticipant(currentTeam);
        }
    }

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

    public void checkForNextRound(Tournament tournament) {
        if (tournament.getRoundParticipants().size() == 1) {
            endTournament(tournament, tournament.getRoundParticipants().get(0));
        } else if (tournament.getRoundParticipants().size() > 1) {
            startRound(tournament);
        } else {
            cancelTournament(tournament, "No participants remaining.");
        }
    }

    private void endTournament(Tournament tournament, TournamentParticipant winner) {
        tournament.setState(TournamentState.ENDED);
        Bukkit.broadcastMessage(CC.translate("&6[Tournament] The tournament has ended! &e" + winner.getLeaderName() + (winner.getSize() > 1 ? "'s team is" : " is") + " the winner!"));

        winner.getOnlinePlayers().forEach(p -> {
            p.sendMessage(CC.translate("&a&lCongratulations on winning the tournament!"));
            resetPlayerStateToLobby(p);
        });

        activeTournaments.remove(tournament.getTournamentId());
    }

    private void removeGroupFromTournament(TournamentParticipant participant) {
        Tournament tournament = getTournamentByParticipant(participant);
        if (tournament == null) return;

        tournament.removeParticipant(participant);
        participant.getMemberUuids().forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) resetPlayerStateToLobby(p);
        });

        if (tournament.getState() == TournamentState.STARTING && tournament.getStartingTask() != null) {
            tournament.getStartingTask().cancel();
            tournament.setStartingTask(null);
            tournament.setState(TournamentState.WAITING);
            tournament.broadcast(CC.translate("&6[Tournament] &cThe countdown has been cancelled as a group changed."));
        }
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

    private Optional<TournamentParticipant> findParticipantGroupForPlayer(Tournament tournament, UUID playerUuid) {
        return tournament.getWaitingPool().stream()
                .filter(p -> p.containsPlayer(playerUuid))
                .findFirst()
                .map(Optional::of)
                .orElseGet(() -> tournament.getParticipants().stream()
                        .filter(p -> p.containsPlayer(playerUuid))
                        .findFirst());
    }

    private Tournament getTournamentByParticipant(TournamentParticipant participant) {
        return activeTournaments.values().stream()
                .filter(t -> t.getWaitingPool().contains(participant) || t.getParticipants().contains(participant))
                .findFirst().orElse(null);
    }

    private void setPlayerTournamentState(Player player, Tournament tournament) {
        Profile profile = profileService.getProfile(player.getUniqueId());
        profile.setState(ProfileState.PLAYING_TOURNAMENT);
        profile.setTournament(tournament);
        hotbarService.applyHotbarItems(player);
    }

    private void resetPlayerStateToLobby(Player player) {
        if (player == null || !player.isOnline()) return;
        Profile profile = profileService.getProfile(player.getUniqueId());

        profile.setState(ProfileState.LOBBY);
        profile.setTournament(null);
        hotbarService.applyHotbarItems(player);
    }
}
