package dev.revere.alley.feature.tournament.participant;

import dev.revere.alley.bootstrap.lifecycle.Service;
import dev.revere.alley.feature.tournament.model.Tournament;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
public interface ParticipantRegistry extends Service {
    /**
     * Finds the participant containing a specific player.
     *
     * @param player     The player to search for.
     * @param tournament The tournament to search in.
     * @return The participant or null if not found.
     */
    TournamentParticipant findParticipantByPlayer(Player player, Tournament tournament);

    /**
     * Creates and registers a new participant from a player (party-aware) if
     * validation passes.
     *
     * @param player     The player joining.
     * @param tournament The tournament being joined.
     * @return The created participant or null if joining failed.
     */
    TournamentParticipant registerParticipant(Player player, Tournament tournament);

    /**
     * Removes a participant from the tournament registry and resets member
     * states as needed.
     *
     * @param participant The participant to remove.
     * @param tournament  The tournament they're leaving.
     */
    void unregisterParticipant(TournamentParticipant participant, Tournament tournament);

    /**
     * Purges all participants from the tournament, resetting their states.
     *
     * @param tournament The tournament to purge.
     */
    void purgeTournament(Tournament tournament);

    /**
     * Updates a participant's status in the tournament.
     *
     * @param participant The participant to update.
     * @param status      The new status.
     */
    void updateParticipantStatus(TournamentParticipant participant, ParticipantStatus status);

    /**
     * Gets all participants with a specific status.
     *
     * @param tournament The tournament to search.
     * @param status     The status to filter by.
     * @return List of matching participants.
     */
    List<TournamentParticipant> getParticipantsByStatus(
            Tournament tournament, ParticipantStatus status);
}