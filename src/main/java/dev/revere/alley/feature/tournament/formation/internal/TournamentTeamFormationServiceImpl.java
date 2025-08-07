package dev.revere.alley.feature.tournament.formation.internal;

import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.feature.tournament.formation.TeamFormationStrategy;
import dev.revere.alley.feature.tournament.formation.TeamFormationStrategyFactory;
import dev.revere.alley.feature.tournament.formation.TournamentTeamFormationService;
import dev.revere.alley.feature.tournament.model.Tournament;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;

import java.util.ArrayList;
import java.util.List;

@Service(provides = TournamentTeamFormationService.class, priority = 1100)
public class TournamentTeamFormationServiceImpl implements TournamentTeamFormationService {
    private final TeamFormationStrategy teamFormationStrategy;

    public TournamentTeamFormationServiceImpl() {
        // todo: make type configurable in config
        this.teamFormationStrategy = TeamFormationStrategyFactory.createStrategy(
                TeamFormationStrategyFactory.StrategyType.BALANCED
        );
    }

    @Override
    public List<TournamentParticipant> formTeamsForTournament(Tournament tournament) {
        List<TournamentParticipant> participantPool = new ArrayList<>(tournament.getWaitingPool());
        int maxTeamSize = tournament.getTeamSize();

        return teamFormationStrategy.formTeams(participantPool, maxTeamSize);
    }
}