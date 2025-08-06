package dev.revere.alley.feature.duel.menu;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.arena.ArenaService;
import dev.revere.alley.feature.duel.DuelRequestService;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.pagination.PaginatedMenu;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Emmy
 * @project Alley
 * @date 17/10/2024 - 22:13
 */
@AllArgsConstructor
public class DuelArenaSelectorMenu extends PaginatedMenu {
    private Player targetPlayer;
    private Kit kit;

    @Override
    public String getPrePaginatedTitle(Player player) {
        return "&6&lSelect an arena";
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        this.addGlassHeader(buttons, 15);

        return buttons;
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        int slot = 0;

        for (Arena arena : AlleyPlugin.getInstance().getService(ArenaService.class).getArenas()) {
            if (arena.getKits().contains(this.kit.getName()) && arena.isEnabled()) {
                buttons.put(slot++, new DuelArenaSelectorButton(this.targetPlayer, this.kit, arena));
            }
        }

        return buttons;
    }

    @Override
    public int getSize() {
        return 9 * 6;
    }

    @AllArgsConstructor
    private static class DuelArenaSelectorButton extends Button {
        private Player targetPlayer;
        private Kit kit;
        private Arena arena;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.PAPER)
                    .name("&6&l" + this.arena.getDisplayName())
                    .lore(
                            " &6│ &6Target: &f" + this.targetPlayer.getName(),
                            " &6│ &6Kit: &f" + this.kit.getDisplayName(),
                            "",
                            "&aClick to send request!"
                    )
                    .durability(0)
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            player.closeInventory();

            AlleyPlugin.getInstance().getService(DuelRequestService.class).createAndSendRequest(player, this.targetPlayer, this.kit, this.arena);
        }
    }
}