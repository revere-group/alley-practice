package dev.revere.alley.feature.tournament.menu;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.KitService;
import dev.revere.alley.feature.tournament.TournamentService;
import dev.revere.alley.feature.tournament.model.TournamentType;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.pagination.PaginatedMenu;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * @author Remi
 * @project alley-practice
 * @date 6/08/2025
 */
@AllArgsConstructor
public class TournamentHostKitMenu extends PaginatedMenu {
    private final TournamentType tournamentType;

    @Override
    public String getPrePaginatedTitle(Player player) {
        return "&6Select a Kit";
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
        KitService kitService = AlleyPlugin.getInstance().getService(KitService.class);

        int slot = 0;
        for (Kit kit : kitService.getKits()) {
            if (kit.isEnabled()) {
                slot = this.validateSlot(slot);
                buttons.put(slot++, new KitButton(kit, tournamentType));
            }
        }

        this.addGlassToAvoidedSlots(buttons);

        return buttons;
    }

    @Override
    public int getSize() {
        return 9 * 5;
    }

    @AllArgsConstructor
    private static class KitButton extends Button {
        private final Kit kit;
        private final TournamentType type;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(kit.getIcon())
                    .name(kit.getMenuTitle())
                    .durability(kit.getDurability())
                    .hideMeta()
                    .lore(getStyledLore())
                    .hideMeta()
                    .build();
        }

        private List<String> getStyledLore() {
            List<String> lore = new ArrayList<>();
            lore.add(CC.MENU_BAR);
            if (kit.getDescription() != null && !kit.getDescription().isEmpty()) {
                lore.addAll(Arrays.asList(
                        "&7" + kit.getDescription(),
                        ""
                ));
            }
            lore.addAll(Arrays.asList(
                    "&6│ &rClick to host a " + type.getDisplayName() + " tournament",
                    "&6│ &rwith the " + kit.getDisplayName() + " kit.",
                    ""
            ));
            lore.add("&aClick to host tournament.");
            lore.add(CC.MENU_BAR);
            return lore;
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType.isLeftClick()) {
                player.closeInventory();
                TournamentService tournamentService = AlleyPlugin.getInstance().getService(TournamentService.class);
                tournamentService.hostTournament(player, type, kit);
                playSuccess(player);
            }
        }
    }
}
