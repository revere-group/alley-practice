package dev.revere.alley.feature.tournament.match;

import dev.revere.alley.bootstrap.lifecycle.Service;
import dev.revere.alley.feature.match.model.GameParticipant;
import dev.revere.alley.feature.match.model.internal.MatchGamePlayer;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
public interface MatchParticipantFactory extends Service {
    /**
     * Creates a GameParticipant for a tournament team, producing a
     * TeamGameParticipant for teams (>1) or a solo participant for 1.
     *
     * @param participant The tournament team.
     * @return The constructed GameParticipant.
     */
    GameParticipant<MatchGamePlayer> buildParticipant(TournamentParticipant participant);
}