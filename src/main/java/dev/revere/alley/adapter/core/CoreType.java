package dev.revere.alley.adapter.core;

import lombok.Getter;

/**
 * @author Emmy
 * @project Alley
 * @since 26/04/2025
 */
@Getter
public enum CoreType {
    DEFAULT("Default", "Revere Group."),
    PHOENIX("Phoenix", "Refine Development"),
    AQUA("AquaCore", "Activated_, FaceSlap_"),
    HELIUM("Helium", "Plasma Services"),

    ;

    private final String pluginName;
    private final String pluginAuthor;

    /**
     * Constructor for the EnumCoreType enum.
     *
     * @param pluginName   The name of the bootstrap.
     * @param pluginAuthor The author of the bootstrap.
     */
    CoreType(String pluginName, String pluginAuthor) {
        this.pluginName = pluginName;
        this.pluginAuthor = pluginAuthor;
    }
}