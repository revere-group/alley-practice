package dev.revere.alley.feature.match.command.player;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.locale.internal.impl.message.GlobalMessagesLocaleImpl;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.enums.ProfileState;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * @author Remi
 * @project Alley
 * @date 5/21/2024
 */
public class SpectateCommand extends BaseCommand {
    @CommandData(
            name = "spectate",
            aliases = {"spec"},
            usage = "spectate <player>",
            description = "Spectate a player."
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();

        if (args.length == 0) {
            command.sendUsage();
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(this.getString(GlobalMessagesLocaleImpl.ERROR_INVALID_PLAYER));
            return;
        }

        if (player.equals(target)) {
            player.sendMessage(CC.translate("&cYou cannot spectate yourself."));
            return;
        }

        ProfileService profileService = this.plugin.getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());
        if (profile.getState() != ProfileState.LOBBY && profile.getState() != ProfileState.TOURNAMENT_LOBBY) {
            player.sendMessage(this.getString(GlobalMessagesLocaleImpl.ERROR_YOU_MUST_BE_IN_LOBBY));
            return;
        }

        Profile targetProfile = this.plugin.getService(ProfileService.class).getProfile(target.getUniqueId());

        if (targetProfile.getFfaMatch() != null && profile.getState() == ProfileState.TOURNAMENT_LOBBY) {
            player.sendMessage(CC.translate("&cYou can not spectate whilst in a tournament.")); // todo: make these messages configurable
            return;
        }

        if (targetProfile.getFfaMatch() != null) {
            targetProfile.getFfaMatch().addSpectator(player);
            return;
        }

        if (targetProfile.getState() != ProfileState.PLAYING) {
            player.sendMessage(this.getString(GlobalMessagesLocaleImpl.ERROR_PLAYER_NOT_PLAYING_MATCH)
                    .replace("{name-color}", String.valueOf(targetProfile.getNameColor()))
                    .replace("{player}", target.getName()));
            return;
        } else if (targetProfile.getMatch() != null) {
            targetProfile.getMatch().addSpectator(player);
        } else {
            player.sendMessage(CC.translate("&cYou are unable to spectate that player."));
        }
    }
}