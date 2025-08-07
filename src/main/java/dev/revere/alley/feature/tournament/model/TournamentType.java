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
public enum TournamentType {
    SOLO("1v1", 1, 32, 3),
    DUO("2v2", 2, 16, 3),
    THREE("3v3", 3, 16, 3),
    FOUR("4v4", 4, 8, 3);

    private final String displayName;
    private final int teamSize;
    private final int maxTeams;
    private final int minTeams;
}
