package dev.revere.alley.feature.arena.internal.types;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.serializer.Serializer;
import dev.revere.alley.core.config.ConfigService;
import dev.revere.alley.core.locale.LocaleService;
import dev.revere.alley.core.locale.internal.impl.SettingsLocaleImpl;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.arena.ArenaService;
import dev.revere.alley.feature.arena.ArenaType;
import dev.revere.alley.feature.arena.schematic.ArenaSchematicService;
import dev.revere.alley.feature.match.internal.types.BedMatch;
import dev.revere.alley.feature.match.internal.types.RoundsMatch;
import dev.revere.alley.feature.match.model.GameParticipant;
import dev.revere.alley.feature.match.model.internal.MatchGamePlayer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.UUID;

/**
 * @author Emmy
 * @project Alley
 * @date 20/05/2024 - 19:14
 */
@Setter
@Getter
public class StandAloneArena extends Arena {
    protected final AlleyPlugin plugin = AlleyPlugin.getInstance();

    private boolean isTemporaryCopy = false;
    private String originalArenaName;
    private int copyId = -1;

    private Location team1Portal;
    private Location team2Portal;

    private int portalRadius;
    private int heightLimit;
    private int voidLevel;

    /**
     * Constructor for the StandAloneArena class.
     *
     * @param name    The name of the arena.
     * @param minimum The minimum location of the arena.
     * @param maximum The maximum location of the arena.
     */
    public StandAloneArena(String name, Location minimum, Location maximum, Location team1Portal, Location team2Portal, int heightLimit, int voidLevel) {
        super(name, minimum, maximum);

        if (team1Portal != null) this.team1Portal = team1Portal;
        if (team2Portal != null) this.team2Portal = team2Portal;
        this.portalRadius = this.plugin.getService(LocaleService.class).getInt(SettingsLocaleImpl.GAME_ARENA_PORTAL_RADIUS);
        this.heightLimit = heightLimit;
        this.voidLevel = voidLevel;
    }

    public StandAloneArena(String originalArenaName, int copyId, Location minimum, Location maximum, Location team1Portal, Location team2Portal, int heightLimit, int voidLevel) {
        super(originalArenaName + "-copy-" + copyId, minimum, maximum);
        this.originalArenaName = originalArenaName;
        this.copyId = copyId;
        this.isTemporaryCopy = true;

        if (team1Portal != null) this.team1Portal = team1Portal;
        if (team2Portal != null) this.team2Portal = team2Portal;
        this.portalRadius = this.plugin.getService(LocaleService.class).getInt(SettingsLocaleImpl.GAME_ARENA_PORTAL_RADIUS);
        this.heightLimit = heightLimit;
        this.voidLevel = voidLevel;
    }

    @Override
    public ArenaType getType() {
        return ArenaType.STANDALONE;
    }

    @Override
    public void createArena() {
        if (!this.isTemporaryCopy) {
            ArenaService arenaService = this.plugin.getService(ArenaService.class);
            arenaService.registerNewArena(this);
            this.saveArena();
        }
    }

    @Override
    public void saveArena() {
        String name = "arenas." + this.getName();
        ConfigService configService = this.plugin.getService(ConfigService.class);
        FileConfiguration config = configService.getArenasConfig();

        config.set(name, null);
        config.set(name + ".type", this.getType().name());
        config.set(name + ".minimum", Serializer.serializeLocation(this.getMinimum()));
        config.set(name + ".maximum", Serializer.serializeLocation(this.getMaximum()));
        config.set(name + ".center", Serializer.serializeLocation(this.getCenter()));
        config.set(name + ".pos1", Serializer.serializeLocation(this.getPos1()));
        config.set(name + ".pos2", Serializer.serializeLocation(this.getPos2()));
        config.set(name + ".kits", this.getKits());
        config.set(name + ".enabled", this.isEnabled());
        config.set(name + ".display-name", this.getDisplayName());

        if (this.team1Portal != null)
            config.set(name + ".team-one-portal", Serializer.serializeLocation(this.team1Portal));
        if (this.team2Portal != null)
            config.set(name + ".team-two-portal", Serializer.serializeLocation(this.team2Portal));

        config.set(name + ".height-limit", this.heightLimit);
        config.set(name + ".void-level", this.voidLevel);

        configService.saveConfig(configService.getConfigFile("storage/arenas.yml"), config);

        this.plugin.getService(ArenaSchematicService.class).updateSchematic(this);
    }

