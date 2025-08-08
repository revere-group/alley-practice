package dev.revere.alley.feature.tournament.match;

import dev.revere.alley.feature.tournament.model.TournamentParticipant;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
@Getter
@RequiredArgsConstructor
public class MatchProcessingResult {
    private final TournamentParticipant winner;
    private final TournamentParticipant loser;
    private final boolean successful;
    private final String errorMessage;

    /**
     * Creates a success result with winner and loser.
     *
     * @param winner The winning participant.
     * @param loser The losing participant.
     * @return Success result.
     */
    public static MatchProcessingResult success(TournamentParticipant winner, TournamentParticipant loser) {
        return new MatchProcessingResult(winner, loser, true, null);
    }

    /**
     * Creates a failure result with error message.
     *
     * @param error The error message.
     * @return Failure result.
     */
    public static MatchProcessingResult failure(String error) {
        return new MatchProcessingResult(null, null, false, error);
    }
}