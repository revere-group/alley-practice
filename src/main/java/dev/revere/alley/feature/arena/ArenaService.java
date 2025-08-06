package dev.revere.alley.feature.arena;

import dev.revere.alley.bootstrap.lifecycle.Service;
import dev.revere.alley.feature.arena.internal.types.StandAloneArena;
import dev.revere.alley.feature.kit.Kit;
import org.bukkit.World;

import java.util.List;

/**
 * @author Remi
 * @project alley-practice
 * @date 2/07/2025
 */
public interface ArenaService extends Service {
    /**
     * Gets a list of all persistent arenas loaded from the configuration.
     *
     * @return An unmodifiable list of the base arenas.
     */
    List<Arena> getArenas();

    /**
     * Gets a list of currently active temporary (copied) arenas.
     *
     * @return An unmodifiable list of the temporary stand-alone arenas.
     */
    List<StandAloneArena> getTemporaryArenas();

    /**
     * Gets the world used for hosting temporary arena copies.
     *
     * @return The temporary Bukkit World.
     */
    World getTemporaryWorld();

    /**
     * Retrieves an arena by its unique name.
     *
     * @param name The name of the arena.
     * @return The AbstractArena, or null if not found.
     */
    Arena getArenaByName(String name);

    /**
     * Selects a random, enabled arena that is compatible with the given kit.
     * If the selected arena is a StandAloneArena, a temporary copy is created and returned.
     *
     * @param kit The kit to find a compatible arena for.
     * @return A suitable AbstractArena, or null if none are available.
     */
    Arena getRandomArena(Kit kit);

    /**
     * Saves an arena's data to the configuration file.
     *
     * @param arena The arena to save.
     */
    void saveArena(Arena arena);

    /**
     * Deletes an arena's data from the configuration file.
     *
     * @param arena The arena to delete.
     */
    void deleteArena(Arena arena);

    /**
     * Deletes a temporary StandAloneArena from the service's tracking list and the temporary world.
     *
     * @param arena The StandAloneArena to delete.
     */
    void deleteTemporaryArena(StandAloneArena arena);

    /**
     * Creates a temporary, instanced copy of a StandAloneArena in the dedicated temporary world.
     *
     * @param originalArena The original StandAloneArena to copy.
     * @return The new, temporary StandAloneArena instance.
     */
    StandAloneArena createTemporaryArenaCopy(StandAloneArena originalArena);

    /**
     * Takes an arena and returns a temporary copy if it's a StandAloneArena,
     * or returns the original arena if it's any other type.
     *
     * @param arena The arena to process.
     * @return A temporary copy or the original arena.
     */
    Arena selectArenaWithPotentialTemporaryCopy(Arena arena);

    /**
     * Adds a newly created arena to the service's tracking list and caches.
     *
     * @param arena The new arena to add.
     */
    void registerNewArena(Arena arena);

    /**
     * Provides access to the internal arena validator for external validation needs.
     *
     * @return The ArenaValidator instance.
     */
    ArenaValidator getArenaValidator();
}