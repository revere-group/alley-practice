package dev.revere.alley.feature.kit.command.impl.settings;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.kit.setting.KitSettingService;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;

/**
 * @author Remi
 * @project Alley
 * @date 5/26/2024
 */
public class KitSettingsCommand extends BaseCommand {
    @CommandData(
            name = "kit.settings",
            isAdminOnly = true,
            usage = "kit settings",
            description = "List all available kit settings."
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        KitSettingService kitSettingService = this.plugin.getService(KitSettingService.class);

        player.sendMessage("");
        player.sendMessage(CC.translate("&6&lKit Settings List &f(" + kitSettingService.getSettings().size() + "&f)"));
        if (kitSettingService.getSettings().isEmpty()) {
            player.sendMessage(CC.translate(" &6│ &cNo Kit Settings available."));
        }
        kitSettingService.getSettings().forEach(setting -> player.sendMessage(CC.translate(" &6│ &6" + setting.getName() + " &8(&7" + setting.getDescription() + "&7)")));
        player.sendMessage("");
    }
}