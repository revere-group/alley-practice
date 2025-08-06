package dev.revere.alley.core.profile.data.command.ranked;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * @author Emmy
 * @project Alley
 * @since 13/03/2025
 */
public class RankedCommand extends BaseCommand {

    //TODO: Menu for managing ranked bans? reason? duration?

    @CommandData(
            name = "ranked",
            isAdminOnly = true,
            usage = "ranked",
            description = "Manage ranked allowance."
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();

        Arrays.asList(
                " ",
                "&6&lRanked Commands Help:",
                " &6│ &6/ranked ban &8(&7player&8) &7| Ban a player from ranked matches.",
                " &6│ &6/ranked unban &8(&7player&8) &7| Unban a player from ranked matches.",
                " "
        ).forEach(message -> player.sendMessage(CC.translate(message)));
    }
}