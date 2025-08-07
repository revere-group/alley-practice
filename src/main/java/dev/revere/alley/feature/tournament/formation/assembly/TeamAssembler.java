package dev.revere.alley.feature.tournament.formation.assembly;

import dev.revere.alley.feature.tournament.formation.model.TeamDistribution;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;

import java.util.List;

public interface TeamAssembler {
    List<TournamentParticipant> assembleTeams(List<TournamentParticipant> participantPool,
                                              TeamDistribution distribution,
                                              int maxTeamSize);
}