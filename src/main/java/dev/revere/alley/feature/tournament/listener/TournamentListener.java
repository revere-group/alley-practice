package dev.revere.alley.feature.tournament.listener;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.feature.tournament.TournamentService;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @author Remi
 * @project alley-practice
 * @date 6/08/2025
 */
public class TournamentListener implements Listener {

    private final TournamentService tournamentService = AlleyPlugin.getInstance().getService(TournamentService.class);

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (tournamentService.getPlayerTournament(event.getPlayer()) != null) {
            tournamentService.handlePlayerDeparture(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        if (tournamentService.getPlayerTournament(event.getPlayer()) != null) {
            tournamentService.handlePlayerDeparture(event.getPlayer());
        }
    }
}