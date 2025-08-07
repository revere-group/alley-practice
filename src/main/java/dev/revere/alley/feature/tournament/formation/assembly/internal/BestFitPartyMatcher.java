package dev.revere.alley.feature.tournament.formation.assembly.internal;

import dev.revere.alley.feature.tournament.formation.assembly.PartyMatcher;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;

import java.util.ArrayList;
import java.util.List;

public class BestFitPartyMatcher implements PartyMatcher {
    @Override
    public TournamentParticipant createTeamOfSize(List<TournamentParticipant> availableParties,
                                                  int targetSize,
                                                  int maxTeamSize) {
        if (availableParties.isEmpty()) return null;

        List<TournamentParticipant> bestCombination = findBestCombination(availableParties, targetSize, maxTeamSize);

        if (bestCombination.isEmpty()) return null;

        availableParties.removeAll(bestCombination);

        return mergePariesToTeam(bestCombination);
    }

    private List<TournamentParticipant> findBestCombination(List<TournamentParticipant> availableParties,
                                                            int targetSize,
                                                            int maxTeamSize) {
        List<TournamentParticipant> bestCombination = new ArrayList<>();
        int bestScore = Integer.MAX_VALUE;

        int n = Math.min(availableParties.size(), 10);
        for (int mask = 1; mask < (1 << n); mask++) {
            List<TournamentParticipant> combination = new ArrayList<>();
            int totalSize = 0;

            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) {
                    TournamentParticipant party = availableParties.get(i);
                    combination.add(party);
                    totalSize += party.getSize();
                }
            }

            if (totalSize > maxTeamSize) continue;

            int score = Math.abs(targetSize - totalSize);
            if (totalSize > targetSize) score += 50;

            if (score < bestScore) {
                bestScore = score;
                bestCombination = new ArrayList<>(combination);
            }
        }

        return bestCombination;
    }

    private TournamentParticipant mergePariesToTeam(List<TournamentParticipant> parties) {
        if (parties.isEmpty()) return null;

        TournamentParticipant team = parties.get(0);
        for (int i = 1; i < parties.size(); i++) {
            team.merge(parties.get(i));
        }
        return team;
    }
}