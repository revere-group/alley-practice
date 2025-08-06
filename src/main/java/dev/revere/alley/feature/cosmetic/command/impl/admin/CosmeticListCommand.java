package dev.revere.alley.feature.cosmetic.command.impl.admin;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.common.text.StringUtil;
import dev.revere.alley.core.locale.internal.impl.message.GlobalMessagesLocaleImpl;
import dev.revere.alley.feature.cosmetic.CosmeticService;
import dev.revere.alley.feature.cosmetic.internal.repository.BaseCosmeticRepository;
import dev.revere.alley.feature.cosmetic.model.Cosmetic;
import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

/**
 * @author Remi
 * @project Alley
 * @date 6/1/2024
 */
public class CosmeticListCommand extends BaseCommand {
    @CommandData(
            name = "cosmetic.list",
            isAdminOnly = true,
            usage = "cosmetic list",
            description = "List all registered cosmetics."
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        Map<CosmeticType, BaseCosmeticRepository<?>> repositories = this.plugin.getService(CosmeticService.class).getRepositories();

        player.sendMessage("");

        if (repositories.isEmpty()) {
            player.sendMessage(this.getString(GlobalMessagesLocaleImpl.COSMETICS_NONE_REGISTERED));
            player.sendMessage("");
            return;
        }

        for (Map.Entry<CosmeticType, BaseCosmeticRepository<?>> entry : repositories.entrySet()) {
            CosmeticType type = entry.getKey();
            BaseCosmeticRepository<?> repository = entry.getValue();

            List<? extends Cosmetic> cosmetics = repository.getCosmetics();
            if (cosmetics.isEmpty()) {
                continue;
            }

            String friendlyTypeName = StringUtil.formatEnumName(type);
            String header = String.format("     &6&l%s &f(%d)", friendlyTypeName, cosmetics.size());
            player.sendMessage(CC.translate(header));

            for (Cosmetic cosmetic : cosmetics) {
                player.sendMessage(CC.translate("      &6│ &6" + cosmetic.getName()));
            }
        }

        player.sendMessage("");
    }
}