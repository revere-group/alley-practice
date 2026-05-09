package dev.revere.alley.feature.tournament.internal;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.feature.cooldown.Cooldown;
import dev.revere.alley.feature.cooldown.CooldownService;
import dev.revere.alley.feature.cooldown.CooldownType;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.match.Match;
import dev.revere.alley.feature.tournament.TournamentService;
import dev.revere.alley.feature.tournament.broadcast.BroadcastEvent;
import dev.revere.alley.feature.tournament.broadcast.TournamentBroadcaster;
import dev.revere.alley.feature.tournament.engine.TournamentConfiguration;
import dev.revere.alley.feature.tournament.engine.TournamentEngine;
import dev.revere.alley.feature.tournament.engine.TournamentEvent;
import dev.revere.alley.feature.tournament.model.Tournament;
import dev.revere.alley.feature.tournament.model.TournamentState;
import dev.revere.alley.feature.tournament.model.TournamentType;
import dev.revere.alley.feature.tournament.participant.ParticipantRegistry;
import dev.revere.alley.feature.tournament.task.TournamentBroadcastTask;
import dev.revere.alley.feature.tournament.task.TournamentCountdownService;
import dev.revere.alley.feature.tournament.validation.TournamentValidationService;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Remi
 * @project alley-practice
 * @date 6/08/2025
 */
@Getter
@Service(provides = TournamentService.class, priority = 1000)
public class TournamentServiceImpl implements TournamentService {
    private static final UUID GLOBAL_TOURNAMENT_COOLDOWN_KEY = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private final List<Tournament> activeTournaments = new CopyOnWriteArrayList<>();
    private final AtomicInteger numericIdGenerator = new AtomicInteger(1);

    private final TournamentEngine engine;
    private final TournamentBroadcaster broadcaster;
    private final ParticipantRegistry participantRegistry;
    private final TournamentValidationService validation;
    private final ProfileService profileService;
    private final CooldownService cooldownService;

    public TournamentServiceImpl(
            TournamentEngine engine,
            TournamentBroadcaster broadcaster,
            ParticipantRegistry participantRegistry,
            TournamentValidationService validation, ProfileService profileService, CooldownService cooldownService) {
        this.engine = engine;
        this.broadcaster = broadcaster;
        this.participantRegistry = participantRegistry;
        this.validation = validation;
        this.profileService = profileService;
        this.cooldownService = cooldownService;
    }

    @Override
    public void hostTournament(Player host, TournamentType type, Kit kit) {
        if (!validation.canPlayerHostTournament(host)) return;

        int numericId = numericIdGenerator.getAndIncrement();

        TournamentConfiguration config =
                TournamentConfiguration.builder()
                        .numericId(numericId)
                        .host(host)
                        .kit(kit)
                        .displayName(type.getDisplayName())
                        .teamSize(type.getTeamSize())
                        .maxTeams(type.getMaxTeams())
                        .minTeams(type.getMinTeams())
                        .isAdminHosted(false)
                        .build();

        Tournament tournament = engine.initializeTournament(config);
        activeTournaments.add(tournament);

        scheduleBroadcastTask(tournament);
        scheduleInactivityTask(tournament);
        startGlobalHostCooldown();

        broadcaster.broadcast(new BroadcastEvent.TournamentHosted(tournament));
    }

    @Override
    public void adminHostTournament(Player host, Kit kit, int teamSize, int maxTeams) {
        int numericId = numericIdGenerator.getAndIncrement();

        TournamentConfiguration config =
                TournamentConfiguration.builder()
                        .numericId(numericId)
                        .host(host)
                        .kit(kit)
                        .displayName(teamSize + "v" + teamSize)
                        .teamSize(teamSize)
                        .maxTeams(maxTeams)
                        .minTeams(3)
                        .isAdminHosted(true)
                        .build();

        Tournament tournament = engine.initializeTournament(config);
        activeTournaments.add(tournament);

        scheduleBroadcastTask(tournament);
        scheduleInactivityTask(tournament);

        broadcaster.broadcast(new BroadcastEvent.TournamentHosted(tournament));
    }

    @Override
    public void joinTournament(Player player, Tournament tournament) {
        engine.processEvent(tournament, new TournamentEvent.PlayerJoinRequest(player));
        pruneIfEnded(tournament);
    }

    @Override
    public void handlePlayerDeparture(Player player) {
        Profile profile = profileService.getProfile(player.getUniqueId());
        if (profile != null && profile.getMatch() != null) {
            return;
        }

        Tournament tournament = getPlayerTournament(player);
        if (tournament == null) return;

        engine.processEvent(tournament, new TournamentEvent.PlayerDeparture(player));
        pruneIfEnded(tournament);
    }

    @Override
    public void handleMatchEnd(Match match) {
        Tournament tournament = findTournamentByMatch(match);
        if (tournament == null) return;

        engine.processEvent(tournament, new TournamentEvent.MatchCompletion(match));
        pruneIfEnded(tournament);
    }

