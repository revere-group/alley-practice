package dev.revere.alley.core.locale.internal.impl.message;

import dev.revere.alley.common.text.Symbol;
import dev.revere.alley.core.locale.LocaleEntry;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author Emmy
 * @project Alley
 * @since 09/09/2025
 */
@Getter
public enum GlobalMessagesLocaleImpl implements LocaleEntry {
    ARENA_NOT_FOUND("messages/global-messages.yml", "arena.error.not-found", "&cAn arena named &6{arena-name} &cdoes not exist!"),
    ARENA_ALREADY_EXISTS("messages/global-messages.yml", "arena.error.already-exists", "&cAn arena with that name already exists!"),
    ARENA_NO_SELECTION("messages/global-messages.yml", "arena.error.no-selection", "&cYou don't have the bounds selected for the arena! Use &b/arena wand&c!"),
    ARENA_KIT_ALREADY_ADDED("messages/global-messages.yml", "arena.error.kit-already-added-to-arena", "&cThe kit &6{kit-name} &cis already added to the arena &6{arena-name}&c!"),
    ARENA_ARENA_DOES_NOT_HAVE_KIT("messages/global-messages.yml", "arena.error.arena-does-not-have-kit", "&cThe arena &6{arena-name} &cdoes not have the kit &6{kit-name}&c!"),
    ARENA_CAN_NOT_SET_CUBOID_FFA("messages/global-messages.yml", "arena.error.cannot-set-cuboid-ffa", "&cYou cannot set cuboids for Free-For-All arenas! You must use: &4/arena setsafezone pos1/pos2&c."),
    ARENA_MUST_BE_STANDALONE("messages/global-messages.yml", "arena.error.must-be-standalone", "&cThe arena &6{arena-name} &cmust be standalone to do this!"),
    ARENA_INVALID_PORTAL("messages/global-messages.yml", "arena.error.invalid-portal", "&cInvalid portal. Please use 'red' or 'blue'."),
    ARENA_HEIGHT_LIMIT_OUT_OF_BOUNDS("messages/global-messages.yml", "arena.error.height-limit-out-of-bounds", "&cHeight limit must be between 0 and 256!"),
    ARENA_VOID_LEVEL_OUT_OF_BOUNDS("messages/global-messages.yml", "arena.error.void-level-out-of-bounds", "&cVoid level must be between 0 and 256!"),
    ARENA_FFA_ARENAS_NO_SPAWNS("messages/global-messages.yml", "arena.error.ffa-arenas-no-spawns", "&cFFA Arenas do not need spawn positions. Use &4/ffa setspawn&c."),
    ARENA_CANNOT_TOGGLE_FFA("messages/global-messages.yml", "arena.error.cannot-toggle-ffa", "&cYou cannot toggle Free-For-All arenas!"),
    ARENA_CANNOT_ADD_KITS_TO_FFA("messages/global-messages.yml", "arena.error.cannot-add-kits-to-ffa", "&cYou cannot add kits to Free-For-All arenas!"),
    ARENA_SPAWN_NOT_SET("messages/global-messages.yml", "arena.error.spawn-not-set", "&cYou must set both spawn positions! (/arena setspawn)"),
    ARENA_CENTER_NOT_SET("messages/global-messages.yml", "arena.error.center-not-set", "&cYou must set the center! (/arena setcenter)"),
    ARENA_MUST_ADD_KIT("messages/global-messages.yml", "arena.error.must-add-kit", "&cYou must add at least one kit! (/arena addkit)"),
    ARENA_ASSIGNED_KIT_NULL("messages/global-messages.yml", "arena.error.assigned-kit-null", "&cThe kit &6{kit-name} &cis assigned to this arena but does not exist!"),
    ARENA_STANDALONE_PORTALS_NOT_SET("messages/global-messages.yml", "arena.error.standalone-portals-not-set", "&cYou must set both team portals! (/arena setportal)"),
    ARENA_IS_NOT_FFA("messages/global-messages.yml", "arena.error.is-not-ffa", "&cThis is not a Free-For-All arena!"),
    ARENA_INVALID_SPAWN_TYPE("messages/global-messages.yml", "arena.error.invalid-spawn-type", "&cInvalid spawn type! Valid types: blue, red, ffa"),

    ARENA_SELECTION_TOOL_ADDED("messages/global-messages.yml", "arena.command.selection-tool.added", "&aSuccessfully added the selection tool to your inventory!"),
    ARENA_SELECTION_TOOL_REMOVED("messages/global-messages.yml", "arena.command.selection-tool.removed", "&cSuccessfully removed the selection tool from your inventory!"),
    ARENA_SELECTED_BOUNDARY("messages/global-messages.yml", "arena.command.selected-boundary", "&aSet &6{boundary-type} &aboundary to &6{x}, &6{y}, &6{z}&a!"),
    ARENA_DISPLAY_NAME_SET("messages/global-messages.yml", "arena.command.displayname-set", "&aSuccessfully set the display name of the arena &6{arena-name} &ato &r{display-name}&a!"),
    ARENA_CENTER_SET("messages/global-messages.yml", "arena.command.center-set", "&aSuccessfully set the center of the arena &6{arena-name}&a!"),
    ARENA_CUBOID_SET("messages/global-messages.yml", "arena.command.cuboid-set", "&aSuccessfully set the geom of the arena &6{arena-name}&a!"),
    ARENA_HEIGHT_LIMIT_SET("messages/global-messages.yml", "arena.command.height-limit-set", "&aSuccessfully set the height limit of the arena &6{arena-name}&a to &6{height-limit}&a!"),
    ARENA_PORTAL_SET("messages/global-messages.yml", "arena.command.portal-set", "&aSuccessfully set portal &b{portal} &aof arena &6{arena-name}&a!"),
    ARENA_VOID_LEVEL_SET("messages/global-messages.yml", "arena.command.void-level-set", "&aSuccessfully set the void level of arena &6{arena-name}&a to &6{void-level}&a!"),
    ARENA_TOGGLED("messages/global-messages.yml", "arena.command.toggled", "&aSuccessfully toggled the arena &6{arena-name}&a to &6{status}&a!"),
    ARENA_SPAWN_SET("messages/global-messages.yml", "arena.command.spawn-set", "&aSuccessfully set the &6{position} &fspawn of arena &6{arena-name}&a!"),
    ARENA_FFA_SPAWN_SET("messages/global-messages.yml", "arena.command.ffa-spawn-set", "&aSuccessfully set the FFA spawn of arena &6{arena-name}&a!"),
    ARENA_KIT_ADDED("messages/global-messages.yml", "arena.command.kit-added", "&aSuccessfully added the kit &6{kit-name}&a to arena &6{arena-name}&a!"),
    ARENA_KIT_REMOVED("messages/global-messages.yml", "arena.command.kit-removed", "&cSuccessfully removed the kit &6{kit-name}&c from arena &6{arena-name}&c!"),
    ARENA_CREATED("messages/global-messages.yml", "arena.command.created", "&aSuccessfully created a new arena named &6{arena-name}&a with type &6{arena-type}&a!"),
    ARENA_DELETED("messages/global-messages.yml", "arena.command.deleted", "&cSuccessfully deleted the arena named &6{arena-name}&c!"),
    ARENA_SAVED("messages/global-messages.yml", "arena.command.saved", "&aSuccessfully saved the arena &6{arena-name}&a!"),
    ARENA_SAVED_ALL("messages/global-messages.yml", "arena.command.saved-all", "&aSuccessfully saved all arenas!"),

