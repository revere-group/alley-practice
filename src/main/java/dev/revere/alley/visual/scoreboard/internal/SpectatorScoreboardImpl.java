package dev.revere.alley.visual.scoreboard.internal;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.core.config.ConfigService;
import dev.revere.alley.feature.match.Match;
import dev.revere.alley.feature.match.internal.types.DefaultMatch;
import dev.revere.alley.feature.match.internal.types.FFAMatch;
import dev.revere.alley.feature.match.model.internal.MatchGamePlayer;
import dev.revere.alley.feature.match.model.GameParticipant;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.visual.scoreboard.Scoreboard;
import dev.revere.alley.common.text.CC;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Emmy
 * @project Alley
 * @since 30/04/2025
 */
public class SpectatorScoreboardImpl implements Scoreboard {
    @Override
    public List<String> getLines(Profile profile) {
        ConfigService configService = AlleyPlugin.getInstance().getService(ConfigService.class);
        List<String> scoreboardLines = new ArrayList<>();
        Match match = profile.getMatch();

        if (match == null) {
            return Collections.emptyList();
        }

        GameParticipant<MatchGamePlayer> participantA = getParticipantSafely(profile.getMatch().getParticipants(), 0);
        GameParticipant<MatchGamePlayer> participantB = getParticipantSafely(profile.getMatch().getParticipants(), 1);

        String playerAName = getPlayerNameSafely(participantA);
        String playerBName = getPlayerNameSafely(participantB);
        String pingA = getPingSafely(participantA);
        String pingB = getPingSafely(participantB);

        if (match instanceof DefaultMatch) {
            for (String line : configService.getScoreboardConfig().getStringList("scoreboard.lines.spectating.regular-match")) {
                scoreboardLines.add(CC.translate(line)
                        .replaceAll("\\{playerA}", playerAName)
                        .replaceAll("\\{playerB}", playerBName)
                        .replaceAll("\\{pingA}", pingA)
                        .replaceAll("\\{pingB}", pingB)
                        .replaceAll("\\{colorA}", String.valueOf(((DefaultMatch) profile.getMatch()).getTeamAColor()))
                        .replaceAll("\\{colorB}", String.valueOf(((DefaultMatch) profile.getMatch()).getTeamBColor()))
                        .replaceAll("\\{duration}", profile.getMatch().getDuration())
                        .replaceAll("\\{arena}", profile.getMatch().getArena().getDisplayName() == null ? "&c&lNULL" : profile.getMatch().getArena().getDisplayName())
                        .replaceAll("\\{kit}", profile.getMatch().getKit().getDisplayName()));
            }
        } else if (match instanceof FFAMatch) {
            FFAMatch ffaMatch = (FFAMatch) match;

            for (String line : configService.getScoreboardConfig().getStringList("scoreboard.lines.spectating.ffa-match")) {
                if (line.contains("{player_list}")) {
                    List<MatchGamePlayer> alivePlayers = ffaMatch.getParticipants().stream()
                            .filter(p -> !p.isAllEliminated())
                            .map(GameParticipant::getLeader)
                            .limit(5)
                            .collect(Collectors.toList());

                    for (MatchGamePlayer gamePlayer : alivePlayers) {
                        Player p = gamePlayer.getTeamPlayer();
                        if (p != null) {
                            scoreboardLines.add(CC.translate(" &7- &f" + p.getName() + " &7(&a" + getPing(p) + "ms&7)"));
                        }
                    }
                    continue;
                }

                long aliveCount = ffaMatch.getParticipants().stream().filter(p -> !p.isAllEliminated()).count();
                line = line.replace("{players_alive}", String.valueOf(aliveCount));
                line = line.replace("{players_total}", String.valueOf(ffaMatch.getParticipants().size()));
                line = line.replace("{kit}", ffaMatch.getKit().getDisplayName());
                line = line.replace("{arena}", ffaMatch.getArena().getDisplayName());

                scoreboardLines.add(CC.translate(line));
            }
        } else if (profile.getFfaMatch() != null) {
            for (String line : configService.getScoreboardConfig().getStringList("scoreboard.lines.spectating.ffa")) {
                scoreboardLines.add(CC.translate(line)
                        .replaceAll("\\{arena}", profile.getFfaMatch().getArena().getDisplayName() == null ? "&c&lNULL" : profile.getFfaMatch().getArena().getDisplayName())
                        .replaceAll("\\{kit}", profile.getFfaMatch().getKit().getDisplayName()));
            }
        }

        return scoreboardLines;
    }

    @Override
    public List<String> getLines(Profile profile, Player player) {
        return Collections.emptyList();
    }

    /**
     * Safely gets a participant from the list at the specified index.
     *
     * @param participants The list of participants
     * @param index        The index to retrieve
     * @return The participant at the index, or null if not available
     */
    private GameParticipant<MatchGamePlayer> getParticipantSafely(List<GameParticipant<MatchGamePlayer>> participants, int index) {
        if (participants == null || index >= participants.size() || index < 0) {
            return null;
        }
        return participants.get(index);
    }

    /**
     * Safely gets the player name from a participant.
     *
     * @param participant The participant to get the name from
     * @return The player name, or "Disconnected" if not available
     */
    private String getPlayerNameSafely(GameParticipant<MatchGamePlayer> participant) {
        if (participant == null) {
            return "&c&lDisconnected";
        }

        if (!participant.getPlayers().isEmpty()) {
            return participant.getPlayers().get(0).getUsername();
        }

        if (!participant.getAllPlayers().isEmpty()) {
            return "&7" + participant.getAllPlayers().get(0).getUsername() + " &c(DC)";
        }

        return "&c&lDisconnected";
    }

    /**
     * Safely gets the ping from a participant.
     *
     * @param participant The participant to get the ping from
     * @return The ping as a string, or "0" if not available
     */
    private String getPingSafely(GameParticipant<MatchGamePlayer> participant) {
        if (participant == null) {
            return "0";
        }

        if (!participant.getPlayers().isEmpty()) {
            Player player = participant.getPlayers().get(0).getTeamPlayer();
            if (player != null) {
                return String.valueOf(this.getPing(player));
            }
        }
        return "0";
    }
}