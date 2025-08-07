package dev.revere.alley.feature.tournament.formation;

import dev.revere.alley.bootstrap.lifecycle.Service;
import dev.revere.alley.feature.tournament.model.Tournament;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;

import java.util.List;

public interface TournamentTeamFormationService extends Service {
    /**
     * Forms balanced teams for a tournament.
     *
     * @param tournament The tournament to form teams for
     * @return List of formed teams
     */
    List<TournamentParticipant> formTeamsForTournament(Tournament tournament);
}