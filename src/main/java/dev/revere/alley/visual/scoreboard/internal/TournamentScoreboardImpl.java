package dev.revere.alley.visual.scoreboard.internal;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.common.tournament.RoundStageUtil;
import dev.revere.alley.core.config.ConfigService;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.feature.tournament.model.Tournament;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;
import dev.revere.alley.feature.tournament.model.TournamentState;
import dev.revere.alley.feature.tournament.task.TournamentCountdownService;
import dev.revere.alley.feature.tournament.task.TournamentRoundStartTask;
import dev.revere.alley.feature.tournament.task.TournamentStartTask;
import dev.revere.alley.visual.scoreboard.Scoreboard;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Remi
 * @project alley-practice
 * @date 6/08/2025
 */
public class TournamentScoreboardImpl implements Scoreboard {

    @Override
    public List<String> getLines(Profile profile) {
        Tournament tournament = profile.getTournament();
        if (tournament == null) {
            return Collections.emptyList();
        }

        ConfigService configService = AlleyPlugin.getInstance().getService(ConfigService.class);
        String path;

        switch (tournament.getState()) {
            case STARTING:
                path = "scoreboard.lines.tournament.starting";
                break;
            case IN_PROGRESS:
                path = "scoreboard.lines.tournament.in-progress";
                break;
            case INTERMISSION:
                path = "scoreboard.lines.tournament.intermission";
                break;
            case WAITING:
            default:
                path = "scoreboard.lines.tournament.waiting";
                break;
        }

        List<String> template = configService.getScoreboardConfig().getStringList(path);
        return template.stream()
                .map(line -> replacePlaceholders(line, tournament))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getLines(Profile profile, Player player) {
        return getLines(profile);
    }

    private String replacePlaceholders(String line, Tournament tournament) {
        int currentPlayers = tournament.getWaitingPool().stream().mapToInt(TournamentParticipant::getSize).sum();
        int maxPlayers = tournament.getMaxTeams() * tournament.getTeamSize();

        int teamsLeftForStage = computeTeamsLeftForDisplay(tournament);
        String stageName = RoundStageUtil.getRoundStageName(teamsLeftForStage);

        String processed =
                CC.translate(line)
                        .replace("{tournament_type}", tournament.getDisplayName())
                        .replace("{tournament_host}", tournament.getHostName())
                        .replace("{tournament_kit}", tournament.getKit().getDisplayName())
                        .replace("{tournament_players}", String.valueOf(currentPlayers))
                        .replace("{tournament_max_players}", String.valueOf(maxPlayers))
                        .replace("{tournament_round}", String.valueOf(tournament.getCurrentRound()))
                        .replace("{tournament_teams_left}", String.valueOf(tournament.getRoundParticipants().size()))
                        .replace("{tournament_teams_total}", String.valueOf(tournament.getParticipants().size()))
                        .replace("{tournament_round_stage}", stageName);
        if (processed.contains("{countdown}")) {
            processed = processed.replace("{countdown}", String.valueOf(getStartCountdownSecondsDisplay(tournament)));
        }

        if (processed.contains("{round_countdown}")) {
            processed = processed.replace("{round_countdown}", String.valueOf(getRoundCountdownSecondsDisplay(tournament)));
        }

        return processed;
    }

    /**
     * Computes teams left for stage display, in a state-aware manner:
     * - IN_PROGRESS/INTERMISSION: use roundParticipants size
     * - STARTING/WAITING: estimate by floor(total waiting players / team size),
     * capped by maxTeams, and at least 0
     *
     * @param tournament The tournament
     * @return Teams left to display
     */
    private int computeTeamsLeftForDisplay(Tournament tournament) {
        if (tournament.getState() == TournamentState.IN_PROGRESS
                || tournament.getState() == TournamentState.INTERMISSION) {
            return tournament.getRoundParticipants().size();
        }

        int teamSize = Math.max(1, tournament.getTeamSize());
        int totalWaiting =
                tournament.getWaitingPool().stream().mapToInt(TournamentParticipant::getSize).sum();

        int estimate = totalWaiting / teamSize;
        estimate = Math.min(estimate, tournament.getMaxTeams());
        return Math.max(0, estimate);
    }

    /**
     * Returns the start countdown for the scoreboard, adjusted to match chat.
     * The tasks print before decrementing; scoreboard reads after, so add +1.
     *
     * @param tournament The tournament context.
     * @return Remaining seconds as displayed on the scoreboard.
     */
    private int getStartCountdownSecondsDisplay(Tournament tournament) {
        if (tournament.getState() != TournamentState.STARTING) {
            return 0;
        }

        TournamentCountdownService countdowns = AlleyPlugin.getInstance().getService(TournamentCountdownService.class);

        TournamentStartTask task = countdowns.getStartTask();
        int seconds = task != null ? task.getCountdown() + 1 : 0;
        return Math.max(0, seconds);
    }

    /**
     * Returns the round countdown for the scoreboard, adjusted to match chat.
     *
     * @param tournament The tournament context.
     * @return Remaining seconds as displayed on the scoreboard.
     */
    private int getRoundCountdownSecondsDisplay(Tournament tournament) {
        if (tournament.getState() != TournamentState.INTERMISSION) {
            return 0;
        }

        TournamentCountdownService countdowns = AlleyPlugin.getInstance().getService(TournamentCountdownService.class);

        TournamentRoundStartTask task = countdowns.getRoundStartTask();
        int seconds = task != null ? task.getCountdown() + 1 : 0;
        return Math.max(0, seconds);
    }
}