package dev.revere.alley.feature.tournament.formation.strategy;

import dev.revere.alley.feature.tournament.formation.TeamFormationStrategy;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class LegacyTeamFormationStrategy implements TeamFormationStrategy {
    @Override
    public List<TournamentParticipant> formTeams(List<TournamentParticipant> participantPool, int maxTeamSize) {
        List<TournamentParticipant> teams = new ArrayList<>();
        List<TournamentParticipant> processingPool = new ArrayList<>(participantPool);

        processingPool.sort(Comparator.comparingInt(TournamentParticipant::getSize).reversed());

        while (!processingPool.isEmpty()) {
            TournamentParticipant currentTeam = processingPool.remove(0);
            while (currentTeam.getSize() < maxTeamSize && !processingPool.isEmpty()) {
                int spaceLeft = maxTeamSize - currentTeam.getSize();

                Optional<TournamentParticipant> bestFitOpt = processingPool.stream()
                        .filter(candidate -> candidate.getSize() <= spaceLeft)
                        .max(Comparator.comparingInt(TournamentParticipant::getSize));

                if (bestFitOpt.isPresent()) {
                    TournamentParticipant bestFit = bestFitOpt.get();
                    currentTeam.merge(bestFit);
                    processingPool.remove(bestFit);
                } else {
                    break;
                }
            }
            teams.add(currentTeam);
        }

        return teams;
    }
}