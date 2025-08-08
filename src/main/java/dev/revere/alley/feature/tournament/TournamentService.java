package dev.revere.alley.feature.tournament;

import dev.revere.alley.bootstrap.lifecycle.Service;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.match.Match;
import dev.revere.alley.feature.tournament.model.Tournament;
import dev.revere.alley.feature.tournament.model.TournamentType;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * @author Remi
 * @project alley-practice
 * @date 6/08/2025
 */
public interface TournamentService extends Service {
    /**
     * Hosts a new tournament, broadcasting its availability to all players.
     * This method applies a cooldown to prevent rapid tournament hosting.
     *
     * @param host The player hosting the tournament.
     * @param type The type of tournament (e.g., SOLO, DUO).
     * @param kit  The kit to be used in the tournament matches.
     */
    void hostTournament(Player host, TournamentType type, Kit kit);

    /**
     * Allows an admin to host a new tournament without restrictions.
     * This method bypasses the hosting cooldown and uses custom team size and
     * max teams.
     *
     * @param host     The admin player hosting the tournament.
     * @param kit      The kit to be used in the tournament matches.
     * @param teamSize The number of players per team.
     * @param maxTeams The maximum number of teams.
     */
    void adminHostTournament(Player host, Kit kit, int teamSize, int maxTeams);

    /**
     * Adds a player or their party to the tournament's waiting pool.
     *
     * @param player     The player attempting to join.
     * @param tournament The tournament to join.
     */
    void joinTournament(Player player, Tournament tournament);

    /**
     * Handles a player leaving the tournament, either during the waiting phase
     * or mid-match.
     *
     * @param player The player who is departing.
     */
    void handlePlayerDeparture(Player player);

    /**
     * Processes the end of a tournament match, determining the winner and loser
     * and updating the tournament's state accordingly.
     *
     * @param match The match that has just ended.
     */
    void handleMatchEnd(Match match);

    /**
     * Forcibly starts a tournament if enough players have joined, bypassing the
     * need for a full lobby.
     *
     * @param tournament The tournament to force start.
     */
    void forceStartTournament(Tournament tournament);

    /**
     * Immediately cancels a tournament, notifying all participants and cleaning
     * up resources.
     *
     * @param tournament The tournament to cancel.
     * @param reason     The reason for the cancellation.
     */
    void cancelTournament(Tournament tournament, String reason);

    /**
     * Retrieves an active tournament by its unique ID.
     *
     * @param tournamentId The UUID of the tournament.
     * @return The Tournament object, or {@code null} if not found.
     */
    Tournament getTournament(UUID tournamentId);

    /**
     * Gets the tournament a specific player is currently participating in.
     *
     * @param player The player to check.
     * @return The Tournament object, or {@code null} if the player is not in one.
     */
    Tournament getPlayerTournament(Player player);

    /**
     * Retrieves a list of all currently active tournaments.
     *
     * @return A list of all active Tournament objects.
     */
    List<Tournament> getTournaments();
}