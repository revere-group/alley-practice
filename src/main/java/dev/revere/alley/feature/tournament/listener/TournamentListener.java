package dev.revere.alley.feature.tournament.listener;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.feature.tournament.TournamentService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * @author Remi
 * @project alley-practice
 * @date 6/08/2025
 */
public class TournamentListener implements Listener {
    private final TournamentService tournamentService = AlleyPlugin.getInstance().getService(TournamentService.class);
    private final ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (isInMatch(event.getPlayer().getUniqueId())) {
            return;
        }

        if (tournamentService.getPlayerTournament(event.getPlayer()) != null) {
            tournamentService.handlePlayerDeparture(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        if (isInMatch(event.getPlayer().getUniqueId())) {
            return;
        }

        if (tournamentService.getPlayerTournament(event.getPlayer()) != null) {
            tournamentService.handlePlayerDeparture(event.getPlayer());
        }
    }

    /**
     * Checks if the player is currently in a match.
     *
     * @param uuid The UUID of the player.
     * @return true if the player is in a match, false otherwise.
     */
    private boolean isInMatch(UUID uuid) {
        Profile profile = profileService.getProfile(uuid);
        return profile != null && profile.getMatch() != null;
    }
}