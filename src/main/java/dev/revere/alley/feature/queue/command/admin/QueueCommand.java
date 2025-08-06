package dev.revere.alley.feature.queue.command.admin;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.command.CommandSender;

/**
 * @author Emmy
 * @project Alley
 * @date 24/09/2024 - 20:51
 */
public class QueueCommand extends BaseCommand {
    @CommandData(
            name = "queue",
            isAdminOnly = true,
            inGameOnly = false,
            usage = "queue",
            description = "Main queue command"
    )
    @Override
    public void onCommand(CommandArgs command) {
        CommandSender sender = command.getSender();

        sender.sendMessage(" ");
        sender.sendMessage(CC.translate("&6&lQueue Commands Help:"));
        sender.sendMessage(CC.translate(" &6│ &6/queue force &8(&7player&8) &8(&7kit&8) &8<&7ranked&8> &7| Force a player into a queue"));
        //sender.sendMessage(CC.translate(" &6│ &6/queue remove &8(&7player&8) &7| Remove a player from queue"));
        sender.sendMessage(CC.translate(" &6│ &6/queue reload &7| Reload the queues"));
        sender.sendMessage("");
    }
}