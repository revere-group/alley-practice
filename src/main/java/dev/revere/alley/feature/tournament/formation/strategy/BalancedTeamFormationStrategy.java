package dev.revere.alley.feature.tournament.formation.strategy;

import dev.revere.alley.feature.tournament.formation.TeamFormationStrategy;
import dev.revere.alley.feature.tournament.formation.assembly.internal.PartyAwareTeamAssembler;
import dev.revere.alley.feature.tournament.formation.assembly.TeamAssembler;
import dev.revere.alley.feature.tournament.formation.distribution.TeamDistributionCalculator;
import dev.revere.alley.feature.tournament.formation.distribution.internal.OptimalDistributionCalculator;
import dev.revere.alley.feature.tournament.formation.model.TeamDistribution;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;

import java.util.List;

public class BalancedTeamFormationStrategy implements TeamFormationStrategy {
    private final TeamDistributionCalculator distributionCalculator;
    private final TeamAssembler teamAssembler;

    public BalancedTeamFormationStrategy() {
        this.distributionCalculator = new OptimalDistributionCalculator();
        this.teamAssembler = new PartyAwareTeamAssembler();
    }

    @Override
    public List<TournamentParticipant> formTeams(List<TournamentParticipant> participantPool, int maxTeamSize) {
        TeamDistribution distribution = distributionCalculator.calculateDistribution(participantPool, maxTeamSize);

        return teamAssembler.assembleTeams(participantPool, distribution, maxTeamSize);
    }
}