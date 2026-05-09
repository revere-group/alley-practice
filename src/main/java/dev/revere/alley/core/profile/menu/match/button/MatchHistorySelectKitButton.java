package dev.revere.alley.core.profile.menu.match.button;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.match.data.MatchData;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.menu.match.MatchHistoryViewMenu;
import dev.revere.alley.common.item.ItemBuilder;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @author Emmy
 * @project Alley
 * @since 04/06/2025
 */
@AllArgsConstructor
public class MatchHistorySelectKitButton extends Button {
    protected final Kit kit;

    @Override
    public ItemStack getButtonItem(Player player) {
        Profile profile = AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(player.getUniqueId());

        List<MatchData> matchDataList = profile.getProfileData().getPreviousMatches();

        int count = (int) matchDataList.stream()
                .filter(matchData -> matchData.getKit().equals(this.kit.getName()))
                .count();

        int unrankedCount = (int) matchDataList.stream()
                .filter(matchData -> matchData.getKit().equals(this.kit.getName()) && !matchData.isRanked())
                .count();

        int rankedCount = count - unrankedCount;

        return new ItemBuilder(this.kit.getIcon())
                .name("&6&l" + this.kit.getDisplayName())
                .lore(
                        " &6│ Total: &6" + count,
                        " &6│ Ranked: &6" + rankedCount,
                        " &6│ Unranked: &6" + unrankedCount,
                        "",
                        "&aClick to view!"
                )
                .durability(this.kit.getDurability())
                .hideMeta()
                .amount(count > 0 ? count : 1)
                .build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (clickType != ClickType.LEFT) return;

        new MatchHistoryViewMenu(this.kit).openMenu(player);
    }
}
