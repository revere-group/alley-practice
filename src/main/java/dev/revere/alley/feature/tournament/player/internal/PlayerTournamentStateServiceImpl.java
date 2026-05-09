package dev.revere.alley.feature.tournament.player.internal;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.enums.ProfileState;
import dev.revere.alley.feature.hotbar.HotbarService;
import dev.revere.alley.feature.tournament.model.Tournament;
import dev.revere.alley.feature.tournament.player.PlayerTournamentStateService;
import org.bukkit.entity.Player;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */

@Service(provides = PlayerTournamentStateService.class, priority = 1100)
public class PlayerTournamentStateServiceImpl implements PlayerTournamentStateService {
    private final ProfileService profileService;
    private final HotbarService hotbarService;

    public PlayerTournamentStateServiceImpl() {
        this.profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        this.hotbarService = AlleyPlugin.getInstance().getService(HotbarService.class);
    }

    @Override
    public void setPlayerTournamentState(Player player, Tournament tournament) {
        if (player == null || !player.isOnline()) return;

        Profile profile = profileService.getProfile(player.getUniqueId());
        if (profile == null) return;

        profile.setState(ProfileState.TOURNAMENT_LOBBY);
        profile.setTournament(tournament);
        hotbarService.applyHotbarItems(player);
    }

    @Override
    public void resetPlayerStateToLobby(Player player) {
        if (player == null || !player.isOnline()) return;

        Profile profile = profileService.getProfile(player.getUniqueId());
        if (profile == null) return;

        profile.setState(ProfileState.LOBBY);
        profile.setTournament(null);
        hotbarService.applyHotbarItems(player);
    }
}