    COOLDOWN_NOT_FOUND("messages/global-messages.yml", "cooldown.error.not-found", "&cNo cooldown found for &6{player-name} &cof type &6{cooldown-type}&c."),
    COOLDOWN_RESET("messages/global-messages.yml", "cooldown.command.reset", "&aCooldown for &6{player-name} &aof type &6{cooldown-type} &ahas been reset."),

    COOLDOWN_PEARL_MUST_WAIT("messages/global-messages.yml", "cooldown.pearl.must-wait", "&cYou must wait &6{time} &cbefore you can throw another ender pearl!"),
    COOLDOWN_CAN_NOW_USE_PEARLS_AGAIN("messages/global-messages.yml", "cooldown.pearl.can-now-use-pearls-again", "&aYou can now use ender pearls again!"),
    COOLDOWN_CANNOT_USE_AT_FFA_SPAWN("messages/global-messages.yml", "cooldown.pearl.cannot-use-at-ffa-spawn", "&cYou cannot use ender pearls while in an FFA spawn!"),

    COOLDOWN_FIREBALL_MUST_WAIT("messages/global-messages.yml", "cooldown.fireball.must-wait", "&cYou must wait &6{time} &cbefore you can throw another fireball!"),
    COOLDOWN_PARTY_ANNOUNCE_MUST_WAIT("messages/global-messages.yml", "cooldown.party-announce.must-wait", "&cYou must wait &6{time} &cbefore you can announce your party again!"),
    COOLDOWN_GOLDEN_HEAD_MUST_WAIT("messages/global-messages.yml", "cooldown.golden-head.must-wait", "&cYou must wait &6{time} &cbefore you can eat another golden head!"),

    COSMETIC_NOT_OWNED("messages/global-messages.yml", "cosmetics.not-owned", "&cYou do not own the &6{cosmetic-name}&c cosmetic!"),
    COSMETIC_SELECTED("messages/global-messages.yml", "cosmetics.selected", "&aSuccessfully selected the &6{cosmetic-name} &acosmetic!"),
    COSMETIC_ALREADY_SELECTED("messages/global-messages.yml", "cosmetics.already-selected", "&cYou already selected the &6{cosmetic-name}&c cosmetic!"),

    COSMETIC_SET_FOR_PLAYER("messages/global-messages.yml", "cosmetics.command.set", "&aSuccessfully set &6{cosmetic} &a{type} as the active cosmetic for &6{player}&a!"),
    COSMETICS_NONE_REGISTERED("messages/global-messages.yml", "cosmetics.error.none-registered", "&cThere are no cosmetics registered on the server!"),
    COSMETIC_TYPE_NOT_SUPPORTED("messages/global-messages.yml", "cosmetics.error.type-not-supported", "&cThe cosmetic type &6{type} &cis not supported!"),
    COSMETIC_NOT_FOUND("messages/global-messages.yml", "cosmetics.error.not-found", "&cA cosmetic named &6{input} &cdoes not exist!"),

    COSMETIC_ALREADY_OWNED("messages/global-messages.yml", "cosmetics.error.already-owner", "&cYou already own this cosmetic."),
    COSMETIC_PURCHASE_SUCCESS("messages/global-messages.yml", "cosmetics.purchase-success", "&aSuccessfully purchased the &6{cosmetic} &acosmetic!"),
    COSMETIC_PURCHASE_INSUFFICIENT_FUNDS("messages/global-messages.yml", "cosmetics.error.purchase-insufficient-funds", "&cYou don't have enough coins to purchase this."),

    CHAT_CHANNEL_NOT_EXIST("messages/global-messages.yml", "chat.error.channel-not-exist", "&cThe chat channel &6{channel} &cdoes not exist."),
    CHAT_CHANNEL_SET("messages/global-messages.yml", "chat.command.channel-set", "&aSet your chat channel to &6{channel}&a."),
    CHAT_CHANNEL_ALREADY_IN("messages/global-messages.yml", "chat.error.already-in-channel", "&cYou're already in the &6{channel} &cchat channel."),

    CHAT_CLEARED_BY_STAFF("messages/global-messages.yml", "chat.cleared-by-staff", "&c&lCHAT HAS BEEN CLEARED BY STAFF!"),

    CRAFTING_TOGGLED("messages/global-messages.yml", "crafting-operations.command.toggled", "&aCrafting operations for &6{item} &aare now &6{status}&a."),
    CRAFTING_MUST_HOLD_CRAFTABLE_ITEM("messages/global-messages.yml", "crafting-operations.error.must-hold-craftable-item", "&cYou must be holding a craftable item to manage crafting operations."),

