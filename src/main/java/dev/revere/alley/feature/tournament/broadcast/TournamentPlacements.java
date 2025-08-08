package dev.revere.alley.feature.tournament.broadcast;

import dev.revere.alley.feature.tournament.model.TournamentParticipant;
import lombok.Getter;

import java.util.List;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
@Getter
public class TournamentPlacements {
    private final List<TournamentParticipant> orderedPlacements;
    private final int totalParticipants;

    public TournamentPlacements(List<TournamentParticipant> placements, int total) {
        this.orderedPlacements = placements;
        this.totalParticipants = total;
    }

    /**
     * Gets the participant at 1-based position.
     *
     * @param position The position (1..n).
     * @return The participant at position or null.
     */
    public TournamentParticipant getPlacement(int position) {
        if (position <= 0 || position > orderedPlacements.size()) {
            return null;
        }
        return orderedPlacements.get(position - 1);
    }

    /**
     * Gets the winner (position 1).
     *
     * @return Winner participant.
     */
    public TournamentParticipant getWinner() {
        return getPlacement(1);
    }
}