package dev.revere.alley.feature.tournament.task;

import dev.revere.alley.bootstrap.lifecycle.Service;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
public interface TournamentCountdownService extends Service {
    /**
     * Gets the current start countdown task runnable.
     *
     * @return The TournamentStartTask, or null if none is active.
     */
    TournamentStartTask getStartTask();

    /**
     * Gets the current round start countdown task runnable.
     *
     * @return The TournamentRoundStartTask, or null if none is active.
     */
    TournamentRoundStartTask getRoundStartTask();

    /**
     * Registers the active start countdown runnable.
     *
     * @param task The start countdown runnable.
     */
    void setStartTask(TournamentStartTask task);

    /**
     * Clears the currently registered start countdown runnable.
     */
    void clearStartTask();

    /**
     * Registers the active round start countdown runnable.
     *
     * @param task The round start countdown runnable.
     */
    void setRoundStartTask(TournamentRoundStartTask task);

    /**
     * Clears the currently registered round start countdown runnable.
     */
    void clearRoundStartTask();
}