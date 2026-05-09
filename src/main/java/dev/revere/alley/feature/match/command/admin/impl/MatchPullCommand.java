package dev.revere.alley.feature.match.command.admin.impl;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.locale.internal.impl.message.GlobalMessagesLocaleImpl;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.enums.ProfileState;
import dev.revere.alley.feature.match.Match;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import dev.revere.alley.library.command.annotation.CompleterData;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Remi
 * @project Alley
 */
public class MatchPullCommand extends BaseCommand {
    @CompleterData(name = "match.pull")
    public List<String> matchPullCompleter(CommandArgs command) {
        List<String> completion = new ArrayList<>();
        Player player = command.getPlayer();
        if (!player.hasPermission("alley.admin")) return completion;

        switch (command.getArgs().length) {
            case 1:
            case 2:
                for (Player onlinePlayer : player.getServer().getOnlinePlayers()) {
                    completion.add(onlinePlayer.getName());
                }
                break;
            case 3:
                completion.addAll(Arrays.asList("true", "false"));
                break;
            default:
                break;
        }
        return completion;
    }

    @CommandData(
            name = "match.pull",
            isAdminOnly = true,
            usage = "match pull <player> <target> [newTeam]",
            description = "Pull a lobby player into the target's active match"
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player sender = command.getPlayer();
        String[] args = command.getArgs();

        if (args.length < 2 || args.length > 3) {
            command.sendUsage();
            return;
        }

        Player player = sender.getServer().getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage(this.getString(GlobalMessagesLocaleImpl.ERROR_INVALID_PLAYER));
            return;
        }

        Player target = sender.getServer().getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(this.getString(GlobalMessagesLocaleImpl.ERROR_INVALID_PLAYER));
            return;
        }

        if (player.equals(target)) {
            sender.sendMessage(CC.translate("&cThe player and target cannot be the same person."));
            return;
        }

        Profile playerProfile = this.plugin.getService(ProfileService.class).getProfile(player.getUniqueId());
        if (playerProfile.getState() != ProfileState.LOBBY) {
            sender.sendMessage(CC.translate("&e" + player.getName() + " &cmust be in the lobby to be pulled into a match."));
            return;
        }

        Profile targetProfile = this.plugin.getService(ProfileService.class).getProfile(target.getUniqueId());
        Match match = targetProfile.getMatch();
        if (match == null) {
            sender.sendMessage(CC.translate("&e" + target.getName() + " &cis not currently in a match."));
            return;
        }

        boolean newTeam = args.length == 3 && Boolean.parseBoolean(args[2]);
        if (newTeam && match.rejectsNewTeamPull()) {
            sender.sendMessage(CC.translate("&cNew team pulls are only supported in FFA matches."));
            return;
        }

        boolean success = match.pullPlayerIntoMatch(player, target, newTeam);
        if (success) {
            sender.sendMessage(CC.translate("&aPulled &e" + player.getName() + " &ainto &e" + target.getName() + "&a's match" + (newTeam ? " &7(new team)" : "") + "&a."));
        } else {
            sender.sendMessage(CC.translate("&cFailed to pull &e" + player.getName() + " &cinto the match. The match may be ending or the player is already in it."));
        }
    }
}
