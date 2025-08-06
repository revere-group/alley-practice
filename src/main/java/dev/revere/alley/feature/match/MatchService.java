package dev.revere.alley.feature.match;

import dev.revere.alley.bootstrap.lifecycle.Service;
import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.match.model.GameParticipant;
import dev.revere.alley.feature.match.model.internal.MatchGamePlayer;
import dev.revere.alley.feature.tournament.model.Tournament;

import java.util.List;

/**
 * @author Remi
 * @project alley-practice
 * @date 2/07/2025
 */
public interface MatchService extends Service {
    /**
     * Gets a list of all currently active matches.
     *
     * @return An unmodifiable list of active matches.
     */
    List<Match> getMatches();

    /**
     * Gets the list of commands that are blocked for players while in a match.
     *
     * @return A list of blocked command strings.
     */
    List<String> getBlockedCommands();

    /**
     * Adds a match to the service's tracking list.
     * Should be called right after a match is created.
     *
     * @param match The match to add.
     */
    void addMatch(Match match);

    /**
     * Removes a match from the service's tracking list.
     * Should be called when a match ends.
     *
     * @param match The match to remove.
     */
    void removeMatch(Match match);

    /**
     * Creates, starts, and registers a new match with the given parameters.
     *
     * @param kit              The kit to be used in the match.
     * @param arena            The arena where the match will take place.
     * @param participantA     The first participant in the match.
     * @param participantB     The second participant in the match.
     * @param teamMatch        Whether this is a team-based match.
     * @param affectStatistics Whether this match should affect player stats (Elo, wins/losses).
     * @param isRanked         Whether this match is ranked.
     */
    void createAndStartMatch(Kit kit,
                             Arena arena,
                             GameParticipant<MatchGamePlayer> participantA,
                             GameParticipant<MatchGamePlayer> participantB,
                             boolean teamMatch,
                             boolean affectStatistics,
                             boolean isRanked);

    /**
     * Creates, starts, and registers a new match with the given parameters.
     *
     * @param kit          The kit to be used in the match.
     * @param arena        The arena where the match will take place.
     * @param participants A list of participants in the match.
     */
    void createAndStartMatch(Kit kit,
                             Arena arena,
                             List<GameParticipant<MatchGamePlayer>> participants);

    /**
     * Creates, starts, and registers a new tournament match.
     * This method will use the internal match factory to create the correct match type
     * (e.g., BedMatch) and attach the tournament context to it.
     *
     * @param tournament The tournament this match belongs to.
     * @param kit        The kit to be used.
     * @param arena      The arena where the match will take place.
     * @param pA         The first participant.
     * @param pB         The second participant.
     */
    void createTournamentMatch(Tournament tournament, Kit kit, Arena arena, GameParticipant<MatchGamePlayer> pA, GameParticipant<MatchGamePlayer> pB);
}