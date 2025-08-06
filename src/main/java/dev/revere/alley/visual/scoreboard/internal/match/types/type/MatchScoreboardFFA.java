package dev.revere.alley.visual.scoreboard.internal.match.types.type;

import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.feature.match.internal.types.FFAMatch;
import dev.revere.alley.feature.match.model.GameParticipant;
import dev.revere.alley.feature.match.model.MatchGamePlayerData;
import dev.revere.alley.feature.match.model.internal.MatchGamePlayer;
import dev.revere.alley.visual.scoreboard.internal.match.BaseMatchScoreboard;
import dev.revere.alley.visual.scoreboard.internal.match.annotation.ScoreboardData;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Remi
 * @project Alley
 * @since 26/06/2025
 */
@ScoreboardData(match = FFAMatch.class)
public class MatchScoreboardFFA extends BaseMatchScoreboard {
    @Override
    protected String getSoloConfigPath() {
        return "scoreboard.lines.playing.solo.ffa-match";
    }

    @Override
    protected String getTeamConfigPath() {
        return "";
    }

    @Override
    public List<String> getLines(Profile profile, Player player, GameParticipant<MatchGamePlayer> you, GameParticipant<MatchGamePlayer> opponent) {
        List<String> lines = super.getLines(profile, player, you, opponent);
        List<String> processedLines = new ArrayList<>();

        FFAMatch ffaMatch = (FFAMatch) profile.getMatch();
        MatchGamePlayer gamePlayer = ffaMatch.getGamePlayer(player);
        if (gamePlayer == null) {
            return lines;
        }
        MatchGamePlayerData data = gamePlayer.getData();

        for (String line : lines) {
            line = line.replace("{kit}", ffaMatch.getKit().getDisplayName());
            line = line.replace("{players}", String.valueOf(ffaMatch.getParticipants().size()));
            line = line.replace("{ping}", String.valueOf(getPing(player)));
            line = line.replace("{kills}", String.valueOf(data.getKills()));
            line = line.replace("{deaths}", String.valueOf(data.getDeaths()));

            processedLines.add(line);
        }

        return processedLines;
    }
}