    DIVISION_NOT_FOUND("messages/global-messages.yml", "division.error.not-found", "&cA division named &6{division-name} &cdoes not exist!"),
    DIVISION_TIER_NOT_FOUND("messages/global-messages.yml", "division.error.tier-not-found", "&cA tier named &6{tier-name} &cdoes not exist in the &6{division-name} &cdivision!"),
    DIVISION_ALREADY_EXISTS("messages/global-messages.yml", "division.error.already-exists", "&cThe name &6{division-name} &cis already taken by another division!"),

    DIVISION_DESCRIPTION_SET("messages/global-messages.yml", "division.command.description-set", "&aSuccessfully set the description of the &6{division-name} &adivision to &r{description}&a!"),
    DIVISION_DISPLAY_NAME_SET("messages/global-messages.yml", "division.command.display-name-set", "&aSuccessfully set the display name of the &6{division-name} &adivision to &r{display-name}&a!"),
    DIVISION_ICON_SET("messages/global-messages.yml", "division.command.icon-set", "&aSuccessfully set the icon for the division &6{division-name} &ato &6{item-type}:{item-durability}&a!"),
    DIVISION_WINS_SET("messages/global-messages.yml", "division.command.wins-set", "&aSuccessfully set the required wins for the &6{division-name} &adivision's &6{tier-name} &atier to &6{required-wins}&a!"),
    DIVISION_CREATED("messages/global-messages.yml", "division.command.created", "&aSuccessfully created a new division named &6{division-name} &awith &6{required-wins} &awins!"),
    DIVISION_DELETED("messages/global-messages.yml", "division.command.deleted", "&cSuccessfully deleted the division named &6{division-name}&c!"),

    EXPLOSIVE_SETTING_UPDATED("messages/global-messages.yml", "explosive.command.setting-updated", "&aSuccessfully set the explosive {setting-name} value to &6{setting-value}&a."),

    ERROR_AMOUNT_MUST_BE_GREATER_THAN_ZERO("messages/global-messages.yml", "error-messages.amount-must-be-greater-than-zero", "&cThe amount must be greater than zero!"),

    ERROR_DUEL_REQUESTS_EXPIRED("messages/global-messages.yml", "error-messages.duel-requests.error.expired", "&cThat duel request has expired."),
    ERROR_DUEL_REQUESTS_NO_ARENA("messages/global-messages.yml", "error-messages.duel-requests.error.no-arenas", "&cThere is no available arena for that kit."),
    ERROR_DUEL_REQUESTS_CANT_DUEL_SELF("messages/global-messages.yml", "error-messages.duel-requests.error.cant-duel-self", "&cYou can't duel yourself!"),
    ERROR_DUEL_REQUESTS_ALREADY_PENDING("messages/global-messages.yml", "error-messages.duel-requests.error.already-pending", "&cYou already have a pending duel request from that player."),
    ERROR_DUEL_REQUESTS_ALREADY_PENDING_PARTY("messages/global-messages.yml", "error-messages.duel-requests.error.already-pending-party", "&cYou already have a pending duel request from that player or their party."),
    ERROR_DUEL_REQUESTS_INVALID_FROM_PLAYER("messages/global-messages.yml", "error-messages.duel-requests.error.no-pending-request", "&cYou do not have a pending duel request from that player."),
    ERROR_DUEL_REQUESTS_REQUESTS_DISABLED_PLAYER("messages/global-messages.yml", "error-messages.duel-requests.error.player-requests-disabled", "&c{name-color}{player} has duel requests disabled."),

    ERROR_INVALID_PAGE_NUMBER("messages/global-messages.yml", "error-messages.invalid-page-number", "&c'{input}' is not a valid page number! Please enter a valid number."),
    ERROR_INVALID_NUMBER("messages/global-messages.yml", "error-messages.invalid.number", "&c'{input}' is not a valid number! Please enter a valid number."),
    ERROR_INVALID_PLAYER("messages/global-messages.yml", "error-messages.invalid.player", "&cThat player could not be found!"),
    ERROR_INVALID_TYPE("messages/global-messages.yml", "error-messages.invalid.type", "&cInvalid {type}. Available types: &6{types}&c."),
    ERROR_INVALID_ITEM("messages/global-messages.yml", "error-messages.invalid.item", "&cInvalid Item!"),
    ERROR_INVALID_BOOLEAN("messages/global-messages.yml", "error-messages.invalid-parameters", "&cInvalid parameters! Please use true or false."),
    ERROR_NO_MORE_PAGES_AVAILABLE("messages/global-messages.yml", "error-messages.no-more-pages-available", "&c{input} is not a valid page number! There are only &6{max-pages} &cpages available."),

    ERROR_PLAYER_IS_BUSY("messages/global-messages.yml", "error-messages.player.is-busy", "&6{name-color}{player} &cis busy."),
    ERROR_PLAYER_NOT_PLAYING_MATCH("messages/global-messages.yml", "error-messages.player.not-playing-match", "&c{name-color}{player} &cis not in a match."),
    ERROR_PLAYER_NOT_PLAYING_FFA("messages/global-messages.yml", "error-messages.player.not-playing-ffa", "&c{name-color}{player} &cis not in an FFA Match!"),
    ERROR_PLAYER_PARTY_INVITES_DISABLED("messages/global-messages.yml", "error-messages.player.party-invites-disabled", "&c{name-color}{player} &chas party invites disabled."),

