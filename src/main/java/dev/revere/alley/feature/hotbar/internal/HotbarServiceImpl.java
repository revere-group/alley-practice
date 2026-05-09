package dev.revere.alley.feature.hotbar.internal;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.bootstrap.AlleyContext;
import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.logger.Logger;
import dev.revere.alley.common.reflect.utility.ReflectionUtility;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.config.ConfigService;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.menu.setting.PracticeSettingsMenu;
import dev.revere.alley.feature.host.menu.HostMenu;
import dev.revere.alley.feature.hotbar.HotbarAction;
import dev.revere.alley.feature.hotbar.HotbarItem;
import dev.revere.alley.feature.hotbar.HotbarService;
import dev.revere.alley.feature.hotbar.HotbarType;
import dev.revere.alley.feature.hotbar.data.HotbarActionData;
import dev.revere.alley.feature.hotbar.data.HotbarTypeData;
import dev.revere.alley.feature.layout.LayoutService;
import dev.revere.alley.feature.leaderboard.menu.LeaderboardMenu;
import dev.revere.alley.feature.match.menu.CurrentMatchesMenu;
import dev.revere.alley.feature.match.menu.SpectatorTeleportMenu;
import dev.revere.alley.feature.party.menu.duel.PartyDuelMenu;
import dev.revere.alley.feature.party.menu.event.PartyEventMenu;
import dev.revere.alley.feature.queue.QueueService;
import dev.revere.alley.feature.queue.QueueType;
import dev.revere.alley.feature.queue.menu.sub.RankedMenu;
import dev.revere.alley.library.menu.Menu;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Remi
 * @project Alley
 * @date 5/27/2024
 */
@Getter
@Service(provides = HotbarService.class, priority = 190)
public class HotbarServiceImpl implements HotbarService {
    private final ProfileService profileService;
    private final ConfigService configService;
    private final LayoutService layoutService;
    private final QueueService queueService;

    private final List<HotbarItem> hotbarItems = new ArrayList<>();

    /**
     * DI Constructor for the HotbarService class.
     *
     * @param profileService The profile service to manage player profiles.
     * @param configService  The configuration service to manage hotbar configurations.
     * @param queueService   The Queue service instance.
     * @param layoutService  The Layout service instance.
     */
    public HotbarServiceImpl(ProfileService profileService, ConfigService configService, LayoutService layoutService, QueueService queueService) {
        this.profileService = profileService;
        this.configService = configService;
        this.layoutService = layoutService;
        this.queueService = queueService;
    }

    @Override
    public void initialize(AlleyContext context) {
        FileConfiguration hotbarConfig = this.configService.getHotbarConfig();
        ConfigurationSection hotbarSection = hotbarConfig.getConfigurationSection("hotbar-items");
        if (hotbarSection == null) {
            Logger.error("Hotbar items section is missing in the hotbar configuration file.");
            return;
        }

        for (String key : hotbarSection.getKeys(false)) {
            ConfigurationSection itemSection = hotbarSection.getConfigurationSection(key);
            if (itemSection == null) continue;

            String displayName = itemSection.getString("display-name", "&fNULL");
            displayName = CC.translate(displayName);

            List<String> lore = itemSection.getStringList("lore");
            if (lore == null) {
                lore = Collections.singletonList(CC.translate("&f" + key + " has not been configured properly."));
            } else {
                lore = lore.stream().map(CC::translate).collect(Collectors.toList());
            }

            Material material = Material.matchMaterial(itemSection.getString("material", "STONE").toUpperCase());
            int durability = itemSection.getInt("durability", 0);

            List<HotbarTypeData> typeData = new ArrayList<>();

            ConfigurationSection typesSection = itemSection.getConfigurationSection("types");
            if (typesSection == null) {
                Logger.error("Types section is missing for hotbar item. Keep in mind that the types section is required for hotbar items to work properly.");
            } else {
                for (String typeKey : typesSection.getKeys(false)) {
                    HotbarType type = HotbarType.valueOf(typeKey.toUpperCase());
                    int slot = typesSection.getInt(typeKey + ".slot", -1);
                    HotbarTypeData hotbarTypeData = new HotbarTypeData(type, slot);
                    hotbarTypeData.setEnabled(typesSection.getBoolean(typeKey + ".enabled", false));
                    typeData.add(hotbarTypeData);
                }
            }

            String command = itemSection.getString("command");
            HotbarAction action = command.isEmpty() ? HotbarAction.OPEN_MENU : HotbarAction.RUN_COMMAND;
            HotbarActionData actionData = new HotbarActionData(action);

            if (action == HotbarAction.RUN_COMMAND) {
                actionData.setCommand(command);
            } else {
                String menuName = itemSection.getString("menu");
                if (menuName != null && !menuName.isEmpty()) {
                    try {
                        actionData.setMenuName(menuName);
                    } catch (Exception exception) {
                        Logger.error("Failed to set menu for hotbar item: " + key + ". Menu: " + menuName + " does not exist or is not properly configured.");
                    }
                } else {
                    Logger.error("Menu name is missing for hotbar item: " + key);
                }
            }

            HotbarItem hotbarItem = this.createHotbarItem(key, displayName, lore, material, durability, typeData, actionData);
            this.hotbarItems.add(hotbarItem);
        }
    }

