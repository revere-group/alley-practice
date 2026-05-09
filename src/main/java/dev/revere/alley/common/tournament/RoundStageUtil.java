package dev.revere.alley.common.tournament;

import lombok.experimental.UtilityClass;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
@UtilityClass
public final class RoundStageUtil {
    /**
     * Converts remaining team count into a human-readable stage name.
     *
     * @param teamsLeft Number of teams still in the bracket.
     * @return Stage name like "Finals", "Semi-Finals", "Quarter-Finals", or "Round of X".
     */
    public String getRoundStageName(int teamsLeft) {
        switch (teamsLeft) {
            case 2:
                return "Finals";
            case 4:
                return "Semi-Finals";
            case 8:
                return "Quarter-Finals";
            case 16:
                return "Round of 16";
            case 32:
                return "Round of 32";
            case 64:
                return "Round of 64";
            default:
                if (teamsLeft <= 1) return "Finals";
                return "Round of " + teamsLeft;
        }
    }
}