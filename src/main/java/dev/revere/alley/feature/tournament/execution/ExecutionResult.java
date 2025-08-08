package dev.revere.alley.feature.tournament.execution;

import dev.revere.alley.feature.tournament.model.Tournament;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
@Getter
@RequiredArgsConstructor
public class ExecutionResult {
    private final ExecutionStatus status;
    private final String message;
    private final Tournament updatedTournament;

    /**
     * Status of an execution result.
     */
    public enum ExecutionStatus {
        CONTINUE,
        TRANSITION,
        COMPLETE,
        ERROR
    }

    /**
     * Creates a continue result.
     *
     * @param tournament The current tournament.
     * @return The continue result.
     */
    public static ExecutionResult continueExecution(Tournament tournament) {
        return new ExecutionResult(ExecutionStatus.CONTINUE, null, tournament);
    }

    /**
     * Creates a transition result with message.
     *
     * @param tournament The tournament.
     * @param message    A message describing the transition.
     * @return The transition result.
     */
    public static ExecutionResult transitionState(
            Tournament tournament, String message) {
        return new ExecutionResult(ExecutionStatus.TRANSITION, message, tournament);
    }

    /**
     * Creates a completion result with message.
     *
     * @param tournament The tournament.
     * @param message    A message describing the completion.
     * @return The completion result.
     */
    public static ExecutionResult complete(Tournament tournament, String message) {
        return new ExecutionResult(ExecutionStatus.COMPLETE, message, tournament);
    }
}