package dev.revere.alley.feature.arena.command.impl.manage;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.common.text.LoreHelper;
import dev.revere.alley.common.text.TextFormatter;
import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.arena.ArenaService;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Comparator;

/**
 * @author Emmy
 * @project Alley
 * @date 10/07/2025
 */
public class ArenaListCommand extends BaseCommand {
    @CommandData(
            name = "arena.list",
            aliases = {"arenas"},
            isAdminOnly = true,
            usage = "arena list",
            description = "Lists all arenas on the server"
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();

        ArenaService arenaService = this.plugin.getService(ArenaService.class);

        player.sendMessage("");
        player.sendMessage(CC.translate("     &6&lArena List &f(" + arenaService.getArenas().size() + "&f)"));

        if (arenaService.getArenas().isEmpty()) {
            player.sendMessage(CC.translate("      &6│ &cNo arenas available."));
            return;
        }

        arenaService.getArenas().stream()
                .sorted(Comparator.comparing(Arena::getName))
                .forEach(arena -> {
                    ComponentBuilder hover = new ComponentBuilder("")
                            .append(CC.translate("&6&lArena Info" + LoreHelper.displayEnabled(arena.isEnabled()) + "\n"))
                            .append(CC.translate(" &6│ Display Name: &6" + arena.getDisplayName() + "\n"))
                            .append(CC.translate(" &6│ Type: &6" + arena.getType().name() + "\n"))
                            .append(CC.translate(" &6│ Center: &6" + TextFormatter.formatLocation(arena.getCenter()) + "\n"))
                            .append(CC.translate(" &6│ Pos1: &6" + TextFormatter.formatLocation(arena.getPos1()) + "\n"))
                            .append(CC.translate(" &6│ Pos2: &6" + TextFormatter.formatLocation(arena.getPos2()) + "\n"));

                    ComponentBuilder message = new ComponentBuilder("      ");
                    message.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover.create()));

                    message.append("● ").color(ChatColor.WHITE.asBungee());
                    message.append(arena.getName()).color(ChatColor.GOLD.asBungee());
                    message.append(" - ").color(ChatColor.WHITE.asBungee());

                    message.append("[TP] ")
                            .color(ChatColor.GREEN.asBungee())
                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/arena teleport " + arena.getName()))
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to teleport to " + arena.getName()).color(ChatColor.GREEN.asBungee()).create()));

                    message.append("[X] ")
                            .color(ChatColor.RED.asBungee())
                            .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/arena delete " + arena.getName()))
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to delete " + arena.getName()).color(ChatColor.RED.asBungee()).create()));

                    message.append("[i]")
                            .color(ChatColor.GRAY.asBungee())
                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/arena view " + arena.getName()))
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to view more detailed info for " + arena.getName()).color(ChatColor.GRAY.asBungee()).create()));

                    player.spigot().sendMessage(message.create());
                });

        player.sendMessage("");
    }
}