package dev.revere.alley.feature.tournament.formation.distribution.internal;

import dev.revere.alley.feature.tournament.formation.distribution.DistributionOptimizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BalanceBasedOptimizer implements DistributionOptimizer {
    @Override
    public List<Integer> findOptimalDistribution(List<List<Integer>> possibleDistributions) {
        return possibleDistributions.stream()
                .min(Comparator.comparingInt(this::calculateImbalanceScore))
                .orElse(new ArrayList<>());
    }

    private int calculateImbalanceScore(List<Integer> teamSizes) {
        if (teamSizes.isEmpty()) return Integer.MAX_VALUE;

        int min = Collections.min(teamSizes);
        int max = Collections.max(teamSizes);

        int imbalanceScore = max - min;

        long sizeOnePenalty = teamSizes.stream().mapToLong(size -> size == 1 ? 100 : 0).sum();

        long smallTeamPenalty = teamSizes.stream().mapToLong(size -> size == 2 ? 10 : 0).sum();

        return imbalanceScore + (int) sizeOnePenalty + (int) smallTeamPenalty;
    }
}