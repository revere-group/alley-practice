package dev.revere.alley.feature.tournament.formation.assembly;

import dev.revere.alley.feature.tournament.model.TournamentParticipant;

import java.util.List;

public interface PartyMatcher {
    TournamentParticipant createTeamOfSize(List<TournamentParticipant> availableParties,
                                           int targetSize,
                                           int maxTeamSize);
}