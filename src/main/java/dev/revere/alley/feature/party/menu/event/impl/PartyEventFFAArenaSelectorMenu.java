package dev.revere.alley.feature.party.menu.event.impl;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.arena.ArenaService;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.party.menu.event.impl.button.PartyEventFFAArenaSelectorButton;
import dev.revere.alley.feature.party.menu.event.impl.button.PartyEventSplitArenaSelectorButton;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.pagination.PaginatedMenu;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Emmy
 * @project Alley
 * @date 09/11/2024 - 09:37
 */
@AllArgsConstructor
public class PartyEventFFAArenaSelectorMenu extends PaginatedMenu {
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

        for (Arena arena : AlleyPlugin.getInstance().getService(ArenaService.class).getArenas()) {
            if (arena.getKits().contains(kit.getName()) && arena.isEnabled()) {
                buttons.put(buttons.size(), new PartyEventFFAArenaSelectorButton(kit, arena));
            }
        }

        return buttons;
    }

    @Override
    public int getSize() {
        return 9 * 6;
    }
}