    ERROR_MUST_SELECT_MUSIC("messages/global-messages.yml", "profile-messages.error.must-select-music-before-toggle", "&cYou must at least select one music disc before you can toggle them!"),
    ERROR_YOU_MUST_LEAVE_PARTY("messages/global-messages.yml", "error-messages.you.must-leave-party", "&cYou must leave your party before doing that!"),
    ERROR_YOU_MUST_BE_IN_LOBBY("messages/global-messages.yml", "error-messages.you.must-be-in-lobby", "&cYou must be in the lobby to do that!"),
    ERROR_YOU_MUST_HOLD_ITEM("messages/global-messages.yml", "error-messages.you.must-hold-item", "&cYou need to be holding an item!"),
    ERROR_YOU_NOT_PLAYING_FFA("messages/global-messages.yml", "error-messages.you.not-playing-ffa", "&cYou are not in an FFA match!"),
    ERROR_YOU_NOT_SPECTATING_FFA("messages/global-messages.yml", "error-messages.you.not-spectating-ffa", "&cYou are not spectating an FFA match!"),
    ERROR_YOU_ALREADY_SPECTATING_FFA("messages/global-messages.yml", "error-messages.you.already-spectating-ffa", "&cYou are already spectating FFA!"),
    ERROR_YOU_NOT_PLAYING_MATCH("messages/global-messages.yml", "error-messages.you.not-playing-match", "&cYou are not in a match!"),
    ERROR_YOU_ALREADY_PLAYING_MATCH("messages/global-messages.yml", "error-messages.you.already-playing-match", "&cYou are already in a match!"),
    ERROR_YOU_NOT_SPECTATING_MATCH("messages/global-messages.yml", "error-messages.you.not-spectating-match", "&cYou are not spectating a match!"),
    ERROR_YOU_ALREADY_SPECTATING_MATCH("messages/global-messages.yml", "error-messages.you.already-spectating-match", "&cYou are already spectating a match!"),
    ERROR_YOU_NO_MATCH_HISTORY("messages/global-messages.yml", "error-messages.you.no-match-history", "&cYou have no match history."),
    ERROR_YOU_PARTY_NOT_PUBLIC("messages/global-messages.yml", "error-messages.you.party-not-public", "&cYour party is not open to the public to announce. Please run the following command: &7/party open"),

    ERROR_YOU_NOT_IN_PARTY("messages/global-messages.yml", "error-messages.you.not-in-party", "&cYou are not in a party."),
    ERROR_YOU_NOT_PARTY_LEADER("messages/global-messages.yml", "error-messages.you.not-party-leader", "&cYou are not the leader of the party."),
    ERROR_YOU_ALREADY_IN_PARTY("messages/global-messages.yml", "error-messages.you.already-in-party", "&cYou are already in a party."),
    ERROR_YOU_ALREADY_IN_THIS_PARTY("messages/global-messages.yml", "error-messages.you.already-in-this-party", "&cYou are already in this party."),
    ERROR_YOU_PARTY_CHAT_DISABLED("messages/global-messages.yml", "error-messages.you.party-chat-disabled", "&cYou have party messages disabled. &7(To enable: /togglepartymessages)"),
    ERROR_YOU_NO_PARTY_INVITE_FROM_PLAYER("messages/global-messages.yml", "error-messages.you.no-party-invite-from-player", "&cYou do not have a party invitation from &6{name-color}{player}&c."),
    ERROR_YOU_PARTY_NEED_TWO_PLAYERS("messages/global-messages.yml", "error-messages.you.party-need-two-players", "&cYou need at least two players."),
    ERROR_YOU_ARE_IN_COMBAT("messages/global-messages.yml", "error-messages.you.are-in-combat", "&cYou are in combat!"),

    ERROR_YOU_BANNED_FROM_PARTY("messages/global-messages.yml", "error-messages.you.banned-from-party", "&cYou are banned from &6{name-color}{player}'s &cparty."),

    FFA_ADDED_PLAYER("messages/global-messages.yml", "ffa.added-player", "&a&lADDED! &6{name-color}{player} &7&l» &6FFA {ffa-name}"),
    FFA_KICKED_PLAYER("messages/global-messages.yml", "ffa.kicked-player", "&c&lKICKED! &6{name-color}{player} &7&l» &6FFA {ffa-name}"),
    FFA_SPAWN_ENTERED("messages/global-messages.yml", "ffa.entered-spawn", "&aYou have entered the FFA spawn."),
    FFA_SPAWN_LEFT("messages/global-messages.yml", "ffa.left-spawn", "&aYou have left the FFA spawn."),

    FFA_ALREADY_EXISTS("messages/global-messages.yml", "ffa.error.already-exists", "&cAn FFA Match named &6{ffa-name} &calready exists!"),
    FFA_NOT_FOUND("messages/global-messages.yml", "ffa.error.not-found", "&cAn FFA Match named &6{ffa-name} &cdoes not exist!"),
    FFA_DISABLED("messages/global-messages.yml", "ffa.error.disabled", "&cFFA mode is disabled for the kit &6{kit-name}&c!"),
    FFA_KIT_NOT_ELIGIBLE("messages/global-messages.yml", "ffa.error.kit-not-eligible", "&cThis kit is not eligible for FFA matches! Please disable the &6BUILD/BOXING SETTINGS &cand try again."),
    FFA_CAN_ONLY_SETUP_IN_FFA_ARENA("messages/global-messages.yml", "ffa.error.can-only-setup-in-ffa-arena", "&cYou can only setup FFA matches in FFA arenas!"),
    FFA_FULL("messages/global-messages.yml", "ffa.error.ffa-full", "&cThis FFA Match is full!"),
    FFA_INVALID_SPAWN_TYPE("messages/global-messages.yml", "ffa.error.invalid-spawn-type", "&cInvalid spawn type! Valid types: pos1, pos2"),

    FFA_MATCH_CREATED("messages/global-messages.yml", "ffa.command.created", "&aSuccessfully created a new FFA Match for the kit &6{kit-name}&a!"),
    FFA_TOGGLED("messages/global-messages.yml", "ffa.command.toggled", "&aFFA mode has been &6{status} &afor kit &6{kit-name}&a!"),
    FFA_ARENA_SET("messages/global-messages.yml", "ffa.command.arena-set", "&aSuccessfully set the arena of the FFA Match for kit &6{kit-name} &ato &6{arena-name}&a!"),
    FFA_SAFE_ZONE_SET("messages/global-messages.yml", "ffa.command.safe-zone-set", "&aSuccessfully set the &6{pos} &asafezone of the FFA Match for kit &6{kit-name}&a!"),
    FFA_MAX_PLAYERS_SET("messages/global-messages.yml", "ffa.command.max-players-set", "&aSuccessfully set the max players of the FFA Match for kit &6{kit-name} &ato &6{max-players}&a!"),
    FFA_SPAWN_SET("messages/global-messages.yml", "ffa.command.spawn-set", "&aFFA spawn position has been set for &6{arena-name}&a!"),

