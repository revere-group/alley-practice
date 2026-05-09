package dev.revere.alley.feature.division.command.impl.manage;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.locale.internal.impl.message.GlobalMessagesLocaleImpl;
import dev.revere.alley.feature.division.Division;
import dev.revere.alley.feature.division.DivisionService;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * @author Emmy
 * @project Alley
 * @since 26/01/2025
 */
public class DivisionViewCommand extends BaseCommand {
    @CommandData(
            name = "division.view",
            isAdminOnly = true,
            usage = "division view <name>",
            description = "View information about a division."
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();

        if (args.length < 1) {
            command.sendUsage();
            return;
        }

        DivisionService divisionService = this.plugin.getService(DivisionService.class);
        Division division = divisionService.getDivision(args[0]);
        if (division == null) {
            player.sendMessage(this.getString(GlobalMessagesLocaleImpl.DIVISION_NOT_FOUND).replace("{division-name}", args[0]));
            return;
        }

        Arrays.asList(
                "",
                "&6&lDivision &f(" + division.getDisplayName() + ")",
                " &6│ &6Name: &f" + division.getDisplayName(),
                " &6│ &6Tiers: &f" + division.getTiers().size(),
                " &6│ &6Description: &f" + division.getDescription(),
                " &6│ &6Required Wins: &f" + division.getTiers().get(0).getRequiredWins(),
                ""
        ).forEach(line -> player.sendMessage(CC.translate(line)));
    }
}