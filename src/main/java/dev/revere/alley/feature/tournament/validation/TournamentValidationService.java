package dev.revere.alley.feature.tournament.validation;

import dev.revere.alley.bootstrap.lifecycle.Service;
import dev.revere.alley.feature.tournament.model.Tournament;
import org.bukkit.entity.Player;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
public interface TournamentValidationService extends Service {
    /**
     * Validates if a player can host a tournament.
     *
     * @param player The player attempting to host.
     * @return True if they can host.
     */
    boolean canPlayerHostTournament(Player player);

    /**
     * Validates if a tournament can be force started.
     *
     * @param tournament The tournament to check.
     * @return True if it can be force started.
     */
    boolean canForceStartTournament(Tournament tournament);
}