    FFA_KITS_RELOADED("messages/global-messages.yml", "ffa.command.kits-reloaded", "&aSuccessfully reloaded all FFA kits!"),

    HOTBAR_NOT_FOUND("messages/global-messages.yml", "hotbar.error.not-found", "&cThe hotbar item named &e{hotbar-name} &cdoes not exist."),
    HOTBAR_NO_HOTBAR_ITEMS_CREATED("messages/global-messages.yml", "hotbar.error.no-hotbar-items-created", "&cNo hotbar items have been created yet."),

    HOTBAR_CREATED_ITEM("messages/global-messages.yml", "hotbar.command.created", "&aYou have created a new hotbar item named &e{hotbar-name}&a."),
    HOTBAR_DELETED_ITEM("messages/global-messages.yml", "hotbar.command.deleted", "&aYou have deleted the hotbar item named &e{hotbar-name}&a."),

    ITEM_GIVEN("messages/global-messages.yml", "item.command.given-item", "&aYou have been given &6{amount} &a{item-name}&a."),
    ITEM_NOT_CONFIGURED("messages/global-messages.yml", "item.error.item-not-configured", "&cThe item named &6{item-name} &cis not configured."),

    ITEM_ENCHANTED("messages/global-messages.yml", "item.command.enchanted-item", "&aYou have enchanted your &6{item-name} &awith &6{enchantment} &alevel &6{level}&a."),

    JOIN_MESSAGE_CHAT_ENABLED("messages/global-messages.yml", "join-message.enabled", true),
    JOIN_MESSAGE_CHAT_MESSAGE_LIST("messages/global-messages.yml", "join-message.message", Arrays.asList(
            "",
            "&6&lAlley Practice Core",
            " &6&l│ &rWebsite: &6revere.no",
            " &6&l│ &rDiscord: &6discord.gg/p3R5qhfWAx",
            " &6&l│ &rGitHub: &6github.com/revere-group/alley-practice",
            "",
            "&6&lMade by &f{author} &7(v{version})",
            ""
    )),

    KIT_NOT_FOUND("messages/global-messages.yml", "kit.error.not-found", "&cThere is no kit with that name!"), //TODO: add {kit-name} placeholder
    KIT_ALREADY_EXISTS("messages/global-messages.yml", "kit.error.already-exists", "&cA kit with that name already exists!"), //TODO: add {kit-name} placeholder
    KIT_SLOT_MUST_BE_NUMBER("messages/global-messages.yml", "kit.error.slot-must-be-number", "&cThe slot must be a number!"),

    KIT_POTION_EFFECT_REMOVED("messages/global-messages.yml", "kit.command.potion-effect-removed", "&cYou have removed the potion effect: &6{potion-effect} &cfrom the kit: &6{kit-name}&c."),
    KIT_RAIDING_ROLE_KIT_REMOVED("messages/global-messages.yml", "kit.command.raiding-role-kit-removed", "&aSuccessfully removed the &6{role} &araiding role kit from &6{role-kit-name}&a for the &6{kit-name} &akit."),

    KIT_INVENTORY_GIVEN("messages/global-messages.yml", "kit.command.inventory-given", "&aSuccessfully retrieved the inventory of the &6{kit-name} &akit!"),
    KIT_INVENTORY_SET("messages/global-messages.yml", "kit.command.inventory-set", "&aSuccessfully set the inventory of the &6{kit-name} &akit!"),
    KIT_FFA_SLOT_SET("messages/global-messages.yml", "kit.command.ffaslot-set", "&aSuccessfully set the FFA slot of the &6{kit-name} &akit to &6{slot}&a!"),
    KIT_DESCRIPTION_SET("messages/global-messages.yml", "kit.command.description-set", "&aSuccessfully set the description of the &6{kit-name} &akit: &r{description}"),
    KIT_DESCRIPTION_CLEARED("messages/global-messages.yml", "kit.command.description-cleared", "&aSuccessfully cleared the description of the &6{kit-name} &akit!"),
    KIT_DISCLAIMER_SET("messages/global-messages.yml", "kit.command.disclaimer-set", "&aSuccessfully set the disclaimer of the &6{kit-name} &akit: &r{disclaimer}"),
    KIT_DISPLAYNAME_SET("messages/global-messages.yml", "kit.command.displayname-set", "&aSuccessfully set the display name of the &6{kit-name} &akit: &r{display-name}"),
    KIT_ICON_SET("messages/global-messages.yml", "kit.command.icon-set", "&aSuccessfully set the icon of the &6{kit-name} &akit to &6{icon}&a!"),
    KIT_CATEGORY_SET("messages/global-messages.yml", "kit.command.category-set", "&aSuccessfully set the category of the &6{kit-name} &akit to &6{category}&a!"),
    KIT_MENU_TITLE_SET("messages/global-messages.yml", "kit.command.menu-title-set", "&aSuccessfully set the menu title of the &6{kit-name} &akit: &r{title}"),
    KIT_KB_PROFILE_SET("messages/global-messages.yml", "kit.command.kb-profile-set", "&aSuccessfully set the knockback profile of the &6{kit-name} &akit to &6{kb-profile}&a!"),
    KIT_SET_EDITABLE("messages/global-messages.yml", "kit.command.set-editable", "&aSuccessfully set the editable status of the &6{kit-name} &akit to &6{editable}&a!"),
    KIT_SET_RAIDING_ROLE_KIT("messages/global-messages.yml", "kit.command.set-raiding-role-kit", "&aSuccessfully set the &6{role} &araiding role kit to &6{role-kit} &afor the &6{kit-name} &akit."),
    KIT_CANNOT_SET_ENABLED_AS_RAIDING_ROLE_KIT("messages/global-messages.yml", "kit.error.cannot-set-enabled-as-raiding-role-kit", "&cThe &6{role-kit} &ckit is currently enabled. Please disable it before setting it as a raiding role kit."),
    KIT_NEED_TO_DISABLE_TO_SET_RAIDING_ROLE("messages/global-messages.yml", "kit.error.need-to-disable-to-set-raiding-role-kit", "&cYou need to disable the &6{kit-name} &ckit before setting a raiding role kit."),
    KIT_RAIDING_ROLE_KIT_NOT_MAPPED("messages/global-messages.yml", "kit.error.raiding-role-kit-not-present", "&cThe &6{kit-name} &ckit does not have a raiding kit mapped for the &6{role} &crole."),

