package dev.revere.alley.core.profile.menu.match.button;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.constants.MessageConstant;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.time.DateFormat;
import dev.revere.alley.common.time.DateFormatter;
import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.arena.ArenaService;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.KitService;
import dev.revere.alley.feature.match.data.MatchData;
import dev.revere.alley.feature.match.data.types.MatchDataSolo;
import dev.revere.alley.library.menu.Button;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * @author Emmy
 * @project Alley
 * @since 04/06/2025
 */
@AllArgsConstructor
public class MatchHistoryViewButton extends Button {
    protected final MatchData matchData;

    @Override
    public ItemStack getButtonItem(Player player) {
        if (this.matchData instanceof MatchDataSolo) {
            MatchDataSolo matchDataSolo = (MatchDataSolo) this.matchData;

            UUID winnerUUID = matchDataSolo.getWinner();
            UUID loserUUID = matchDataSolo.getLoser();

            String winnerName = Bukkit.getOfflinePlayer(winnerUUID).getName();
            String loserName = Bukkit.getOfflinePlayer(loserUUID).getName();

            DateFormatter dateFormatter = new DateFormatter(DateFormat.DATE_PLUS_TIME, matchDataSolo.getCreationTime());
            String date = dateFormatter.getDateFormat().format(dateFormatter.getDate());

            String rankedStatus = matchDataSolo.isRanked() ? "&6Ranked" : "&9Unranked";

            Kit kit = AlleyPlugin.getInstance().getService(KitService.class).getKit(matchDataSolo.getKit());
            Arena arena = AlleyPlugin.getInstance().getService(ArenaService.class).getArenaByName(matchDataSolo.getArena());

            return new ItemBuilder(Material.SKULL_ITEM)
                    .name("&a&l" + winnerName + " &7vs &c&l" + loserName + " &7(" + rankedStatus + "&7)")
                    .setSkull(winnerName)
                    .lore(
                            "&7" + date,
                            "",
                            "&6&lParticipants",
                            " &6│ Winner: &a" + winnerName,
                            " &6│ Loser: &c" + loserName,
                            "",
                            "&6&lMatch Details",
                            " &6│ Kit: &6" + kit.getDisplayName(),
                            " &6│ Arena: &6" + arena.getDisplayName(),
                            "",
                            "&aClick to view more details!"
                    )
                    .hideMeta()
                    .durability(3)
                    .build();
        } else {
            return new ItemBuilder(Material.BARRIER)
                    .name("&c&lNot implemented")
                    .lore(
                            "&fThis is not implemented for team matches yet."
                    )
                    .hideMeta()
                    .build();
        }
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (clickType != ClickType.LEFT) return;

        player.sendMessage(MessageConstant.IN_DEVELOPMENT);
        player.closeInventory();
    }
}