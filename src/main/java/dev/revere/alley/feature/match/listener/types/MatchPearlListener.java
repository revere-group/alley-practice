package dev.revere.alley.feature.match.listener.types;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.InventoryUtil;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.config.ConfigService;
import dev.revere.alley.core.locale.LocaleService;
import dev.revere.alley.core.locale.internal.impl.message.GlobalMessagesLocaleImpl;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.enums.ProfileState;
import dev.revere.alley.feature.cooldown.Cooldown;
import dev.revere.alley.feature.cooldown.CooldownService;
import dev.revere.alley.feature.cooldown.CooldownType;
import dev.revere.alley.feature.kit.setting.types.mechanic.KitSettingPearlCooldownImpl;
import dev.revere.alley.feature.kit.setting.types.mode.KitSettingRaiding;
import dev.revere.alley.feature.match.MatchState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Gate;
import org.bukkit.material.Stairs;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Optional;

public class MatchPearlListener implements Listener {

    //TODO: Locale (im not touching pearls, thats on you mr insurant)

    @EventHandler
    public void onPearlLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof EnderPearl) || !(event.getEntity().getShooter() instanceof Player)) {
            return;
        }

        EnderPearl enderPearl = (EnderPearl) event.getEntity();
        Player player = (Player) enderPearl.getShooter();

        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());

        if (!isValidGameState(player, event)) {
            return;
        }

        if (hasPearlCooldown(player, event)) {
            return;
        }

        if (!profile.getMatch().getKit().isSettingEnabled(KitSettingRaiding.class)) {
            applyCooldown(player);
            return;
        }

        Block playerBlock = getBlockAtPlayerFeet(player);
        Block playerBlockAbove = getBlockAtPlayerHead(player);

        if (shouldCancelPearl(player, playerBlock, playerBlockAbove, event)) {
            return;
        }

        setPearlMetadata(enderPearl, player, playerBlock, playerBlockAbove);
    }

    @EventHandler
    public void onPearlLand(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof EnderPearl) || !(event.getEntity().getShooter() instanceof Player)) {
            return;
        }

        EnderPearl enderPearl = (EnderPearl) event.getEntity();
        Player player = (Player) enderPearl.getShooter();
        Location landLocation = enderPearl.getLocation().clone();

        if (enderPearl.hasMetadata("cpearl") || enderPearl.hasMetadata("tali")) {
            Location teleportLocation = getTeleportLocation(landLocation, player);
            if (teleportLocation != null) {
                player.teleport(teleportLocation);
            }
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            return;
        }

        Player player = event.getPlayer();
        Location destination = event.getTo().clone();

        if (shouldCancelTeleport(destination, player, event)) {
            return;
        }

        adjustTeleportLocation(destination, event);
        applyCooldown(player);
    }

    private boolean isValidGameState(Player player, ProjectileLaunchEvent event) {
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());

        if (profile.getState() != ProfileState.PLAYING) {
            return false;
        }

        if (profile.getMatch().getState() != MatchState.RUNNING) {
            cancelPearlAndRefund(player, event, "&cYou cannot use ender pearls right now.");
            return false;
        }

        if (profile.getMatch().getKit().isSettingEnabled(KitSettingPearlCooldownImpl.class)) {
            return false;
        }

        return true;
    }

    private boolean hasPearlCooldown(Player player, ProjectileLaunchEvent event) {
        CooldownService cooldownService = AlleyPlugin.getInstance().getService(CooldownService.class);
        Optional<Cooldown> optionalCooldown = Optional.ofNullable(
                cooldownService.getCooldown(player.getUniqueId(), CooldownType.ENDER_PEARL)
        );

        if (optionalCooldown.isPresent() && optionalCooldown.get().isActive()) {
            cancelPearlAndRefund(player, event,
                    "&cYou must wait " + optionalCooldown.get().remainingTime() + " seconds before using another ender pearl.");
            return true;
        }

        return false;
    }

    private boolean shouldCancelPearl(Player player, Block playerBlock, Block playerBlockAbove, ProjectileLaunchEvent event) {
        String playerDirection = getPlayerDirection(player);

        // Check fence gate restrictions
        if (isFenceGateRestricted(playerBlock, playerDirection)) {
            sendInvalidPearlFeedback(player, event);
            return true;
        }

        // Check chest restrictions
        if (isChestRestricted(playerBlock, playerDirection)) {
            sendInvalidPearlFeedback(player, event);
            return true;
        }

        // Check slab/stair restrictions with block in front
        if (isSlabStairRestricted(playerBlock, playerBlockAbove, playerDirection)) {
            sendInvalidPearlFeedback(player, event);
            return true;
        }

        // Check stair facing restrictions
        if (isStairFacingRestricted(playerBlock, playerDirection)) {
            sendInvalidPearlFeedback(player, event);
            return true;
        }

        // Check general block in front restrictions
        if (isGeneralBlockRestricted(playerBlock, playerBlockAbove)) {
            sendInvalidPearlFeedback(player, event);
            return true;
        }

        return false;
    }

    private boolean shouldCancelTeleport(Location destination, Player player, PlayerTeleportEvent event) {
        Block destinationBlock = destination.getBlock();

        // Check various block type restrictions
        if (isForbiddenTeleportBlock(destinationBlock)) {
            sendInvalidPearlFeedback(player, event);
            return true;
        }

        // Check fence gate restrictions
        if (isFenceGateTeleportRestricted(destinationBlock, player, event)) {
            return true;
        }

        // Check disabled block types
        if (isDisabledBlockType(destinationBlock, player, event)) {
            return true;
        }

        return false;
    }

    private boolean isFenceGateRestricted(Block block, String direction) {
        if (!getConfig().getBoolean("TalibanPearl.fencegate-in-half")) {
            return false;
        }

        if (block.getType() != Material.FENCE_GATE) {
            return false;
        }

        if (block.getRelative(BlockFace.UP).getType() == Material.FENCE_GATE ||
                !block.getRelative(BlockFace.UP).getType().isSolid() ||
                !block.getRelative(BlockFace.DOWN).getType().isSolid()) {
            return false;
        }

        Gate gate = (Gate) block.getState().getData();
        BlockFace gateDirection = gate.getFacing();
        BlockFace playerDirection = BlockFace.valueOf(direction);

        return !isDirectionCompatible(gateDirection, playerDirection);
    }

    private boolean isChestRestricted(Block block, String direction) {
        if (!getConfig().getBoolean("TalibanPearl.chest")) {
            return false;
        }

        if (!isChest(block)) {
            return false;
        }

        if (!block.getRelative(BlockFace.UP).getType().isSolid() ||
                block.getRelative(BlockFace.UP).getType() == Material.FENCE_GATE) {
            return false;
        }

        BlockFace directionFace = BlockFace.valueOf(direction);
        return block.getRelative(directionFace).getType().isSolid();
    }

    private boolean isSlabStairRestricted(Block block, Block blockAbove, String direction) {
        if (!getConfig().getBoolean("TalibanPearl.Block-if-block-in-front")) {
            return false;
        }

        boolean isSlab = getConfig().getBoolean("TalibanPearl.Slabs") && isSlab(block);
        boolean isStair = getConfig().getBoolean("TalibanPearl.Stairs") && isStair(block);

        if (!isSlab && !isStair) {
            return false;
        }

        BlockFace directionFace = BlockFace.valueOf(direction);
        return blockAbove.getRelative(directionFace).getType().isSolid();
    }

    private boolean isStairFacingRestricted(Block block, String direction) {
        if (!getConfig().getBoolean("TalibanPearl.Stairs-block-if-incorrect-facing") ||
                !getConfig().getBoolean("TalibanPearl.Stairs") ||
                !isStair(block)) {
            return false;
        }

        Stairs stairs = (Stairs) block.getState().getData();
        BlockFace stairDirection = stairs.getFacing();
        BlockFace playerDirection = BlockFace.valueOf(direction);

        return isOppositeDirection(stairDirection, playerDirection);
    }

    private boolean isGeneralBlockRestricted(Block block, Block blockAbove) {
        if (!getConfig().getBoolean("invalid-if-block-in-front")) {
            return false;
        }

        return block.getType().isSolid() &&
                blockAbove.getType().isSolid() &&
                !isStair(block) &&
                !isSlab(block) &&
                !isCobbleWall(block) &&
                !isEndPortal(block) &&
                !isBed(block);
    }

    private boolean isForbiddenTeleportBlock(Block block) {
        Material type = block.getType();
        return type == Material.IRON_FENCE ||
                type == Material.NETHER_FENCE ||
                type == Material.ANVIL ||
                type == Material.THIN_GLASS ||
                type == Material.IRON_DOOR ||
                type == Material.WOOD_DOOR ||
                type == Material.WOODEN_DOOR ||
                type == Material.FENCE ||
                type == Material.STAINED_GLASS_PANE;
    }

    private boolean isFenceGateTeleportRestricted(Block block, Player player, PlayerTeleportEvent event) {
        Block upper = block.getRelative(BlockFace.UP);
        Block lower = block.getRelative(BlockFace.DOWN);

        // Case 1: Both upper and current are fence gates, upper is closed
        if (upper.getType() == Material.FENCE_GATE &&
                block.getType() == Material.FENCE_GATE &&
                !((Gate) upper.getState().getData()).isOpen() &&
                !((Gate) block.getState().getData()).isOpen() &&
                lower.getType().isSolid()) {
            sendInvalidPearlFeedback(player, event);
            return true;
        }

        // Case 2: Current is fence gate (closed), lower is fence gate (closed), upper is not fence gate
        if (upper.getType() != Material.FENCE_GATE &&
                block.getType() == Material.FENCE_GATE &&
                !((Gate) block.getState().getData()).isOpen() &&
                lower.getType() == Material.FENCE_GATE &&
                !((Gate) lower.getState().getData()).isOpen()) {
            sendInvalidPearlFeedback(player, event);
            return true;
        }

        return false;
    }

    private boolean isDisabledBlockType(Block block, Player player, PlayerTeleportEvent event) {
        // Check chest
        if (!getConfig().getBoolean("TalibanPearl.chest") && isChest(block) &&
                block.getRelative(BlockFace.UP).getType().isSolid() &&
                block.getRelative(BlockFace.UP).getType() != Material.FENCE_GATE) {
            sendInvalidPearlFeedback(player, event);
            return true;
        }

        // Check end portal
        if (!getConfig().getBoolean("TalibanPearl.endportal") && isEndPortal(block) &&
                block.getRelative(BlockFace.UP).getType().isSolid()) {
            sendInvalidPearlFeedback(player, event);
            return true;
        }

        // Check bed
        if (!getConfig().getBoolean("TalibanPearl.bed") && isBed(block) &&
                block.getRelative(BlockFace.UP).getType().isSolid()) {
            sendInvalidPearlFeedback(player, event);
            return true;
        }

        // Check cobble wall
        if (!getConfig().getBoolean("TalibanPearl.cobblewall") && isCobbleWall(block)) {
            sendInvalidPearlFeedback(player, event);
            return true;
        }

        return false;
    }

    private void setPearlMetadata(EnderPearl pearl, Player player, Block block, Block blockAbove) {
        String direction = getPlayerDirection(player);

        // Set "tali" metadata for air block pearls
        if (shouldSetTaliMetadata(block, blockAbove, direction)) {
            pearl.setMetadata("tali", new FixedMetadataValue(AlleyPlugin.getInstance(), true));
        }

        // Set "cpearl" metadata for various block types
        if (shouldSetCpearlMetadata(block, direction)) {
            pearl.setMetadata("cpearl", new FixedMetadataValue(AlleyPlugin.getInstance(), true));
        }

        // Set metadata for slabs and stairs
        if (shouldSetSlabStairMetadata(block)) {
            pearl.setMetadata("tali", new FixedMetadataValue(AlleyPlugin.getInstance(), true));
        }
    }

    private boolean shouldSetTaliMetadata(Block block, Block blockAbove, String direction) {
        if (!getConfig().getBoolean("block-down-equals-air")) {
            return false;
        }

        BlockFace directionFace = BlockFace.valueOf(direction);
        Block blockInDirection = block.getRelative(directionFace);
        Block blockAboveInDirection = blockAbove.getRelative(directionFace);

        return block.getRelative(BlockFace.UP).getType() != Material.FENCE_GATE &&
                block.getRelative(BlockFace.UP).getType().isSolid() &&
                !block.getType().isSolid() &&
                block.getRelative(BlockFace.DOWN).getType().isSolid() &&
                !blockInDirection.getType().isSolid() &&
                !blockAboveInDirection.getType().isSolid();
    }

    private boolean shouldSetCpearlMetadata(Block block, String direction) {
        // Cobble wall
        if (getConfig().getBoolean("TalibanPearl.cobblewall") &&
                isCobbleWall(block) &&
                block.getRelative(BlockFace.UP).getType().isSolid()) {
            return true;
        }

        // Chest
        if (getConfig().getBoolean("TalibanPearl.chest") && isChest(block) &&
                block.getRelative(BlockFace.UP).getType().isSolid() &&
                block.getRelative(BlockFace.UP).getType() != Material.FENCE_GATE) {
            BlockFace directionFace = BlockFace.valueOf(direction);
            return !block.getRelative(directionFace).getType().isSolid();
        }

        // Chest below
        if (getConfig().getBoolean("TalibanPearl.chest") &&
                isChest(block.getRelative(BlockFace.DOWN)) &&
                !isChest(block.getRelative(BlockFace.UP)) &&
                block.getRelative(BlockFace.UP).getType().isSolid() &&
                block.getRelative(BlockFace.UP).getType() != Material.FENCE_GATE) {
            return true;
        }

        // Trapdoor
        if (getConfig().getBoolean("TalibanPearl.trapdoor") &&
                block.getType() == Material.TRAP_DOOR &&
                block.getRelative(BlockFace.UP).getType().isSolid()) {
            return true;
        }

        // Piston base
        if (getConfig().getBoolean("TalibanPearl.piston-base") &&
                isPistonBase(block.getRelative(BlockFace.DOWN)) &&
                block.getRelative(BlockFace.UP).getType().isSolid()) {
            return true;
        }

        // End portal
        if (getConfig().getBoolean("TalibanPearl.endportal") &&
                isEndPortal(block) &&
                block.getRelative(BlockFace.UP).getType().isSolid()) {
            return true;
        }

        // Bed
        if (getConfig().getBoolean("TalibanPearl.bed") &&
                isBed(block) &&
                block.getRelative(BlockFace.UP).getType().isSolid()) {
            return true;
        }

        return false;
    }

    private boolean shouldSetSlabStairMetadata(Block block) {
        return (getConfig().getBoolean("TalibanPearl.Slabs") && isSlab(block)) ||
                (getConfig().getBoolean("TalibanPearl.Stairs") && isStair(block));
    }

    private void adjustTeleportLocation(Location destination, PlayerTeleportEvent event) {
        Block block = destination.getBlock();

        // Adjust Y position for stairs
        if (isStair(block) && block.getRelative(BlockFace.UP).getType().isSolid()) {
            destination.setY(destination.getY() - 0.5);
            event.setTo(destination);
        }

        // Adjust Y position for slabs
        if (isSlab(block) && block.getRelative(BlockFace.UP).getType().isSolid()) {
            destination.setY(destination.getY() - 0.5);
            event.setTo(destination);
        }

        // Center X and Z coordinates
        destination.setX(destination.getBlockX() + 0.5);
        destination.setZ(destination.getBlockZ() + 0.5);
        event.setTo(destination);
    }

    private Location getTeleportLocation(Location pearlLocation, Player player) {
        String direction = getDirectionFromPlayer(player);

        double offsetX = 0, offsetZ = 0;
        switch (direction) {
            case "N":
            case "NW":
                offsetZ = -1.5;
                break;
            case "W":
            case "SW":
                offsetX = -1.5;
                break;
            case "S":
            case "SE":
                offsetZ = 1.5;
                break;
            case "E":
            case "NE":
                offsetX = 1.5;
                break;
            default:
                return null;
        }

        return new Location(player.getWorld(),
                pearlLocation.getX() + offsetX,
                pearlLocation.getY(),
                pearlLocation.getZ() + offsetZ,
                player.getLocation().getYaw(),
                player.getLocation().getPitch());
    }

    private Block getBlockAtPlayerFeet(Player player) {
        String direction = getDirectionFromPlayer(player);

        Location playerLoc = player.getLocation();
        double offsetX = 0, offsetZ = 0;

        switch (direction) {
            case "N":
            case "NW":
                offsetZ = -0.7;
                break;
            case "W":
            case "SW":
                offsetX = -0.7;
                break;
            case "S":
            case "SE":
                offsetZ = 0.7;
                break;
            case "E":
            case "NE":
                offsetX = 0.7;
                break;
        }

        Location blockLoc = new Location(player.getWorld(),
                playerLoc.getX() + offsetX,
                playerLoc.getY(),
                playerLoc.getZ() + offsetZ);
        return blockLoc.getBlock();
    }

    private Block getBlockAtPlayerHead(Player player) {
        String direction = getDirectionFromPlayer(player);

        Location playerLoc = player.getLocation();
        double offsetX = 0, offsetZ = 0;

        switch (direction) {
            case "N":
            case "NW":
                offsetZ = -0.7;
                break;
            case "W":
            case "SW":
                offsetX = -0.7;
                break;
            case "S":
            case "SE":
                offsetZ = 0.7;
                break;
            case "E":
            case "NE":
                offsetX = 0.7;
                break;
        }

        Location blockLoc = new Location(player.getWorld(),
                playerLoc.getX() + offsetX,
                playerLoc.getY() + 1,
                playerLoc.getZ() + offsetZ);
        return blockLoc.getBlock();
    }

    private String getDirectionFromPlayer(Player player) {
        return getDirectionFromYaw(player.getLocation().getYaw());
    }

    private String getDirectionFromYaw(float yaw) {
        double rotation = (yaw - 90) % 360;
        if (rotation < 0) rotation += 360.0;

        if (rotation < 22.5) return "W";
        else if (rotation < 67.5) return "NW";
        else if (rotation < 112.5) return "N";
        else if (rotation < 157.5) return "NE";
        else if (rotation < 202.5) return "E";
        else if (rotation < 247.5) return "SE";
        else if (rotation < 292.5) return "S";
        else if (rotation < 337.5) return "SW";
        else return "W";
    }

    private String getPlayerDirection(Player player) {
        String shortDirection = getDirectionFromPlayer(player);
        switch (shortDirection) {
            case "N":
                return "NORTH";
            case "NW":
                return "NORTH_WEST";
            case "W":
                return "WEST";
            case "SW":
                return "SOUTH_WEST";
            case "S":
                return "SOUTH";
            case "SE":
                return "SOUTH_EAST";
            case "E":
                return "EAST";
            case "NE":
                return "NORTH_EAST";
            default:
                return null;
        }
    }

    private boolean isEndPortal(Block block) {
        return block.getType() == Material.ENDER_PORTAL_FRAME;
    }

    private boolean isBed(Block block) {
        Material type = block.getType();
        return type == Material.BED_BLOCK || type == Material.BED;
    }

    private boolean isChest(Block block) {
        Material type = block.getType();
        return type == Material.ENDER_CHEST || type == Material.CHEST || type == Material.TRAPPED_CHEST;
    }

    private boolean isCobbleWall(Block block) {
        return block.getType() == Material.COBBLE_WALL;
    }

    private boolean isSlab(Block block) {
        Material type = block.getType();
        return type == Material.WOOD_STEP || type == Material.STEP;
    }

    private boolean isStair(Block block) {
        Material type = block.getType();
        return type == Material.ACACIA_STAIRS ||
                type == Material.BIRCH_WOOD_STAIRS ||
                type == Material.BRICK_STAIRS ||
                type == Material.COBBLESTONE_STAIRS ||
                type == Material.DARK_OAK_STAIRS ||
                type == Material.JUNGLE_WOOD_STAIRS ||
                type == Material.NETHER_BRICK_STAIRS ||
                type == Material.QUARTZ_STAIRS ||
                type == Material.SANDSTONE_STAIRS ||
                type == Material.SPRUCE_WOOD_STAIRS ||
                type == Material.SMOOTH_STAIRS ||
                type == Material.WOOD_STAIRS;
    }

    private boolean isPistonBase(Block block) {
        Material type = block.getType();
        return type == Material.PISTON_BASE || type == Material.PISTON_STICKY_BASE;
    }

    private boolean isDirectionCompatible(BlockFace gateDirection, BlockFace playerDirection) {
        return (gateDirection == BlockFace.NORTH && playerDirection == BlockFace.NORTH) ||
                (gateDirection == BlockFace.SOUTH && playerDirection == BlockFace.NORTH) ||
                (gateDirection == BlockFace.NORTH && playerDirection == BlockFace.SOUTH) ||
                (gateDirection == BlockFace.SOUTH && playerDirection == BlockFace.SOUTH) ||
                (gateDirection == BlockFace.WEST && playerDirection == BlockFace.WEST) ||
                (gateDirection == BlockFace.EAST && playerDirection == BlockFace.WEST) ||
                (gateDirection == BlockFace.WEST && playerDirection == BlockFace.EAST) ||
                (gateDirection == BlockFace.EAST && playerDirection == BlockFace.EAST);
    }

    private boolean isOppositeDirection(BlockFace stairDirection, BlockFace playerDirection) {
        return (stairDirection == BlockFace.WEST && playerDirection == BlockFace.EAST) ||
                (stairDirection == BlockFace.EAST && playerDirection == BlockFace.WEST) ||
                (stairDirection == BlockFace.NORTH && playerDirection == BlockFace.SOUTH) ||
                (stairDirection == BlockFace.SOUTH && playerDirection == BlockFace.NORTH);
    }

    private void cancelPearlAndRefund(Player player, ProjectileLaunchEvent event, String message) {
        event.setCancelled(true);
        InventoryUtil.giveItem(player, Material.ENDER_PEARL, 1);
        player.updateInventory();
        player.sendMessage(CC.translate(message));
    }

    private void sendInvalidPearlFeedback(Player player, Event event) {
        if (getConfig().getBoolean("Invalid-Pearl-Message")) {
            player.sendMessage(CC.translate(getConfig().getString("Message")));
        }

        if (getConfig().getBoolean("return-pearl")) {
            player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1));
            player.updateInventory();
        }

        if (event instanceof Cancellable) {
            ((Cancellable) event).setCancelled(true);
        }
    }

    private void applyCooldown(Player player) {
        CooldownService cooldownService = AlleyPlugin.getInstance().getService(CooldownService.class);
        Optional<Cooldown> optionalCooldown = Optional.ofNullable(
                cooldownService.getCooldown(player.getUniqueId(), CooldownType.ENDER_PEARL)
        );

        Cooldown cooldown = optionalCooldown.orElseGet(() -> {
            Cooldown newCooldown = new Cooldown(CooldownType.ENDER_PEARL,
                    () -> player.sendMessage(AlleyPlugin.getInstance().getService(LocaleService.class).getString(GlobalMessagesLocaleImpl.COOLDOWN_CAN_NOW_USE_PEARLS_AGAIN)));
            cooldownService.addCooldown(player.getUniqueId(), CooldownType.ENDER_PEARL, newCooldown);
            return newCooldown;
        });

        cooldown.resetCooldown();
    }

    private ConfigurationSection getConfig() {
        return AlleyPlugin.getInstance().getService(ConfigService.class).getPearlConfig();
    }
}