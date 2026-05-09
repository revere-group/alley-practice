package dev.revere.alley.feature.ffa.command.impl.admin.manage;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.ffa.FFAService;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;

/**
 * @author Remi
 * @project Alley
 * @date 5/27/2024
 */
public class FFAListCommand extends BaseCommand {
    @CommandData(
            name = "ffa.list",
            isAdminOnly = true,
            usage = "ffa list",
            description = "Sends a list of all FFA matches."
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        FFAService ffaService = this.plugin.getService(FFAService.class);

        player.sendMessage("");
        player.sendMessage(CC.translate("     &6&lFFA Match List &f(" + ffaService.getMatches().size() + "&f)"));
        if (ffaService.getMatches().isEmpty()) {
            player.sendMessage(CC.translate("      &6│ &cNo Matches available."));
        }
        ffaService.getMatches().forEach(match -> player.sendMessage(CC.translate("      &6│ &6" + match.getKit().getDisplayName() + " &f(" + (match.getPlayers().size() + "/" + match.getMaxPlayers()) + "&f)")));
        player.sendMessage("");
    }
}