    /**
     * Creates a HotbarItem with the specified parameters.
     *
     * @param name        The name of the hotbar item.
     * @param displayName The display name of the hotbar item.
     * @param lore        The lore of the hotbar item.
     * @param material    The material of the hotbar item.
     * @param durability  The durability of the hotbar item.
     * @param typeData    The type data for the hotbar item.
     * @param actionData  The action data for the hotbar item.
     * @return A new HotbarItem instance.
     */
    private HotbarItem createHotbarItem(String name, String displayName, List<String> lore, Material material, int durability, List<HotbarTypeData> typeData, HotbarActionData actionData) {
        HotbarItem hotbarItem = new HotbarItem(name);
        hotbarItem.setDisplayName(displayName);
        hotbarItem.setLore(lore);
        hotbarItem.setMaterial(material);
        hotbarItem.setDurability(durability);
        hotbarItem.setTypeData(typeData);
        hotbarItem.setActionData(actionData);
        return hotbarItem;
    }

    /**
     * Builds an ItemStack for a HotbarItem that can be received by players.
     *
     * @param hotbarItem The HotbarItem to build the ItemStack for.
     * @return An ItemStack representing the HotbarItem.
     */
    @Override
    public ItemStack buildReceivableItem(HotbarItem hotbarItem) {
        ItemStack itemStack = new ItemBuilder(hotbarItem.getMaterial())
                .name(hotbarItem.getDisplayName())
                .lore(hotbarItem.getLore())
                .durability(hotbarItem.getDurability())
                .build();

        itemStack = ReflectionUtility.setUnbreakable(itemStack, true);
        return itemStack;
    }

    @Override
    public void applyHotbarItems(Player player, HotbarType type) {
        player.getInventory().clear();

        List<HotbarItem> itemsToApply = this.getItemsForType(type);
        if (itemsToApply == null) return;

        for (HotbarItem item : itemsToApply) {
            ItemStack itemStack = this.buildReceivableItem(item);
            player.getInventory().setItem(item.getTypeData().get(type.ordinal()).getSlot(), itemStack);
        }

        AlleyPlugin.getInstance().getServer().getScheduler().runTaskLater(AlleyPlugin.getInstance(), player::updateInventory, 1L);
    }

    @Override
    public void applyHotbarItems(Player player) {
        Profile profile = this.profileService.getProfile(player.getUniqueId());
        this.applyHotbarItems(player, this.getCorrespondingType(profile));
    }

    @Override
    public void createHotbarItem(String name, HotbarType type) {
        if (this.hotbarItems.stream().anyMatch(item -> item.getName().equalsIgnoreCase(name))) {
            Logger.warn("A hotbar item with the name '" + name + "' already exists.");
            return;
        }

        HotbarItem hotbarItem = new HotbarItem(name);
        hotbarItem.getTypeData().stream()
                .filter(typeData -> typeData.getType() == type)
                .findFirst()
                .ifPresent(typeData -> typeData.setEnabled(true));

        this.hotbarItems.add(hotbarItem);
        this.saveToConfig(hotbarItem);
    }

    @Override
    public void deleteHotbarItem(HotbarItem hotbarItem) {
        FileConfiguration hotbarConfig = this.configService.getHotbarConfig();
        File hotbarFile = this.configService.getConfigFile("storage/hotbar.yml");

        ConfigurationSection hotbarSection = hotbarConfig.getConfigurationSection("hotbar-items");
        if (hotbarSection != null) {
            hotbarSection.set(hotbarItem.getName(), null);
            this.hotbarItems.remove(hotbarItem);
        } else {
            Logger.error("Hotbar items section is missing in the hotbar configuration file.");
        }

        this.configService.saveConfig(hotbarFile, hotbarConfig);
    }


