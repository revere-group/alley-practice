package dev.revere.alley.feature.division;

import dev.revere.alley.feature.division.model.DivisionTier;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;

import java.util.List;

/**
 * @author Emmy
 * @project Alley
 * @since 25/01/2025
 */
@Getter
@Setter
public class Division {
    private final List<DivisionTier> tiers;
    private final String name;

    private String displayName;
    private String description;

    private int durability;

    private Material icon;

    /**
     * Constructor for the Division class.
     *
     * @param name        The name of the division.
     * @param displayName The display name of the division.
     * @param description The description of the division.
     * @param durability  The durability of the division.
     * @param icon        The icon of the division.
     * @param tiers       The tiers of the division.
     */
    public Division(String name, String displayName, String description, int durability, Material icon, List<DivisionTier> tiers) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.durability = durability;
        this.icon = icon;
        this.tiers = tiers;
    }

    /**
     * Gets the division tier by the name.
     *
     * @param tier The name of the division tier.
     * @return The division tier.
     */
    public String getTier(int tier) {
        if (tier < 0 || tier >= this.tiers.size()) {
            return null;
        }

        DivisionTier divisionTier = this.tiers.get(tier);
        return divisionTier.getName();
    }

    /**
     * Gets the division tier by the name.
     *
     * @param tier The name of the division tier.
     * @return The division tier.
     */
    public DivisionTier getTier(String tier) {
        for (DivisionTier divisionTier : this.tiers) {
            if (divisionTier.getName().equalsIgnoreCase(tier)) {
                return divisionTier;
            }
        }
        return null;
    }

    /**
     * Gets the wins of the last tier in the division.
     *
     * @return The wins of the last tier.
     */
    public int getTotalWins() {
        return this.tiers.get(this.tiers.size() - 1).getRequiredWins();
    }
}