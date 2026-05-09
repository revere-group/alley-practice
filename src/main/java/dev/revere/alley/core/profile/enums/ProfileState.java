package dev.revere.alley.core.profile.enums;

import lombok.Getter;

/**
 * @author Emmy
 * @project Alley
 * @date 5/21/2024
 */
@Getter
public enum ProfileState {
    LOBBY("Lobby", "The player is in the lobby"),
    WAITING("Waiting", "The player is waiting to queue for an opponent"),
    PLAYING("Playing", "The player is playing a match"),
    FIGHTING_BOT("Fighting Bot", "The player is fighting a bot"),
    SPECTATING("Spectating", "The player is spectating a match"),
    EDITING("Editing", "The player is editing a kit"),
    TOURNAMENT_LOBBY("Tournament", "The player is in a tournament lobby"),
    PLAYING_EVENT("Event", "The player is in an event"),
    FFA("FFA", "The player is in the FFA lobby"),

    ;

    private final String name;
    private final String description;

    /**
     * Constructor for the EnumProfileState enum.
     *
     * @param name        The name of the profile state.
     * @param description The description of the profile state.
     */
    ProfileState(String name, String description) {
        this.name = name;
        this.description = description;
    }
}