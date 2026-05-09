package dev.revere.alley.feature.division.command;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.division.menu.DivisionsMenu;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;

/**
 * @author Remi
 * @project Alley
 * @date 6/2/2024
 */
public class DivisionCommand extends BaseCommand {
    @CommandData(
            name = "division",
            usage = "division",
            description = "Sends a list of commands for managing divisions"
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();

        //TODO: make page based

        if (player.hasPermission("alley.admin")) {
            player.sendMessage(" ");
            player.sendMessage(CC.translate("&6&lDivision Commands Help:"));
            player.sendMessage(CC.translate(" &6│ &6/division create &8(&7divisionName&8) &8(&7requiredWins&8) &7| Create a division"));
            player.sendMessage(CC.translate(" &6│ &6/division delete &8(&7divisionName&8) &7| Delete a division"));
            player.sendMessage(CC.translate(" &6│ &6/division view &8(&7divisionName&8) &7| View division info"));
            player.sendMessage(CC.translate(" &6│ &6/division setwins &8(&7divisionName&8) &8(&7requiredWins&8) &8(&7tier&8) &7| Set required wins of a tier"));
            player.sendMessage(CC.translate(" &6│ &6/division seticon &8(&7divisionName&8) &7| Set division icon"));
            player.sendMessage(CC.translate(" &6│ &6/division setdisplayname &8(&7divisionName&8) &8(&7displayName&8) &7| Set division display name"));
            player.sendMessage(CC.translate(" &6│ &6/division setdescription &8(&7divisionName&8) &8(&7description&8) &7| Set division description"));
            player.sendMessage(CC.translate(" &6│ &6/division menu &7| Open the division menu"));
            player.sendMessage(CC.translate(" &6│ &6/division list &7| View all divisions"));
            player.sendMessage("");
            return;
        }

        new DivisionsMenu().openMenu(player);
    }
}
