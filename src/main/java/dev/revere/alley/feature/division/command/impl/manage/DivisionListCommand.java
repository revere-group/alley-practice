package dev.revere.alley.feature.division.command.impl.manage;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.division.DivisionService;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;

/**
 * @author Remi
 * @project Alley
 * @date 6/2/2024
 */
public class DivisionListCommand extends BaseCommand {
    @CommandData(
            name = "division.list",
            isAdminOnly = true,
            usage = "division list",
            description = "Sends a list of all divisions."
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        DivisionService divisionService = this.plugin.getService(DivisionService.class);

        player.sendMessage("");
        player.sendMessage(CC.translate("     &6&lDivision List &f(" + divisionService.getDivisions().size() + "&f)"));
        if (divisionService.getDivisions().isEmpty()) {
            player.sendMessage(CC.translate("      &6│ &cNo Divisions available."));
        }
        divisionService.getDivisions()
                .forEach(division -> player.sendMessage(CC.translate("      &6│ &6" + division.getDisplayName() + " &f(" + division.getTiers().get(0).getRequiredWins() + " wins)")));
        player.sendMessage("");

    }
}
