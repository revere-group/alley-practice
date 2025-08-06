package dev.revere.alley.feature.tournament.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Remi
 * @project alley-practice
 * @date 6/08/2025
 */
@Getter
@AllArgsConstructor
public enum TournamentState {
    WAITING("Waiting For Players"),
    STARTING("Starting"),
    IN_PROGRESS("In Progress"),
    INTERMISSION("Waiting For Next Round"),
    ENDED("Ended")

    ;

    private final String displayName;
}
