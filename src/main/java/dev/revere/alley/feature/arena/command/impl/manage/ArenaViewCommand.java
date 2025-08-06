package dev.revere.alley.feature.arena.command.impl.manage;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.locale.internal.impl.message.GlobalMessagesLocaleImpl;
import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.arena.ArenaService;
import dev.revere.alley.feature.arena.internal.types.FreeForAllArena;
import dev.revere.alley.feature.arena.internal.types.StandAloneArena;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.Locale;
import java.util.Objects;

/**
 * @author Emmy
 * @project Alley
 * @date 24/09/2024 - 18:29
 */
public class ArenaViewCommand extends BaseCommand {
    @CommandData(
            name = "arena.view",
            isAdminOnly = true,
            inGameOnly = false,
            usage = "arena view <arenaName>",
            description = "View detailed information about an arena"
    )
    @Override
    public void onCommand(CommandArgs command) {
        CommandSender sender = command.getSender();
        String[] args = command.getArgs();

        if (args.length < 1) {
            command.sendUsage();
            return;
        }

        ArenaService arenaService = this.plugin.getService(ArenaService.class);

        Arena arena = arenaService.getArenaByName(args[0]);
        if (arena == null) {
            sender.sendMessage(this.getString(GlobalMessagesLocaleImpl.ARENA_NOT_FOUND).replace("{arena-name}", args[0]));
            return;
        }

        //TODO: add remaining arena details

        sender.sendMessage("");
        sender.sendMessage(CC.translate("&6&lArena " + arena.getName() + " &f(" + (arena.isEnabled() ? "&aEnabled" : "&cDisabled") + "&f)"));
        sender.sendMessage(CC.translate(" &6│ &6Display Name: &f" + arena.getDisplayName()));
        sender.sendMessage(CC.translate(" &6│ &6Name: &f" + arena.getName()));
        sender.sendMessage(CC.translate(" &6│ &6Type: &f" + arena.getType()));
        sender.sendMessage(CC.translate("    &6&lData: &f"));
        sender.sendMessage(CC.translate("    &f• &6Center: &f" + (arena.getCenter() != null ? arena.getCenter().getX() + ", " + arena.getCenter().getY() + ", " + arena.getCenter().getZ() + ", &7" + arena.getCenter().getPitch() + ", " + arena.getCenter().getYaw() + " &7[" + arena.getCenter().getWorld().getName() + "]" : "&cNull")));
        sender.sendMessage(CC.translate("    &f• &6Pos1: &f" + (arena.getPos1() != null ? arena.getPos1().getX() + ", " + arena.getPos1().getY() + ", " + arena.getPos1().getZ() + ", &7" + arena.getPos1().getPitch() + ", " + arena.getPos1().getYaw() + " &7[" + arena.getPos1().getWorld().getName() + "]" : "&cNull")));
        sender.sendMessage(CC.translate("    &f• &6Pos2: &f" + (arena.getPos2() != null ? arena.getPos2().getX() + ", " + arena.getPos2().getY() + ", " + arena.getPos2().getZ() + ", &7" + arena.getPos2().getPitch() + ", " + arena.getPos2().getYaw() + " &7[" + arena.getPos2().getWorld().getName() + "]" : "&cNull")));
        sender.sendMessage(CC.translate(" &6&l│ &rDisplay Name: &6" + arena.getDisplayName()));
        sender.sendMessage(CC.translate(" &6&l│ &rType: &6" + arena.getType()));
        sender.sendMessage(CC.translate(" &6&l│ &rKits: &6" + (arena.getKits().isEmpty() ? "None" : String.join(", ", arena.getKits()))));
        if (arena instanceof FreeForAllArena) {
            FreeForAllArena ffaArena = (FreeForAllArena) arena;
            sender.sendMessage(CC.translate(" &6&l│ &rSafe Zones:"));
            sender.sendMessage(CC.translate("   &6&l◆ &fPos1 &7(Minimum)&f: &6" + (ffaArena.getMinimum() != null ? this.formatBlockLocation(ffaArena.getMinimum()) : "Not Set")));
            sender.sendMessage(CC.translate("   &6&l◆ &fPos2 &7(Maximum)&f: &6" + (ffaArena.getMaximum() != null ? this.formatBlockLocation(ffaArena.getMaximum()) : "Not Set")));
            sender.sendMessage(CC.translate(" &6&l│ &rPositions:"));
            sender.sendMessage(CC.translate("   &6&l◆ &fCenter &7(Spectator)&f: &6" + (ffaArena.getCenter() != null ? formatFullLocation(ffaArena.getCenter()) : "&cNull")));
            sender.sendMessage(CC.translate("   &6&l◆ &fPos1: &6" + (ffaArena.getPos1() != null ? this.formatFullLocation(ffaArena.getPos1()) : "&cNull")));
        } else {
            sender.sendMessage(CC.translate(" &6&l│ &rMinimum: &6" + (arena.getMinimum() != null ? this.formatBlockLocation(arena.getMinimum()) : "Not Set")));
            sender.sendMessage(CC.translate(" &6&l│ &rMaximum: &6" + (arena.getMaximum() != null ? this.formatBlockLocation(arena.getMaximum()) : "Not Set")));
            sender.sendMessage(CC.translate(" &6&l│ &rPositions:"));
            sender.sendMessage(CC.translate("   &6&l◆ &fCenter &7(Spectator)&f: &6" + (arena.getCenter() != null ? this.formatFullLocation(arena.getCenter()) : "&cNull")));
            sender.sendMessage(CC.translate("   &6&l◆ &fBlue: &6" + (arena.getPos1() != null ? this.formatFullLocation(arena.getPos1()) : "&cNull")));
            sender.sendMessage(CC.translate("   &6&l◆ &fRed: &6" + (arena.getPos2() != null ? this.formatFullLocation(arena.getPos2()) : "&cNull")));
        }

        if (arena instanceof StandAloneArena) {
            StandAloneArena standaloneArena = (StandAloneArena) arena;
            sender.sendMessage(CC.translate(" &6&l│ &rTeam Portals:"));
            sender.sendMessage(CC.translate("   &6&l◆ &fBlue: &6" + (standaloneArena.getTeam1Portal() != null ? this.formatBlockLocation(standaloneArena.getTeam1Portal()) : "&cNull")));
            sender.sendMessage(CC.translate("   &6&l◆ &fRed: &6" + (standaloneArena.getTeam2Portal() != null ? this.formatBlockLocation(standaloneArena.getTeam2Portal()) : "&cNull")));
            sender.sendMessage(CC.translate(" &6&l│ &rPortal Radius: &6" + standaloneArena.getPortalRadius()));
            sender.sendMessage(CC.translate(" &6&l│ &rHeight Limit: &6" + standaloneArena.getHeightLimit()));
            sender.sendMessage(CC.translate(" &6&l│ &rVoid Level: &6" + standaloneArena.getVoidLevel()));
        }

        sender.sendMessage("");
    }

    private String formatBlockLocation(Location loc) {
        if (loc == null) return "Not Set";

        return String.format(Locale.ENGLISH, "%.1f, %.1f, %.1f &7[%s]",
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                Objects.requireNonNull(loc.getWorld()).getName()
        );
    }

    private String formatFullLocation(Location loc) {
        if (loc == null) return "&cNull";

        return String.format(Locale.ENGLISH, "%.1f, %.1f, %.1f, &7%.0f, %.0f &7[%s]",
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                loc.getPitch(),
                loc.getYaw(),
                Objects.requireNonNull(loc.getWorld()).getName()
        );
    }
}