    @Override
    public void forceStartTournament(Tournament tournament) {
        if (!validation.canForceStartTournament(tournament)) return;

        engine.processEvent(tournament, new TournamentEvent.ForceStart(null));
        pruneIfEnded(tournament);
    }

    @Override
    public void cancelTournament(Tournament tournament, String reason) {
        engine.processEvent(tournament, new TournamentEvent.AdminCancellation(reason));
        clearGlobalHostCooldown();
        disposeTournament(tournament);
    }

    @Override
    public Tournament getTournament(UUID tournamentId) {
        return activeTournaments.stream()
                .filter(t -> t.getTournamentId().equals(tournamentId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Tournament getPlayerTournament(Player player) {
        return activeTournaments.stream()
                .filter(t -> t.getState() != TournamentState.ENDED)
                .filter(t -> participantRegistry.findParticipantByPlayer(player, t) != null)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Tournament> getTournaments() {
        return new ArrayList<>(activeTournaments);
    }

    /**
     * Starts the global tournament host cooldown window.
     * The cooldown duration is defined by CooldownType.TOURNAMENT_HOST.
     */
    private void startGlobalHostCooldown() {
        if (cooldownService == null) return;

        Cooldown cooldown = new Cooldown(CooldownType.TOURNAMENT_HOST, () -> {
        });
        cooldown.resetCooldown();
        cooldownService.addCooldown(
                GLOBAL_TOURNAMENT_COOLDOWN_KEY, CooldownType.TOURNAMENT_HOST, cooldown);
    }

    /**
     * Clears the global tournament host cooldown (e.g., on cancel/inactivity).
     */
    private void clearGlobalHostCooldown() {
        if (cooldownService == null) return;

        cooldownService.removeCooldown(
                GLOBAL_TOURNAMENT_COOLDOWN_KEY, CooldownType.TOURNAMENT_HOST);
    }

    /**
     * Disposes of a tournament by canceling all associated tasks and clearing its data.
     * This is called when a tournament ends or is canceled.
     *
     * @param tournament The tournament to dispose of.
     */
    private void disposeTournament(Tournament tournament) {
        if (tournament.getStartingTask() != null) {
            tournament.getStartingTask().cancel();
            tournament.setStartingTask(null);
        }
        if (tournament.getRoundStartTask() != null) {
            tournament.getRoundStartTask().cancel();
            tournament.setRoundStartTask(null);
        }
        if (tournament.getInactivityTask() != null) {
            tournament.getInactivityTask().cancel();
            tournament.setInactivityTask(null);
        }
        if (tournament.getBroadcastTask() != null) {
            tournament.getBroadcastTask().cancel();
            tournament.setBroadcastTask(null);
        }

        TournamentCountdownService countdowns = AlleyPlugin.getInstance().getService(TournamentCountdownService.class);

        if (countdowns != null) {
            countdowns.clearStartTask();
            countdowns.clearRoundStartTask();
        }

        participantRegistry.purgeTournament(tournament);

        tournament.getActiveMatches().clear();
        tournament.getWaitingPool().clear();
        tournament.getParticipants().clear();
        tournament.getRoundParticipants().clear();
        tournament.getPlacementList().clear();

        activeTournaments.remove(tournament);
    }

    /**
     * Schedules the periodic broadcast task for a tournament.
     *
     * @param tournament The tournament to schedule broadcasts for.
     */
    private void scheduleBroadcastTask(Tournament tournament) {
        TournamentBroadcastTask task = new TournamentBroadcastTask(tournament);
        tournament.setBroadcastTask(
                Bukkit.getScheduler()
                        .runTaskTimer(AlleyPlugin.getInstance(), task, 0L, 300L));
    }

    /**
     * Schedules a task to cancel the tournament after 5 minutes of inactivity.
     * This is to prevent tournaments from lingering indefinitely without activity.
     *
     * @param tournament The tournament to monitor for inactivity.
     */
    private void scheduleInactivityTask(Tournament tournament) {
        tournament.setInactivityTask(Bukkit.getScheduler().runTaskLater(AlleyPlugin.getInstance(),
                () -> {
                    engine.processEvent(tournament, new TournamentEvent.AdminCancellation("Inactivity"));
                    clearGlobalHostCooldown();
                    disposeTournament(tournament);
                },
                6000L));
    }

    /**
     * Removes a tournament from active list if it has ended.
     *
     * @param tournament The tournament to evaluate.
     */
    private void pruneIfEnded(Tournament tournament) {
        if (tournament.getState() == TournamentState.ENDED) {
            disposeTournament(tournament);
        }
    }

    /**
     * Finds the tournament containing a specific match.
     *
     * @param match The match to search for.
     * @return The tournament or null if not found.
     */
    private Tournament findTournamentByMatch(Match match) {
        return activeTournaments.stream()
                .filter(t -> t.getActiveMatches().contains(match))
                .findFirst()
                .orElse(null);
    }
}