package dev.revere.alley.feature.cosmetic.command.impl.admin;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.common.text.StringUtil;
import dev.revere.alley.core.locale.internal.impl.message.GlobalMessagesLocaleImpl;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.data.types.ProfileCosmeticData;
import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * @author Remi
 * @project Alley
 * @date 6/1/2024
 */
public class CosmeticGetSelectedCommand extends BaseCommand {
    @CommandData(
            name = "cosmetic.getselected",
            aliases = {"cosmetic.get"},
            isAdminOnly = true,
            usage = "cosmetic get <player>",
            description = "Get the selected cosmetics of a player."
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();

        if (args.length != 1) {
            command.sendUsage();
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(this.getString(GlobalMessagesLocaleImpl.ERROR_INVALID_PLAYER));
            return;
        }

        Profile profile = this.plugin.getService(ProfileService.class).getProfile(target.getUniqueId());
        player.sendMessage(CC.translate("     &6&lSelected Cosmetics for " + target.getName()));

        ProfileCosmeticData cosmeticData = profile.getProfileData().getCosmeticData();

        for (CosmeticType type : CosmeticType.values()) {
            String selectedName = cosmeticData.getSelected(type);

            String friendlyTypeName = StringUtil.formatEnumName(type);

            player.sendMessage(CC.translate(String.format("      &6│ &6%s: &f%s", friendlyTypeName, selectedName)));
        }
    }
}