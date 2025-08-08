package dev.revere.alley.feature.tournament.player;

import dev.revere.alley.bootstrap.lifecycle.Service;
import dev.revere.alley.feature.tournament.model.Tournament;
import org.bukkit.entity.Player;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
public interface PlayerTournamentStateService extends Service {
    /**
     * Sets a player's profile state to PLAYING_TOURNAMENT and applies their
     * tournament hotbar.
     *
     * @param player The player to update.
     * @param tournament The tournament they are joining.
     */
    void setPlayerTournamentState(Player player, Tournament tournament);

    /**
     * Resets a player's profile state back to LOBBY and teleports them to spawn
     * if applicable.
     *
     * @param player The player to reset.
     */
    void resetPlayerStateToLobby(Player player);
}