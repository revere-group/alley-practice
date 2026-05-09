package dev.revere.alley.feature.command.impl.main.impl;

import dev.revere.alley.adapter.core.Core;
import dev.revere.alley.adapter.core.CoreAdapter;
import dev.revere.alley.adapter.core.CoreType;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import dev.revere.alley.common.text.CC;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

/**
 * @author Emmy
 * @project Alley
 * @since 26/04/2025
 */
public class AlleyCoreCommand extends BaseCommand {
    @CommandData(
            name = "alley.core",
            isAdminOnly = true,
            inGameOnly = false,
            usage = "alley core",
            description = "Displays information about the core hook."
    )
    @Override
    public void onCommand(CommandArgs command) {
        CommandSender sender = command.getSender();
        Core core = this.plugin.getService(CoreAdapter.class).getCore();

        Arrays.asList(
                "",
                "&6&lCore Hook Information",
                " &6&l│ &rPlugin: &6" + core.getType().getPluginName(),
                //" &6&l│ &rVersion: &6" + core.getType().getPluginVersion(),
                " &6&l│ &rAuthors: &6" + core.getType().getPluginAuthor(),
                ""
        ).forEach(line -> sender.sendMessage(CC.translate(line)));

        if (core.getType() == CoreType.DEFAULT) {
            sender.sendMessage(CC.translate("&7Note: This is the default server implementation, as there was no core found to hook into."));
        }
    }
}
