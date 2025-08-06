package dev.revere.alley.visual.scoreboard.internal;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.config.ConfigService;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.feature.tournament.TournamentService;
import dev.revere.alley.feature.tournament.model.Tournament;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;
import dev.revere.alley.feature.tournament.task.TournamentRoundStartTask;
import dev.revere.alley.feature.tournament.task.TournamentStartTask;
import dev.revere.alley.visual.scoreboard.Scoreboard;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Field;
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
                if (tournament.getActiveMatches().isEmpty()) {
                    path = "scoreboard.lines.tournament.in-progress-waiting";
                } else {
                    path = "scoreboard.lines.tournament.in-progress";
                }
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

    private String replacePlaceholders(String line, Tournament tournament) {
        int currentPlayers = tournament.getWaitingPool().stream().mapToInt(TournamentParticipant::getSize).sum();
        int maxPlayers = tournament.getMaxTeams() * tournament.getTeamSize();

        String processedLine = CC.translate(line)
                .replace("{tournament_type}", tournament.getDisplayName())
                .replace("{tournament_kit}", tournament.getKit().getDisplayName())
                .replace("{tournament_players}", String.valueOf(currentPlayers))
                .replace("{tournament_max_players}", String.valueOf(maxPlayers))
                .replace("{tournament_round}", String.valueOf(tournament.getCurrentRound()))
                .replace("{tournament_teams_left}", String.valueOf(tournament.getRoundParticipants().size()))
                .replace("{tournament_teams_total}", String.valueOf(tournament.getParticipants().size()));

        if (processedLine.contains("{countdown}")) {
            processedLine = processedLine.replace("{countdown}", String.valueOf(getCountdown()));
        }

        if (processedLine.contains("round_countdown")) {
            processedLine = processedLine.replace("{round_countdown}", String.valueOf(getRoundCountdown(tournament)));
        }

        return processedLine;
    }

    private int getRoundCountdown(Tournament tournament) {
        if (tournament.getRoundStartTask() != null) {
            TournamentService service = AlleyPlugin.getInstance().getService(TournamentService.class);
            TournamentRoundStartTask task = service.getTournamentRoundStartTask();
            return (task != null) ? task.getCountdown() : 0;
        }
        return 0;
    }

    private int getCountdown() {
        TournamentService service = AlleyPlugin.getInstance().getService(TournamentService.class);
        TournamentStartTask task = service.getTournamentStartTask();
        return (task != null) ? task.getCountdown() : 0;
    }

    @Override
    public List<String> getLines(Profile profile, Player player) {
        return getLines(profile);
    }
}