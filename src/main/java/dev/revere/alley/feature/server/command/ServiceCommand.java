package dev.revere.alley.feature.server.command;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * @author Emmy
 * @project Alley
 * @since 09/03/2025
 */
public class ServiceCommand extends BaseCommand {
    @CommandData(
            name = "service",
            isAdminOnly = true,
            usage = "service",
            description = "Service command."
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();

        Arrays.asList(
                "",
                "&6&lService Commands",
                " &6│ &6/service menu &7| &fOpens the service menu.",
                " &6│ &6/service allowqueue &8(&7true/false&8) &7| &fAllow/disallow queueing.",
                " &6│ &6/service togglecrafting &7| &fEnable/Disable crafting for an item.",
                ""
        ).forEach(line -> player.sendMessage(CC.translate(line)));
    }
}