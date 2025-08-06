package dev.revere.alley.feature.kit.command.helper;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;

import java.util.Arrays;

/**
 * @author Emmy
 * @project alley-practice
 * @since 14/07/2025
 */
public class KitHelperCommand extends BaseCommand {
    @CommandData(
            name = "kithelper",
            isAdminOnly = true,
            usage = "kithelper",
            description = "Provides assistance for essentials."
    )
    @Override
    public void onCommand(CommandArgs command) {
        Arrays.asList(
                "",
                "&6&lKit Helper Commands Help:",
                " &6│ &6/enchant &8(&7enchantment&8) &8(&7level&8) &7| &fEnchant item in hand.",
                " &6│ &6/glow &8(&7true|false&8) &7| &fSet item glow.",
                " &6│ &6/potionduration &8(&7duration&8) &7| &fSet duration of a potion.",
                " &6│ &6/removeenchants &7| &fRemoves enchants from item.",
                " &6│ &6/rename &8(&7name&8) &7| &fRename item in hand.",
                " &6│ &6/unbreakable &8(&7true|false&8) &7| &fSet item unbreakable.",
                ""
        ).forEach(message -> command.getSender().sendMessage(CC.translate(message)));
    }
}