    KIT_SETTING_NOT_ENABLED("messages/global-messages.yml", "kit.error.setting-not-enabled", "&cThe setting &6{setting-name} &cis not enabled for the kit &6{kit-name}&c."),

    KIT_POTION_EFFECTS_SET("messages/global-messages.yml", "kit.command.potion-effects-set", "&aSuccessfully set the potion effects of the &6{kit-name} &akit!"),
    KIT_POTION_EFFECTS_CLEARED("messages/global-messages.yml", "kit.command.potion-effects-cleared", "&aSuccessfully cleared the potion effects of the &6{kit-name} &akit!"),
    KIT_CREATED("messages/global-messages.yml", "kit.command.created", "&aSuccessfully created a new kit named &6{kit-name}&a!"),
    KIT_DELETED("messages/global-messages.yml", "kit.command.deleted", "&cSuccessfully deleted the kit named &6{kit-name}&c!"),
    KIT_SETTING_SET("messages/global-messages.yml", "kit.command.setting-set", "&aSuccessfully set the setting &6{setting-name} &ato &6{enabled} &afor the kit &6{kit-name}&a."),
    KIT_SAVED("messages/global-messages.yml", "kit.command.saved", "&aSuccessfully saved the &6{kit-name} &akit!"),
    KIT_SAVED_ALL("messages/global-messages.yml", "kit.command.saved-all", "&aSuccessfully saved all kits!"),

    LEVEL_NOT_FOUND("messages/global-messages.yml", "level.error.not-found", "&cA level named &6{level-name} &cdoes not exist!"),
    LEVEL_ALREADY_EXISTS("messages/global-messages.yml", "level.error.already-exists", "&cA level named &6{level-name} &calready exists!"),
    LEVEL_MAX_ELO_MUST_BE_GREATER_THAN_MIN("messages/global-messages.yml", "level.error.max-elo-must-be-greater-than-min", "&cMaximum Elo must be greater than minimum Elo! (Minimum: &6{min-elo}&c)"),
    LEVEL_MINIMUM_ELO_CANNOT_BE_NEGATIVE("messages/global-messages.yml", "level.error.min-elo-cannot-be-negative", "&cMinimum Elo cannot be negative!"),
    LEVEL_MINIMUM_ELO_MUST_BE_LESS_THAN_MAXIMUM("messages/global-messages.yml", "level.error.min-elo-must-be-less-than-maximum", "&cMinimum Elo must be less than the maximum Elo! (Maximum: &6{max-elo}&c)"),

    LEVEL_DISPLAY_NAME_SET("messages/global-messages.yml", "level.command.display-name-set", "&aSuccessfully set the display name of the &6{level-name} &alevel to &r{display-name}&a!"),
    LEVEL_ICON_SET("messages/global-messages.yml", "level.command.icon-set", "&aSuccessfully set the icon for the level &6{level-name} &ato &6{icon-material}&a!"),
    LEVEL_MINIMUM_ELO_SET("messages/global-messages.yml", "level.command.min-elo-set", "&aSuccessfully set the minimum Elo of the &6{level-name} &alevel to &6{min-elo}&a!"),
    LEVEL_MAX_ELO_SET("messages/global-messages.yml", "level.command.max-elo-set", "&aSuccessfully set the maximum Elo of the &6{level-name} &alevel to &6{max-elo}&a!"),
    LEVEL_CREATED("messages/global-messages.yml", "level.command.created", "&aSuccessfully created a new level named &6{level-name} &awith min Elo &6{min-elo} &aand max Elo &6{max-elo}&a!"),
    LEVEL_DELETED("messages/global-messages.yml", "level.command.deleted", "&cSuccessfully deleted the level named &6{level-name}&c!"),

    MATCH_CANCELLED_FOR_PLAYER("messages/global-messages.yml", "match.cancelled", "&aYou have ended the match for &6{name-color}{player}&a."),
    MATCH_COMMAND_BLOCKED("messages/global-messages.yml", "match.error.command-blocked", "&cYou cannot use that command whilst in a match!"),

    MUSIC_DISC_DESELECTED("messages/global-messages.yml", "music-disc.deselected", "&cYou have removed &6{disc} &cfrom your music selection."),
    MUSIC_DISC_SELECTED("messages/global-messages.yml", "music-disc.selected", "&aYou have added &6{disc} &ato your music selection."),
    MUSIC_DISC_NOW_PLAYING("messages/global-messages.yml", "music-disc.now-playing", Collections.singletonList("&7[&6♬&7] &fNow playing: &6{disc} &7({duration})")),

    OTHER_SUDO_ALL_PLAYERS("messages/global-messages.yml", "other.sudo.all-players", "&aYou have sudo-ed all players to say: &r{message}"),

    PARTY_DISBANDED("messages/global-messages.yml", "party.disbanded", "&6&lParty &7&l" + Symbol.ARROW_R + " &6{name-color}{player} &cdisbanded the party."),
    PARTY_YOU_JOINED("messages/global-messages.yml", "party.joined",
            Arrays.asList(
                    "",
                    "&6&lParty Joined &a" + Symbol.TICK,
                    " &7You joined &6{name-color}{leader}'s &aparty.",
                    " &7Type /p for help.",
                    ""
            )
    ),
    PARTY_YOU_LEFT("messages/global-messages.yml", "party.left", "&cYou've left the party!"),

    PARTY_PLAYER_JOINED("messages/global-messages.yml", "party.player-joined", "&6{name-color}{player} &ahas joined the party! &7({current-size}/{max-size})"),
    PARTY_PLAYER_LEFT("messages/global-messages.yml", "party.player-left", "&6{name-color}{player} &cleft the party. &7({current-size}/{max-size})"),