    @Override
    public void deleteArena() {
        this.deleteCopiedArena();

        FileConfiguration config = this.plugin.getService(ConfigService.class).getArenasConfig();
        config.set("arenas." + this.getName(), null);

        this.plugin.getService(ConfigService.class).saveConfig(this.plugin.getService(ConfigService.class).getConfigFile("storage/arenas.yml"), config);
    }

    public void deleteCopiedArena() {
        if (!this.isTemporaryCopy) {
            return;
        }

        this.plugin.getService(ArenaSchematicService.class).delete(this);
    }

    public void verifyArenaExists() {
        if (this.getMinimum() == null || this.getMaximum() == null) {
            Bukkit.broadcastMessage("[Arena] Cannot verify - bounds are null");
            return;
        }

        org.bukkit.World world = this.getMinimum().getWorld();
        if (world == null) {
            Bukkit.broadcastMessage("[Arena] Cannot verify - world is null");
            return;
        }

        Bukkit.broadcastMessage("[Arena] Verifying arena: " + this.getName());
        Bukkit.broadcastMessage("[Arena] Bounds: " + this.getMinimum() + " to " + this.getMaximum());
        Bukkit.broadcastMessage("[Arena] World: " + world.getName());

        int nonAirBlocks = 0;
        int totalSampled = 0;

        Location min = this.getMinimum();
        Location max = this.getMaximum();

        for (int x = min.getBlockX(); x <= max.getBlockX(); x += 10) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); y += 10) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z += 10) {
                    org.bukkit.block.Block block = world.getBlockAt(x, y, z);
                    totalSampled++;

                    if (block.getType() != org.bukkit.Material.AIR) {
                        nonAirBlocks++;
                        if (nonAirBlocks <= 5) {
                            Bukkit.broadcastMessage("[Arena] Found block: " + block.getType() + " at " + x + "," + y + "," + z);
                        }
                    }
                }
            }
        }

        Bukkit.broadcastMessage("[Arena] Verification complete: " + nonAirBlocks + "/" + totalSampled + " sampled blocks are non-air");

        if (nonAirBlocks == 0) {
            Bukkit.broadcastMessage("[Arena] WARNING: No non-air blocks found! Arena may not have pasted correctly.");
        } else {
            Bukkit.broadcastMessage("[Arena] Arena appears to exist with " + nonAirBlocks + " non-air blocks sampled.");
        }
    }

    public StandAloneArena createCopy(World targetWorld, Location targetLocation, int copyId) {
        if (isTemporaryCopy) {
            throw new IllegalStateException("Cannot create a copy of a temporary copy arena.");
        }

        Location originalMin = this.getMinimum();
        Location originalMax = this.getMaximum();

        int actualMinX = Math.min(originalMin.getBlockX(), originalMax.getBlockX());
        int actualMaxX = Math.max(originalMin.getBlockX(), originalMax.getBlockX());

        int actualMinY = Math.min(originalMin.getBlockY(), originalMax.getBlockY());
        int actualMaxY = Math.max(originalMin.getBlockY(), originalMax.getBlockY());

        int actualMinZ = Math.min(originalMin.getBlockZ(), originalMax.getBlockZ());
        int actualMaxZ = Math.max(originalMin.getBlockZ(), originalMax.getBlockZ());

        int sizeX = actualMaxX - actualMinX + 1;
        int sizeY = actualMaxY - actualMinY + 1;
        int sizeZ = actualMaxZ - actualMinZ + 1;

        Location newMin = new Location(targetWorld, targetLocation.getX(), targetLocation.getY(), targetLocation.getZ());
        Location newMax = new Location(targetWorld, targetLocation.getX() + sizeX - 1, targetLocation.getY() + sizeY - 1, targetLocation.getZ() + sizeZ - 1);

        Location newTeam1Portal = null;
        Location newTeam2Portal = null;

        if (this.team1Portal != null) {
            int offsetX = this.team1Portal.getBlockX() - actualMinX;
            int offsetY = this.team1Portal.getBlockY() - actualMinY;
            int offsetZ = this.team1Portal.getBlockZ() - actualMinZ;
            newTeam1Portal = targetLocation.clone().add(offsetX, offsetY, offsetZ);
        }

        if (this.team2Portal != null) {
            int offsetX = this.team2Portal.getBlockX() - actualMinX;
            int offsetY = this.team2Portal.getBlockY() - actualMinY;
            int offsetZ = this.team2Portal.getBlockZ() - actualMinZ;
            newTeam2Portal = targetLocation.clone().add(offsetX, offsetY, offsetZ);
        }

        int yOffset = newMin.getBlockY() - actualMinY;

        int adjustedHeightLimit = this.heightLimit + yOffset;
        int adjustedVoidLevel = this.voidLevel + yOffset;

        StandAloneArena copiedArena = new StandAloneArena(
                this.getName(), copyId, newMin, newMax,
                newTeam1Portal, newTeam2Portal, adjustedHeightLimit, adjustedVoidLevel
        );

        copiedArena.setEnabled(true);
        copiedArena.setDisplayName(this.getDisplayName());
        copiedArena.setKits(this.getKits());

        double middleOffset = 0.5;

        if (this.getPos1() != null) {
            int offsetX = this.getPos1().getBlockX() - actualMinX;
            int offsetY = this.getPos1().getBlockY() - actualMinY;
            int offsetZ = this.getPos1().getBlockZ() - actualMinZ;
            Location location = targetLocation.clone().add(offsetX + middleOffset, offsetY, offsetZ + middleOffset);
            location.setYaw(getPos1().getYaw());
            copiedArena.setPos1(location);
        }

        if (this.getPos2() != null) {
            int offsetX = this.getPos2().getBlockX() - actualMinX;
            int offsetY = this.getPos2().getBlockY() - actualMinY;
            int offsetZ = this.getPos2().getBlockZ() - actualMinZ;
            Location location = targetLocation.clone().add(offsetX + middleOffset, offsetY, offsetZ + middleOffset);
            location.setYaw(getPos2().getYaw());
            copiedArena.setPos2(location);
        }

        if (this.getCenter() != null) {
            int offsetX = this.getCenter().getBlockX() - actualMinX;
            int offsetY = this.getCenter().getBlockY() - actualMinY;
            int offsetZ = this.getCenter().getBlockZ() - actualMinZ;
            copiedArena.setCenter(targetLocation.clone().add(offsetX + middleOffset, offsetY, offsetZ + middleOffset));
        }

        return copiedArena;
    }

    /**
     * Check if the player is in the enemy portal.
     *
     * @param match          The match.
     * @param playerLocation The location of the player.
     * @param playerTeam     The team of the player.
     * @return Whether the player is in the enemy portal or not.
     */
    public boolean isEnemyPortal(RoundsMatch match, Location playerLocation, GameParticipant<MatchGamePlayer> playerTeam) {
        Location enemyPortal = playerTeam == match.getParticipantA() ? this.team2Portal : this.team1Portal;
        return playerLocation.distance(enemyPortal) < this.portalRadius;
    }

    /**
     * Check if the block is an enemy bed.
     *
     * @param block              The block to check.
     * @param breakerParticipant The player who is breaking the bed.
     * @return Whether the block is an enemy bed or not.
     */
    public boolean isEnemyBed(Block block, GameParticipant<MatchGamePlayer> breakerParticipant) {
        Location bedLocation = block.getLocation();

        UUID breakerUUID = breakerParticipant.getLeader().getUuid();
        Profile profile = this.plugin.getService(ProfileService.class).getProfile(breakerUUID);

        Location spawnA = this.getPos1();
        Location spawnB = this.getPos2();

        BedMatch match = (BedMatch) profile.getMatch();

        boolean isBreakerTeamA = match != null && match.getParticipantA() == breakerParticipant;
        Location ownSpawn = isBreakerTeamA ? spawnA : spawnB;
        Location enemySpawn = isBreakerTeamA ? spawnB : spawnA;

        double distanceToOwn = bedLocation.distanceSquared(ownSpawn);
        double distanceToEnemy = bedLocation.distanceSquared(enemySpawn);

        return distanceToEnemy < distanceToOwn;
    }
}