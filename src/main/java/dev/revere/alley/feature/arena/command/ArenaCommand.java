package dev.revere.alley.feature.arena.command;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.common.text.ClickableUtil;
import dev.revere.alley.core.locale.internal.impl.message.GlobalMessagesLocaleImpl;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import dev.revere.alley.library.command.annotation.CompleterData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Emmy
 * @project Alley
 * @date 02/05/2024 - 19:02
 */
public class ArenaCommand extends BaseCommand {
    @CompleterData(name = "arena")
    public List<String> arenaCompleter(CommandArgs command) {
        List<String> completion = new ArrayList<>();

        if (command.getArgs().length == 1 && command.getPlayer().hasPermission(this.getAdminPermission())) {
            completion.addAll(Arrays.asList(
                    "create", "delete", "list", "kitlist", "setcuboid", "setcenter",
                    "setspawn", "removekit", "addkit", "teleport", "toggle", "tool",
                    "setdisplayname", "setheightlimit", "setvoidlevel", "setportal",
                    "view", "test", "paste", "saveall", "save"
            ));
        }

        return completion;
    }

    @CommandData(
            name = "arena",
            aliases = "arena.help",
            isAdminOnly = true,
            inGameOnly = false,
            usage = "arena help <page>",
            description = "Displays a list of arena commands"
    )
    @Override
    public void onCommand(CommandArgs command) {
        CommandSender sender = command.getSender();
        String[] args = command.getArgs();
        int page = 1;

        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(this.getString(GlobalMessagesLocaleImpl.ERROR_INVALID_PAGE_NUMBER).replace("{input}", args[0]));
            }
        }

        if (page > pages.length || page < 1) {
            sender.sendMessage(this.getString(GlobalMessagesLocaleImpl.ERROR_NO_MORE_PAGES_AVAILABLE)
                    .replace("{input}", String.valueOf(page))
                    .replace("{max-pages}", String.valueOf(pages.length))
            );
            return;
        }

        sender.sendMessage("");
        sender.sendMessage(CC.translate("&6&lArena Commands &8(&7Page &f" + page + "&7/&f" + pages.length + "&8)"));
        for (String string : pages[page - 1]) {
            sender.sendMessage(CC.translate(string));
        }
        sender.sendMessage("");

        if (sender instanceof Player) {
            Player player = (Player) sender;
            ClickableUtil.sendPageNavigation(player, page, pages.length, "/arena", false, true);
        }
    }

    private final String[][] pages = {
            {
                    " &6│ &6/arena list &7| List all arenas",
                    " &6│ &6/arena create &8(&7arenaName&8) &7| Create an arena",
                    " &6│ &6/arena delete &8(&7arenaName&8) &7| Delete an arena",
                    " &6│ &6/arena toggle &8(&7arenaName&8) &7| Enable or Disable an Arena",
                    " &6│ &6/arena view &8(&7arenaName&8) &7| View arena information",
                    " &6│ &6/arena teleport &8(&7arenaName&8) &7| Teleport to an arena",
                    " &6│ &6/arena tool &7| Get the Arena Selection tool"
            },
            {
                    " &6│ &6/arena setdisplayname &8(&7arenaName&8) &8(&7displayname&8) &7| Set display-name of an arena",
                    " &6│ &6/arena setcenter &8(&7arenaName&8) &7| Set center position",
                    " &6│ &6/arena setcuboid &8(&7arenaName&8) &7| Set min and max position",
                    " &6│ &6/arena setspawn &8(&7arenaName&8) &8<&7pos1/pos2&8> &7| Set spawn positions",
                    " &6│ &6/arena setportal &8(&7arenaName&8) &8<&71/2&8> &7| Set portal positions",
                    " &6│ &6/arena setheightlimit &8(&7arenaName&8) &7| Set height limit for standalone arenas",
                    " &6│ &6/arena setvoidlevel &8(&7arenaName&8) &7| Set void level for standalone arenas"
            },
            {
                    " &6│ &6/arena kitlist &8(&7arenaName&8) &7| List all kits for an arena",
                    " &6│ &6/arena addkit &8(&7arenaName&8) &8(&7kitName&8) &7| Add a kit to an arena",
                    " &6│ &6/arena removekit &8(&7arenaName&8) &8(&7kitName&8) &7| Remove arena kit"
            },
            {
                    " &f│ &6/arena save &8(&7arenaName&8) &7| Save an arena",
                    " &6│ &6/arena saveall &7| Save all arenas"
            },
    };
}