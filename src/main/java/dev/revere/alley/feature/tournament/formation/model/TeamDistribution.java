package dev.revere.alley.feature.tournament.formation.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
public class TeamDistribution {
    private final List<Integer> teamSizes;

    public TeamDistribution(List<Integer> teamSizes) {
        this.teamSizes = Collections.unmodifiableList(new ArrayList<>(teamSizes));
    }

    @Override
    public String toString() {
        return teamSizes.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TeamDistribution that = (TeamDistribution) obj;
        return Objects.equals(teamSizes, that.teamSizes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamSizes);
    }
}