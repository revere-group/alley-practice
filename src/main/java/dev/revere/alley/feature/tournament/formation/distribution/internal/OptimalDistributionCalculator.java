package dev.revere.alley.feature.tournament.formation.distribution.internal;

import dev.revere.alley.feature.tournament.formation.distribution.DistributionOptimizer;
import dev.revere.alley.feature.tournament.formation.distribution.TeamDistributionCalculator;
import dev.revere.alley.feature.tournament.formation.model.TeamDistribution;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OptimalDistributionCalculator implements TeamDistributionCalculator {
    private final DistributionOptimizer optimizer;

    public OptimalDistributionCalculator() {
        this.optimizer = new BalanceBasedOptimizer();
    }

    @Override
    public TeamDistribution calculateDistribution(List<TournamentParticipant> participantPool, int maxTeamSize) {
        List<Integer> partySizes = participantPool.stream()
                .map(TournamentParticipant::getSize)
                .collect(Collectors.toList());

        List<List<Integer>> allPossibleDistributions = generateAllValidDistributions(partySizes, maxTeamSize);

        List<Integer> optimalDistribution = optimizer.findOptimalDistribution(allPossibleDistributions);

        return new TeamDistribution(optimalDistribution);
    }

    private List<List<Integer>> generateAllValidDistributions(List<Integer> partySizes, int maxTeamSize) {
        List<List<Integer>> results = new ArrayList<>();
        List<List<Integer>> currentTeams = new ArrayList<>();

        generateDistributionsRecursively(partySizes, 0, currentTeams, maxTeamSize, results);

        return results;
    }

    private void generateDistributionsRecursively(List<Integer> partySizes, int partyIndex,
                                                  List<List<Integer>> currentTeams, int maxTeamSize,
                                                  List<List<Integer>> results) {
        if (partyIndex >= partySizes.size()) {
            List<Integer> teamSizes = currentTeams.stream()
                    .map(team -> team.stream().mapToInt(Integer::intValue).sum())
                    .filter(size -> size > 0)
                    .collect(Collectors.toList());
            results.add(new ArrayList<>(teamSizes));
            return;
        }

        int currentPartySize = partySizes.get(partyIndex);

        for (int teamIndex = 0; teamIndex < currentTeams.size(); teamIndex++) {
            List<Integer> team = currentTeams.get(teamIndex);
            int currentTeamSize = team.stream().mapToInt(Integer::intValue).sum();

            if (currentTeamSize + currentPartySize <= maxTeamSize) {
                team.add(currentPartySize);
                generateDistributionsRecursively(partySizes, partyIndex + 1, currentTeams, maxTeamSize, results);
                team.remove(team.size() - 1);
            }
        }

        List<Integer> newTeam = new ArrayList<>();
        newTeam.add(currentPartySize);
        currentTeams.add(newTeam);
        generateDistributionsRecursively(partySizes, partyIndex + 1, currentTeams, maxTeamSize, results);
        currentTeams.remove(currentTeams.size() - 1);
    }
}