    @Override
    public void saveToConfig(HotbarItem hotbarItem) {
        FileConfiguration hotbarConfig = this.configService.getHotbarConfig();
        File hotbarFile = this.configService.getConfigFile("storage/hotbar.yml");

        ConfigurationSection hotbarSection = hotbarConfig.getConfigurationSection("hotbar-items");
        if (hotbarSection == null) {
            hotbarSection = hotbarConfig.createSection("hotbar-items");
        }

        ConfigurationSection itemSection = hotbarSection.createSection(hotbarItem.getName());
        itemSection.set("display-name", hotbarItem.getDisplayName());
        itemSection.set("lore", hotbarItem.getLore());
        itemSection.set("material", hotbarItem.getMaterial().name());
        itemSection.set("durability", hotbarItem.getDurability());

        ConfigurationSection typesSection = itemSection.createSection("types");
        for (HotbarTypeData typeData : hotbarItem.getTypeData()) {
            ConfigurationSection typeSection = typesSection.createSection(typeData.getType().name().toLowerCase());
            typeSection.set("slot", typeData.getSlot());
            typeSection.set("enabled", typeData.isEnabled());
        }

        if (hotbarItem.getActionData() != null) {
            itemSection.set("command", hotbarItem.getActionData().getCommand());
            itemSection.set("menu", hotbarItem.getActionData().getMenuName());
        }

        this.configService.saveConfig(hotbarFile, hotbarConfig);
    }

    @Override
    public List<HotbarItem> getItemsForType(HotbarType type) {
        return this.hotbarItems.stream()
                .filter(item -> item.getTypeData().stream().anyMatch(data -> data.getType() == type && data.isEnabled()))
                .collect(Collectors.toList());
    }

    @Override
    public HotbarType getCorrespondingType(Profile profile) {
        HotbarType type;

        switch (profile.getState()) {
            case WAITING:
                type = HotbarType.QUEUE;
                break;
            case SPECTATING:
                type = HotbarType.SPECTATOR;
                break;
            case LOBBY:
                return (profile.getParty() != null) ? HotbarType.PARTY : HotbarType.LOBBY;
            case TOURNAMENT_LOBBY:
                type = HotbarType.TOURNAMENT;
                break;
            default:
                type = HotbarType.LOBBY;
                break;
        }

        return type;
    }

    @Override
    public HotbarItem getHotbarItem(ItemStack itemStack, HotbarType type) {
        if (itemStack == null || !itemStack.hasItemMeta() || !itemStack.getItemMeta().hasDisplayName()) {
            return null;
        }

        List<HotbarItem> items = this.getItemsForType(type);
        return items.stream()
                .filter(hotbarItem -> hotbarItem.getDisplayName().equals(itemStack.getItemMeta().getDisplayName()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public HotbarItem getHotbarItem(String name) {
        return this.hotbarItems.stream().filter(hotbarItem -> hotbarItem.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @Override
    public Menu getMenuInstanceFromName(String name, Player player) {
        Profile profile = this.profileService.getProfile(player.getUniqueId());
        switch (name) {
            case "UNRANKED_MENU":
                return this.queueService.getQueueMenu();
            case "LAYOUT_EDITOR_MENU":
                return this.layoutService.getLayoutMenu();
            case "CURRENT_MATCHES_MENU":
                return new CurrentMatchesMenu();
            case "SETTINGS_MENU":
                return new PracticeSettingsMenu();
            case "HOST_EVENTS_MENU":
                return new HostMenu();
            case "LEADERBOARD_MENU":
                return new LeaderboardMenu();
            case "PARTY_EVENT_MENU":
                return new PartyEventMenu();
            case "PARTY_DUEL_MENU":
                return new PartyDuelMenu();
            case "SPECTATOR_TELEPORTER_MENU":
                return new SpectatorTeleportMenu(profile.getMatch());
            case "RANKED_MENU":
                return new RankedMenu();
            case "UNRANKED_DUO_MENU":
                profile.setQueueType(QueueType.DUOS);
                return this.queueService.getQueueMenu();
            default:
                throw new IllegalArgumentException("Unknown menu type: " + name);
        }
    }
}