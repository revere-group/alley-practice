package dev.revere.alley.adapter.knockback;

import lombok.Getter;

/**
 * @author Emmy
 * @project Alley
 * @since 26/04/2025
 */
@Getter
public enum KnockbackType {
    DEFAULT("Default", "Revere Group"),
    ZONE("ZoneSpigot", "Revere Group"),

    ;

    private final String spigotName;
    private final String spigotAuthor;

    /**
     * Constructor for the EnumKnockbackType enum.
     *
     * @param spigotName   The name of the spigot.
     * @param spigotAuthor The author of the spigot.
     */
    KnockbackType(String spigotName, String spigotAuthor) {
        this.spigotName = spigotName;
        this.spigotAuthor = spigotAuthor;
    }
}