    PARTY_PLAYER_KICKED("messages/global-messages.yml", "party.player-kicked", "&6{name-color}{player} &chas been kicked from the party. &7({current-size}/{max-size})"),
    PARTY_PLAYER_BANNED("messages/global-messages.yml", "party.player-banned", "&6{name-color}{player} &chas been banned from the party. &7({current-size}/{max-size})"),

    PARTY_CREATED("messages/global-messages.yml", "party.created",
            Arrays.asList(
                    "",
                    "&6&lParty Created &a" + Symbol.TICK,
                    " &7Type /p for help.",
                    ""
            )
    ),
    PARTY_LOOKUP("messages/global-messages.yml", "party.lookup", Arrays.asList(
            "",
            " &6&l{leader}'s Party",
            "  &6&l│ &rLeader: &6{name-color}{leader}",
            "  &6&l│ &rMembers: &6{members}",
            "  &6&l│ &rStatus: &6{status}",
            "  &6&l│ &rPrivacy: &6{privacy}",
            ""
    )),
    PARTY_INFO_NO_MEMBERS_FORMAT("messages/global-messages.yml", "party.info.no-members-format", "&cNo Members"),
    PARTY_INFO("messages/global-messages.yml", "party.info.format", Arrays.asList(
            "",
            " &6&lParty Info",
            "  &6&l│ &rLeader: &6{name-color}{leader}",
            "  &6&l│ &rMembers &7({members-amount})&f: &6{members}",
            "  &6&l│ &rPrivacy: &6{privacy}",
            "  &6&l│ &rSize: &6{size}",
            ""
    )),

    PROFILE_TOGGLED_PARTY_INVITES("messages/global-messages.yml", "profile-messages.player-settings.party-invites", "&aYou've {status} &aparty invites."),
    PROFILE_TOGGLED_PARTY_MESSAGES("messages/global-messages.yml", "profile-messages.player-settings.party-messages", "&aYou've {status} &aparty messages."),
    PROFILE_TOGGLED_SCOREBOARD("messages/global-messages.yml", "profile-messages.player-settings.scoreboard", "&aYou've {status} &athe scoreboard."),
    PROFILE_TOGGLED_SCOREBOARD_LINES("messages/global-messages.yml", "profile-messages.player-settings.scoreboard-lines", "&aYou've {status} &athe scoreboard lines."),
    PROFILE_TOGGLED_TAB_LIST("messages/global-messages.yml", "profile-messages.player-settings.tab-list", "&aYou've {status} &athe tablist."),
    PROFILE_TOGGLED_PROFANITY_FILTER("messages/global-messages.yml", "profile-messages.player-settings.profanity-filter", "&aYou've {status} &athe profanity filter."),
    PROFILE_TOGGLED_DUEL_REQUESTS("messages/global-messages.yml", "profile-messages.player-settings.duel-requests", "&aYou've {status} &areceiving duel requests."),
    PROFILE_TOGGLED_LOBBY_MUSIC("messages/global-messages.yml", "profile-messages.player-settings.lobby-music", "&aYou've {status} &alobby music."),
    PROFILE_TOGGLED_SERVER_TITLES("messages/global-messages.yml", "profile-messages.player-settings.server-titles", "&aYou've {status} &aserver titles."),
    PROFILE_WORLD_TIME_SET("messages/global-messages.yml", "profile-messages.world-time-set", "&aSuccessfully set your personal world time to &6{time}&a."),
    PROFILE_WORLD_TIME_RESET("messages/global-messages.yml", "profile-messages.world-time-reset", "&aSuccessfully reset your personal world time to the server's time."),

    QUEUE_TEMPORARILY_DISABLED("messages/global-messages.yml", "queue.error.temporarily-disabled", "&cQueueing is temporarily disabled. Please try again later."),
    QUEUE_TOGGLED("messages/global-messages.yml", "queue.command.toggled", "&aYou've temporarily {status} &aqueueing for all players."),
    QUEUE_RELOADED("messages/global-messages.yml", "queue.command.reloaded", "&aSuccessfully reloaded all queues!"),
    QUEUE_FORCED_PLAYER("messages/global-messages.yml", "queue.command.forced-player", "&aSuccessfully forced &6{player} &ainto the &6{ranked} {kit} &aqueue!"),

    QUEUE_PROGRESSING_UNRANKED_BOOLEAN("messages/global-messages.yml", "queue.joined.progressing.unranked.enabled", true),
    QUEUE_PROGRESSING_UNRANKED("messages/global-messages.yml", "queue.joined.progressing.unranked.format", Arrays.asList(
            "",
            "&6&l{kit}",
            " &6&l│ &fPing Range: &6N/A",
            "  &7&oSearching for match...",
            ""
    )),

    QUEUE_PROGRESSING_RANKED_BOOLEAN("messages/global-messages.yml", "queue.joined.progressing.ranked.enabled", true),
    QUEUE_PROGRESSING_RANKED("messages/global-messages.yml", "queue.joined.progressing.ranked.format", Arrays.asList(
            "",
            "&6&l{kit} &6&l" + Symbol.RANKED_STAR + "Ranked",
            " &6&l│ &fELO Range: &6{min-elo} &7&l" + Symbol.ARROW_R + " &6{max-elo}",
            " &6&l│ &fPing Range: &6N/A",
            "  &7&oSearching for match...",
            ""
    )),

    QUEUE_PROGRESSING_RANKED_LIMIT_REACHED_BOOLEAN("messages/global-messages.yml", "queue.joined.progressing.ranked.limit-reached.enabled", true),
    QUEUE_PROGRESSING_RANKED_LIMIT_REACHED("messages/global-messages.yml", "queue.joined.progressing.ranked.limit-reached.format", Arrays.asList(
            "",
            "&6&l{kit} &6&l" + Symbol.RANKED_STAR + "Ranked",
            " &6&l│ &fPing Range: &6N/A",
            "  &c&lRANGE LIMIT REACHED...",
            ""
    )),

