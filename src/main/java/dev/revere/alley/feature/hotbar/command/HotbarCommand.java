package dev.revere.alley.feature.hotbar.command;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;

import java.util.Arrays;

/**
 * @author Emmy
 * @project alley-practice
 * @since 21/07/2025
 */
public class HotbarCommand extends BaseCommand {
    @Override
    public void onCommand(CommandArgs command) {
        Arrays.asList(
                "",
                "&c&l[NOTE] &fThese commands are not implemented yet.",
                "",
                "&6&lHotbar Commands Help",
                " &6│ &6/hotbar create &8(&7name&8) &8(&7type&8) &7| Create a new hotbar item.",
                " &6│ &6/hotbar delete &8(&7name&8) &7| Delete a hotbar item.",
                " &6│ &6/hotbar list &7| List all hotbar items.",
                ""
        ).forEach(line -> command.getSender().sendMessage(CC.translate(line)));

        //TODO: implement the actual commands
    }
}
