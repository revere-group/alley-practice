package dev.revere.alley.feature.cooldown;

import lombok.Getter;

/**
 * @author Emmy
 * @project Alley
 * @date 27/10/2024 - 08:43
 */
@Getter
public enum CooldownType {
    ENDER_PEARL(15 * 1000L),
    PARTY_ANNOUNCE_COOLDOWN(600 * 1000L),
    FIREBALL(500L),
    GOLDEN_HEAD_CONSUME(2 * 1000L),
    TOURNAMENT_HOST(1800 * 1000L)

    ;

    private final long cooldownDuration;

    /**
     * Constructor for the EnumCooldownType enum.
     *
     * @param cooldownDuration the duration of the cooldown.
     */
    CooldownType(long cooldownDuration) {
        this.cooldownDuration = cooldownDuration;
    }
}