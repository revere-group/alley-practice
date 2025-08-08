package dev.revere.alley.feature.tournament.state.internal;

import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.feature.tournament.model.Tournament;
import dev.revere.alley.feature.tournament.model.TournamentState;
import dev.revere.alley.feature.tournament.state.TournamentStateManager;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
@Service(provides = TournamentStateManager.class, priority = 1100)
public class DefaultTournamentStateManager implements TournamentStateManager {
    private final Map<TournamentState, Set<TournamentState>> allowed =
            new EnumMap<>(TournamentState.class);

    public DefaultTournamentStateManager() {
        allowed.put(
                TournamentState.WAITING,
                EnumSet.of(TournamentState.STARTING, TournamentState.ENDED));
        allowed.put(
                TournamentState.STARTING,
                EnumSet.of(TournamentState.IN_PROGRESS, TournamentState.ENDED));
        allowed.put(
                TournamentState.IN_PROGRESS,
                EnumSet.of(TournamentState.INTERMISSION, TournamentState.ENDED));
        allowed.put(
                TournamentState.INTERMISSION,
                EnumSet.of(TournamentState.IN_PROGRESS, TournamentState.ENDED));
        allowed.put(TournamentState.ENDED, EnumSet.of(TournamentState.ENDED));
    }

    @Override
    public boolean canTransitionTo(Tournament tournament, TournamentState newState) {
        return allowed.getOrDefault(tournament.getState(), EnumSet.noneOf(TournamentState.class))
                .contains(newState);
    }

    @Override
    public Tournament transitionState(Tournament tournament, TournamentState newState) {
        if (!canTransitionTo(tournament, newState)) {
            return tournament;
        }
        tournament.setState(newState);
        return tournament;
    }
}