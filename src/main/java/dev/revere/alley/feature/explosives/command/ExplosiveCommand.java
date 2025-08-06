package dev.revere.alley.feature.explosives.command;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.locale.LocaleService;
import dev.revere.alley.core.locale.internal.impl.message.GlobalMessagesLocaleImpl;
import dev.revere.alley.feature.explosives.ExplosiveService;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

/**
 * @author Remi
 * @project Alley
 * @since 24/06/2025
 */
public class ExplosiveCommand extends BaseCommand {
    @CommandData(
            name = "explosive",
            aliases = {"expl"},
            isAdminOnly = true,
            inGameOnly = false,
            usage = "explosive <setting> <value>",
            description = "Set various explosive settings."
    )
    @Override
    public void onCommand(CommandArgs command) {
        CommandSender sender = command.getSender();
        String[] args = command.getArgs();

        if (args.length != 2) {
            sendHelpMessage(sender);
            return;
        }

        String settingName = args[0].toLowerCase();
        String valueStr = args[1];
        double value;

        try {
            value = Double.parseDouble(valueStr);
        } catch (NumberFormatException exception) {
            sender.sendMessage(this.getString(GlobalMessagesLocaleImpl.ERROR_INVALID_NUMBER).replace("{input}", args[1]));
            return;
        }

        ExplosiveService explosiveService = this.plugin.getService(ExplosiveService.class);

        switch (settingName) {
            case "horizontal":
                explosiveService.setHorizontalFireballKnockback(value);
                break;
            case "vertical":
                explosiveService.setVerticalFireballKnockback(value);
                break;
            case "range":
                explosiveService.setFireballExplosionRange(value);
                break;
            case "speed":
                explosiveService.setFireballThrowSpeed(value);
                break;
            case "fuse":
                explosiveService.setTntFuseTicks((int) value);
                break;
            case "explosion":
                explosiveService.setTntExplosionRange(value);
                break;
            default:
                sendHelpMessage(sender);
                return;
        }

        explosiveService.save();

        LocaleService localeService = this.plugin.getService(LocaleService.class);
        sender.sendMessage(localeService.getString(GlobalMessagesLocaleImpl.EXPLOSIVE_SETTING_UPDATED)
                .replace("{setting-name}", settingName)
                .replace("{setting-value}", String.valueOf(value))
        );
    }

    private void sendHelpMessage(CommandSender sender) {
        List<String> helpMessage = Arrays.asList(
                "",
                "&6&lExplosive Commands Help:",
                " &6│ &6/explosive explosion <value> &8- &7Set range of explosion blocks removal.",
                " &6│ &6/explosive range <value> &8- &7Set explosion range that affects players.",
                " &6│ &6/explosive horizontal <value> &8- &7Set horizontal knockback.",
                " &6│ &6/explosive vertical <value> &8- &7Set vertical knockback.",
                " &6│ &6/explosive speed <value> &8- &7Set fireball launch speed.",
                " &6│ &6/explosive fuse <value> &8- &7Set TNT fuse ticks.",
                ""
        );

        helpMessage.forEach(line -> sender.sendMessage(CC.translate(line)));
    }
}