package dev.revere.alley.feature.tournament.formation;

import dev.revere.alley.feature.tournament.model.TournamentParticipant;

import java.util.List;

public interface TeamFormationStrategy {
    /**
     * Forms balanced teams from a pool of participants.
     *
     * @param participantPool The available participants (parties/solos)
     * @param maxTeamSize The maximum size each team can have
     * @return A list of formed teams
     */
    List<TournamentParticipant> formTeams(List<TournamentParticipant> participantPool, int maxTeamSize);
}