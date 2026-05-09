package dev.revere.alley.feature.match.command.admin.impl;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.locale.internal.impl.message.GlobalMessagesLocaleImpl;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.feature.match.Match;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;

public class MatchReviveCommand extends BaseCommand {
    @CommandData(name = "match.revive", isAdminOnly = true, description = "Revive a player in your current match.", usage = "match revive <player> <silent>")
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();

        if (args.length != 2) {
            command.sendUsage();
            return;
        }

        Player target = player.getServer().getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(this.getString(GlobalMessagesLocaleImpl.ERROR_INVALID_PLAYER));
            return;
        }

        Profile profile = this.plugin.getService(ProfileService.class).getProfile(target.getUniqueId());

        Match match = profile.getMatch();
        if (match == null) {
            player.sendMessage(this.getString(GlobalMessagesLocaleImpl.ERROR_PLAYER_NOT_PLAYING_MATCH)
                    .replace("{name-color}", String.valueOf(profile.getNameColor()))
                    .replace("{player}", target.getName()));
            return;
        }

        boolean silent = Boolean.parseBoolean(args[1]);
        match.revivePlayer(target, silent);

        player.sendMessage(CC.translate("&aRevived &e" + target.getName() + "&a."));
    }
}
