package dev.revere.alley.feature.tournament.match;

import dev.revere.alley.bootstrap.lifecycle.Service;
import dev.revere.alley.feature.match.Match;
import dev.revere.alley.feature.tournament.model.Tournament;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;

import java.util.List;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
public interface MatchOrchestrator extends Service {
    /**
     * Creates matches for the current round of participants.
     *
     * @param tournament The tournament to create matches for.
     * @param participants The participants to match up.
     * @return The orchestration result.
     */
    MatchOrchestrationResult createRoundMatches(
            Tournament tournament, List<TournamentParticipant> participants);

    /**
     * Processes the completion of a tournament match.
     *
     * @param match The completed match.
     * @param tournament The tournament the match belongs to.
     * @return The processing result.
     */
    MatchProcessingResult processMatchResult(Match match, Tournament tournament);

    /**
     * Checks if all active matches in a tournament round are complete.
     *
     * @param tournament The tournament to check.
     * @return True if all matches are complete.
     */
    boolean isRoundComplete(Tournament tournament);
}