package dev.revere.alley.feature.tournament.formation.distribution;

import java.util.List;

public interface DistributionOptimizer {
    List<Integer> findOptimalDistribution(List<List<Integer>> possibleDistributions);
}