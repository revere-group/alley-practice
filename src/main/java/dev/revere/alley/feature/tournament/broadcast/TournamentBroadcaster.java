package dev.revere.alley.feature.tournament.broadcast;

import dev.revere.alley.bootstrap.lifecycle.Service;
import dev.revere.alley.feature.tournament.model.Tournament;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
public interface TournamentBroadcaster extends Service {
    /**
     * Broadcasts a tournament event to all relevant players.
     *
     * @param event The broadcast event to send.
     */
    void broadcast(BroadcastEvent event);

    /**
     * Sends a targeted message to specific participants of a tournament.
     *
     * @param tournament   The tournament context.
     * @param participants The participants to message.
     * @param message      The message to send.
     */
    void sendTargetedMessage(Tournament tournament, Iterable<TournamentParticipant> participants, String message);

    /**
     * Broadcasts the final tournament results and winner (top 3), using the
     * exact format of the original system.
     *
     * @param tournament The completed tournament.
     * @param placements The final participant placements.
     */
    void broadcastResults(Tournament tournament, TournamentPlacements placements);
}