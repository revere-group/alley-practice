package dev.revere.alley.feature.host.menu;

import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.feature.tournament.menu.TournamentHostTypeMenu;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.Menu;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Emmy
 * @project Alley
 * @date 08/06/2024 - 21:19
 */
public class HostMenu extends Menu {
    @Override
    public String getTitle(Player player) {
        return "&6&lHost Event";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        buttons.put(11, new HostButton("&6&lEvent", new ItemStack(Material.EMPTY_MAP), Arrays.asList(
                "&fHost an event with different",
                "&funique implementations.",
                "",
                " &6│ &6Types: &7Sumo",
                " &6│ &cMore soon...",
                "",
                "&aClick to host!"
        )));

        buttons.put(15, new HostButton("&6&lTournament", new ItemStack(Material.BOW), Arrays.asList(
                "&fHost a tournament to",
                "&fcompete in a number",
                "&fof duels to win.",
                "",
                "&aClick to host!"
        )));

        this.addGlass(buttons, 15);
        return buttons;
    }

    @AllArgsConstructor
    public static class HostButton extends Button {
        private String displayName;
        private ItemStack itemStack;
        private List<String> lore;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(this.itemStack)
                    .name(this.displayName)
                    .lore(this.lore)
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) {
                return;
            }

            switch (this.itemStack.getType()) {
                case BOW:
                    new TournamentHostTypeMenu().openMenu(player);
                    break;
                case EMPTY_MAP:
                    //new EventMenu().openMenu(player);
                    break;
            }

            this.playNeutral(player);
        }
    }


    @Override
    public int getSize() {
        return 9 * 3;
    }
}