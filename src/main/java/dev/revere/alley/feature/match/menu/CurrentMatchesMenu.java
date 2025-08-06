package dev.revere.alley.feature.match.menu;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.locale.LocaleService;
import dev.revere.alley.core.locale.internal.impl.message.GlobalMessagesLocaleImpl;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.feature.match.Match;
import dev.revere.alley.feature.match.MatchService;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.pagination.PaginatedMenu;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Remi
 * @project Alley
 * @date 5/26/2024
 */
public class CurrentMatchesMenu extends PaginatedMenu {
    /**
     * Gets the title of the menu.
     *
     * @param player the player viewing the menu
     * @return the title of the menu
     */
    @Override
    public String getPrePaginatedTitle(Player player) {
        return "&6&lCurrent Matches (" + AlleyPlugin.getInstance().getService(MatchService.class).getMatches().size() + ")";
    }

    /**
     * Gets the buttons to display in the menu.
     *
     * @param player the player viewing the menu
     * @return the buttons to display
     */
    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        final Map<Integer, Button> buttons = new ConcurrentHashMap<>();
        int slot = 0;

        for (Match match : AlleyPlugin.getInstance().getService(MatchService.class).getMatches()) {
            buttons.put(slot++, new CurrentMatchButton(match));
        }

        return buttons;
    }

    /**
     * Gets the buttons to display in the global section of the menu.
     *
     * @param player the player viewing the menu
     * @return the global buttons
     */
    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        final Map<Integer, Button> buttons = new HashMap<>();

        this.addGlassHeader(buttons, 15);

        buttons.put(4, new RefreshButton());
        return buttons;
    }

    @RequiredArgsConstructor
    public static class CurrentMatchButton extends Button {
        private final Match match;

        /**
         * Gets the item stack for the button.
         *
         * @param player the player viewing the button
         * @return the item stack
         */
        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(match.getKit().getIcon()).name("&6&l" + match.getParticipants().get(0).getLeader().getUsername() + " &7vs &6&l" + match.getParticipants().get(1).getLeader().getUsername()).durability(match.getKit().getDurability()).hideMeta()
                    .lore(
                            CC.MENU_BAR,
                            " &6│ &6Arena: &f" + match.getArena().getName(),
                            " &6│ &6Kit: &f" + match.getKit().getDisplayName(),
                            " &6│ &6Queue: &f" + (match.getQueue() == null ? "None" : match.getQueue().getQueueType()),
                            " ",
                            "&aClick to spectate.",
                            CC.MENU_BAR
                    )
                    .hideMeta().build();
        }

        /**
         * Handles the click event for the button.
         *
         * @param player       the player who clicked the button
         * @param slot         the slot the button was clicked in
         * @param clickType    the type of click
         * @param hotbarButton the hotbar button clicked
         */
        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
            if (clickType != ClickType.LEFT) return;

            if (AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(player.getUniqueId()).getMatch() != null) {
                player.sendMessage(AlleyPlugin.getInstance().getService(LocaleService.class).getString(GlobalMessagesLocaleImpl.ERROR_YOU_ALREADY_SPECTATING_MATCH));
                return;
            }

            match.addSpectator(player);
            this.playNeutral(player);
        }
    }

    @AllArgsConstructor
    public static class RefreshButton extends Button {
        /**
         * Gets the item stack for the button.
         *
         * @param player the player viewing the button
         * @return the item stack
         */
        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.CARPET)
                    .name("&6&lRefresh")
                    .lore(" &6│ &6Press to refresh the matches")
                    .durability(2)
                    .build();
        }

        /**
         * Handles the click event for the button.
         *
         * @param player       the player who clicked the button
         * @param slot         the slot the button was clicked in
         * @param clickType    the type of click
         * @param hotbarButton the hotbar button clicked
         */
        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
            if (clickType != ClickType.LEFT) return;
            new CurrentMatchesMenu().openMenu(player);
            this.playNeutral(player);
        }
    }
}
