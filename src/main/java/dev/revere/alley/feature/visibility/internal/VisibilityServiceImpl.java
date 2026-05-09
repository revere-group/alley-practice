package dev.revere.alley.feature.visibility.internal;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.common.logger.Logger;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.enums.ProfileState;
import dev.revere.alley.feature.match.model.internal.MatchGamePlayer;
import dev.revere.alley.feature.visibility.VisibilityService;
import org.bukkit.entity.Player;

/**
 * @author Emmy
 * @project Alley
 * @since 06/06/2025
 */
@Service(provides = VisibilityService.class, priority = 360)
public class VisibilityServiceImpl implements VisibilityService {
    private final AlleyPlugin plugin;
    private final ProfileService profileService;

    /**
     * Constructor for DI.
     */
    public VisibilityServiceImpl(AlleyPlugin plugin, ProfileService profileService) {
        this.plugin = plugin;
        this.profileService = profileService;
    }

    @Override
    public void updateVisibility(Player player) {
        if (player == null || !player.isOnline()) return;

        for (Player otherPlayer : this.plugin.getServer().getOnlinePlayers()) {
            if (player.equals(otherPlayer)) continue;
            handleVisibility(player, otherPlayer);
            handleVisibility(otherPlayer, player);
        }
    }

    /**
     * Hides a player from another player based on their profile states.
     *
     * @param viewer The profile of the viewer.
     * @param target The target player to hide.
     */
    public void handleVisibility(Player viewer, Player target) {
        if (viewer == null || target == null || !viewer.isOnline() || !target.isOnline()) {
            return;
        }

        Profile viewerProfile = this.profileService.getProfile(viewer.getUniqueId());
        Profile targetProfile = this.profileService.getProfile(target.getUniqueId());

        if (viewerProfile == null || targetProfile == null) {
            Logger.error("Could not retrieve profile for viewer or target player.");
            return;
        }

        ProfileState viewerProfileState = viewerProfile.getState();
        ProfileState targetProfileState = targetProfile.getState();

        switch (viewerProfileState) {
            case LOBBY:
            case WAITING:
                this.handleLobbyAndQueueState(viewer, target, targetProfileState);
                break;
            case FFA:
                this.handleFfaState(viewer, target, viewerProfile, targetProfile);
                break;
            case PLAYING:
                this.handlePlayingCase(viewer, target, viewerProfile, targetProfile);
                break;
            case SPECTATING:
                this.handleSpectatingCase(viewer, target, viewerProfile);
                break;
            default:
                viewer.showPlayer(target);
                break;
        }
    }

    /**
     * Handles the visibility logic for players in lobby and queue states.
     *
     * @param viewer             the player who is viewing.
     * @param target             player to be viewed.
     * @param targetProfileState the profile state of the target player.
     */
    private void handleLobbyAndQueueState(Player viewer, Player target, ProfileState targetProfileState) {
        switch (targetProfileState) {
            case LOBBY:
            case WAITING:
            case EDITING:
                viewer.showPlayer(target);
                break;
            default:
                viewer.hidePlayer(target);
                break;
        }

        //TODO: further logic once we implement /tpv command (TogglePlayerVisibility)
    }

    private void handleFfaState(Player viewer, Player target, Profile viewerProfile, Profile targetProfile) {
        if (targetProfile.getState() != ProfileState.FFA) {
            viewer.hidePlayer(target);
            return;
        }

        if (viewerProfile.getFfaMatch().getKit() == targetProfile.getFfaMatch().getKit()) {
            viewer.showPlayer(target);
        } else {
            viewer.hidePlayer(target);
        }
    }

    /**
     * Handles the visibility logic for players in playing state.
     *
     * @param viewer        The player who is viewing.
     * @param target        The target player to be viewed.
     * @param viewerProfile The profile of the viewer.
     */
    private void handlePlayingCase(Player viewer, Player target, Profile viewerProfile, Profile targetProfile) {
        if (viewerProfile.getMatch() == null) {
            viewer.hidePlayer(target);
            return;
        }

        if (targetProfile.getState() == ProfileState.SPECTATING) {
            viewer.hidePlayer(target);
            return;
        }

        if (targetProfile.getMatch() == null || !viewerProfile.getMatch().equals(targetProfile.getMatch())) {
            viewer.hidePlayer(target);
            return;
        }

        MatchGamePlayer targetGamePlayer = viewerProfile.getMatch().getFromAllGamePlayers(target);
        if (targetProfile.getMatch().getSpectators().contains(target.getUniqueId())) {
            viewer.hidePlayer(target);
            return;
        }

        if (targetGamePlayer == null) {
            viewer.hidePlayer(target);
            return;
        }

        if (targetGamePlayer.isEliminated()) {
            viewer.hidePlayer(target);
            return;
        }

        viewer.showPlayer(target);
    }


    /**
     * Handles the visibility logic for players in spectating state.
     *
     * @param viewer        The player who is viewing.
     * @param target        The target player to be viewed.
     * @param viewerProfile The profile of the viewer.
     */
    private void handleSpectatingCase(Player viewer, Player target, Profile viewerProfile) {
        Profile targetProfile = this.profileService.getProfile(target.getUniqueId());
        if (targetProfile.getState() == ProfileState.SPECTATING) {
            viewer.showPlayer(target);
            return;
        }

        if (viewerProfile.getMatch() == null) {
            viewer.hidePlayer(target);
            return;
        }

        MatchGamePlayer spectatingTargetGamePlayer = viewerProfile.getMatch().getGamePlayer(target);
        if (spectatingTargetGamePlayer == null) {
            viewer.hidePlayer(target);
            return;
        }

        viewer.showPlayer(target);
    }
}