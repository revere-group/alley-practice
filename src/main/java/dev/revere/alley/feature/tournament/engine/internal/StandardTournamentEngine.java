package dev.revere.alley.feature.tournament.engine.internal;

import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.feature.tournament.engine.TournamentConfiguration;
import dev.revere.alley.feature.tournament.engine.TournamentEngine;
import dev.revere.alley.feature.tournament.engine.TournamentEvent;
import dev.revere.alley.feature.tournament.execution.TournamentExecutionStrategy;
import dev.revere.alley.feature.tournament.model.Tournament;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
@Service(provides = TournamentEngine.class, priority = 1000)
public class StandardTournamentEngine implements TournamentEngine {
    private final TournamentExecutionStrategy strategy;

    public StandardTournamentEngine(TournamentExecutionStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public Tournament initializeTournament(TournamentConfiguration configuration) {
        return new Tournament(
                configuration.getNumericId(),
                configuration.getHost().getName(),
                configuration.getKit(),
                configuration.getDisplayName(),
                configuration.getTeamSize(),
                configuration.getMaxTeams(),
                configuration.getMinTeams());
    }

    @Override
    public void processEvent(Tournament tournament, TournamentEvent event) {
        strategy.handleEvent(tournament, event);
    }
}