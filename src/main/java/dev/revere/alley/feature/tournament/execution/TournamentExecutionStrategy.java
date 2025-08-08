package dev.revere.alley.feature.tournament.execution;

import dev.revere.alley.bootstrap.lifecycle.Service;
import dev.revere.alley.feature.tournament.engine.TournamentEvent;
import dev.revere.alley.feature.tournament.model.Tournament;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
public interface TournamentExecutionStrategy extends Service {
    /**
     * Handles an external event (join, leave, match end, timers) during
     * tournament execution.
     *
     * @param tournament The tournament receiving the event.
     * @param event      The event to process.
     * @return The result of event processing.
     */
    ExecutionResult handleEvent(Tournament tournament, TournamentEvent event);
}