    QUEUE_JOINED_BOOLEAN("messages/global-messages.yml", "queue.join-message.enabled", true),
    QUEUE_JOINED("messages/global-messages.yml", "queue.join-message.format", Arrays.asList(
            "",
            "&6&lQUEUE JOINED &a" + Symbol.TICK,
            " &6&l│ &rKit: &6{kit}",
            " &6&l│ &rType: &6{queue-type}",
            "  &7&oType /leavequeue to leave.",
            ""
    )),

    QUEUE_LEFT_BOOLEAN("messages/global-messages.yml", "queue.leave-message.enabled", true),
    QUEUE_LEFT("messages/global-messages.yml", "queue.leave-message.format", Arrays.asList(
            "",
            "&6&lQUEUE LEFT &c" + Symbol.CROSS,
            " &6&l│ &rYou left the &6{queue-type} &6{kit} &fqueue.",
            ""
    )),

    RANKED_PLAYER_NOT_BANNED("messages/global-messages.yml", "ranked.error.player-not-banned", "&c{name-color}{player} &cis not banned from ranked matches!"),
    RANKED_PLAYER_ALREADY_BANNED("messages/global-messages.yml", "ranked.error.player-already-banned", "&c{name-color}{player} &cis already banned from ranked matches!"),

    RANKED_PLAYER_BAN_BROADCAST_BOOLEAN("messages/global-messages.yml", "ranked.ban-broadcast.enabled", true),
    RANKED_PLAYER_BAN_BROADCAST("messages/global-messages.yml", "ranked.ban-broadcast.format", Collections.singletonList("&c&l{name-color}{player} &7has been banned from ranked matches.")),

    RANKED_BAN_MESSAGE_NOTICE_BOOLEAN("messages/global-messages.yml", "ranked.ban-message-notice.enabled", true),
    RANKED_BAN_MESSAGE_NOTICE("messages/global-messages.yml", "ranked.ban-message-notice.format", Arrays.asList(
            "",
            "&c&lRANKED BANNED",
            " &c&l│ &fReason: &c{reason}",
            " &c&l│ &fDuration: &c{duration}",
            " &c&l│ &fBan ID: &c{ban-id}",
            ""
    )),

    RANKED_PLAYER_UNBAN_BROADCAST_BOOLEAN("messages/global-messages.yml", "ranked.unban-broadcast.enabled", true),
    RANKED_PLAYER_UNBAN_BROADCAST("messages/global-messages.yml", "ranked.unban-broadcast.format", Collections.singletonList("&a&l{name-color}{player} &7has been unbanned from ranked matches.")),

    RANKED_UNBAN_MESSAGE_NOTICE_BOOLEAN("messages/global-messages.yml", "ranked.unban-message-notice.enabled", true),
    RANKED_UNBAN_MESSAGE_NOTICE("messages/global-messages.yml", "ranked.unban-message-notice.format", Arrays.asList(
            "",
            "&a&lRANKED UNBANNED",
            " &a&l│ &fReason: &a{reason}",
            " &a&l│ &fBan ID: &a{ban-id}",
            " &a(You can queue ranked again)",
            ""
    )),

    SPAWN_SET("messages/global-messages.yml", "spawn.command.set", "&aSuccessfully set the new spawn location of &6Alley Practice&a! \n &8- &7{world}: {x}, {y}, {z} (Yaw: {yaw}, Pitch: {pitch})"),
    SPAWN_TELEPORTED("messages/global-messages.yml", "spawn.command.teleported", "&6Teleported you to spawn!"),
    SPAWN_ITEMS_GIVEN("messages/global-messages.yml", "spawn.command.items-given", "&aSuccessfully received the spawn items!"),

    SNAPSHOT_INVENTORY_EXPIRED("messages/global-messages.yml", "snapshot.error.inventory-expired", "&cThis inventory has expired."),

    TIPS_LIST("messages/global-messages.yml", "tips", Arrays.asList(
            "&6Tip: &fUse F5 to look at your opponent one last time before they end you.",
            "&6Tip: &fW-tap like your life depends on it. It kinda does.",
            "&6Tip: &fKeep your crosshair at head level... unless you like tickling toes.",
            "&6Tip: &fPractice spacing — or just hug them and pray."
    )),

    TROLL_PLAYER_DONUTED("messages/global-messages.yml", "troll.player-donuted", "&aYou have successfully donut'd &6{name-color}{player}&a!"),
    TROLL_PLAYER_FAKE_EXPLODED("messages/global-messages.yml", "troll.player-fake-exploded", "&aYou have fake exploded &6{name-color}{player}&a!"),
    TROLL_PLAYER_GIVEN_HEART_ATTACK("messages/global-messages.yml", "troll.player-given-heart-attack", "&aYou have given &6{name-color}{player} &aa heart attack!"),
    TROLL_PLAYER_LAUNCHED("messages/global-messages.yml", "troll.player-launched", "&aYou have launched &6{name-color}{player}&a into the sky!"),
    TROLL_PLAYER_PUSHED("messages/global-messages.yml", "troll.player-pushed", "&aYou have pushed &6{name-color}{player}&a!"),
    TROLL_PLAYER_STRUCK_BY_LIGHTNING("messages/global-messages.yml", "troll.player-struck-by-lightning", "&aYou have struck &6{name-color}{player} &awith lightning!"),
    TROLL_PLAYER_DEMO_MENU_OPENED("messages/global-messages.yml", "troll.player-demo-menu-opened", "&aYou have opened the demo menu for &6{name-color}{player}&a!"),

    ;

    private final String configName;
    private final String configPath;
    private final Object defaultValue;

    /**
     * Constructor for the MessagesLocale enum.
     *
     * @param configName   The name of the configuration file.
     * @param configPath   The path to the specific string within the configuration file.
     * @param defaultValue The default value for the locale entry.
     */
    GlobalMessagesLocaleImpl(String configName, String configPath, Object defaultValue) {
        this.configName = configName;
        this.configPath = configPath;
        this.defaultValue = defaultValue;
    }
}