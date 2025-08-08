package dev.revere.alley.library.menu;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.PlayerUtil;
import dev.revere.alley.common.constants.PluginConstant;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.locale.LocaleEntry;
import dev.revere.alley.core.locale.LocaleService;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.enums.ProfileState;
import dev.revere.alley.feature.hotbar.HotbarService;
import dev.revere.alley.library.menu.impl.PageGlassButton;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public abstract class Menu {
    protected AlleyPlugin plugin = AlleyPlugin.getInstance();

    public static Map<String, Menu> currentlyOpenedMenus = new HashMap<>();

    @Getter
    private Map<Integer, Button> buttons = new HashMap<>();

    private boolean autoUpdate = false;
    private boolean updateAfterClick = true;
    private boolean closedByMenu = false;
    private boolean placeholder = false;

    private Button placeholderButton = Button.placeholder(Material.STAINED_GLASS_PANE, (byte) 15, " ");

    private ItemStack createItemStack(Player player, Button button) {
        ItemStack item = button.getButtonItem(player);

        if (item.getType() != Material.SKULL_ITEM) {
            ItemMeta meta = item.getItemMeta();

            if (meta != null && meta.hasDisplayName()) {
                meta.setDisplayName(meta.getDisplayName() + "§b§c§d§e");
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    public void openMenu(final Player player) {
        this.buttons = this.getButtons(player);

        Inventory inventory;
        int size = this.getSize() == -1 ? this.size(this.buttons) : this.getSize();
        String title = CC.translate(this.getTitle(player));

        if (title.length() > 32) {
            title = title.substring(0, 32);
        }

        inventory = Bukkit.createInventory(player, size, title);

        inventory.setContents(new ItemStack[inventory.getSize()]);

        for (Map.Entry<Integer, Button> buttonEntry : this.buttons.entrySet()) {
            inventory.setItem(buttonEntry.getKey(), createItemStack(player, buttonEntry.getValue()));
        }

        if (this.isPlaceholder()) {
            for (int index = 0; index < size; index++) {
                if (this.buttons.get(index) == null) {
                    this.buttons.put(index, this.placeholderButton);
                    inventory.setItem(index, this.placeholderButton.getButtonItem(player));
                }
            }
        }

        player.openInventory(inventory);

        currentlyOpenedMenus.put(player.getName(), this);

        this.onOpen(player);
    }

    public int size(Map<Integer, Button> buttons) {
        int highest = 0;

        for (int buttonValue : buttons.keySet()) {
            if (buttonValue > highest) {
                highest = buttonValue;
            }
        }

        return (int) (Math.ceil((highest + 1) / 9D) * 9D);
    }

    /**
     * Addition by Remi
     * Adds a border to the inventory.
     *
     * @param buttons the buttons
     * @param data    the data
     * @param rows    the rows
     */
    public void addBorder(Map<Integer, Button> buttons, int data, int rows) {
        for (int i = 0; i < 9; i++) {
            buttons.putIfAbsent(i, Button.placeholder(Material.STAINED_GLASS_PANE, (byte) data, ""));
        }

        for (int i = 0; i < 9; i++) {
            buttons.putIfAbsent((rows - 1) * 9 + i, Button.placeholder(Material.STAINED_GLASS_PANE, (byte) data, ""));
        }

        for (int i = 0; i < rows; i++) {
            buttons.putIfAbsent(i * 9, Button.placeholder(Material.STAINED_GLASS_PANE, (byte) data, ""));
            buttons.putIfAbsent(i * 9 + 8, Button.placeholder(Material.STAINED_GLASS_PANE, (byte) data, ""));
        }
    }

    /**
     * Addition by Emmy
     * Adds a glass header to the paginated menu.
     *
     * @param buttons the button
     */
    public void addGlassHeader(Map<Integer, Button> buttons, int durability) {
        for (int i = 1; i < 9; i++) {
            if (i != 8) {
                buttons.put(i, new PageGlassButton(durability));
            }
        }
    }

    /**
     * Addition by Emmy
     * Skips the slot if it crosses the border.
     *
     * @param slot the slot
     * @return the new slot
     */
    public int skipIfSlotCrossingBorder(int slot) {
        if (slot == 17 || slot == 26 || slot == 35 || slot == 44 || slot == 53 || slot == 62) {
            slot += 2;
        }
        return slot;
    }

    /**
     * Addition by Emmy
     * Checks if the slot is a border slot.
     *
     * @param slot the slot
     * @return true if it is a border slot, false otherwise
     */
    public boolean isBorderSlot(int slot) {
        return slot == 17 || slot == 26 || slot == 35 || slot == 44 || slot == 53 || slot == 62;
    }

    /**
     * Addition by Emmy
     * Refills glass to the empty slots of a menu.
     *
     * @param buttons the button
     */
    public void addGlass(Map<Integer, Button> buttons, int durability) {
        for (int slot = 0; slot < getSize(); slot++) {
            if (!buttons.containsKey(slot)) {
                buttons.put(slot, new PageGlassButton(durability));
            }
        }
    }

    public int getSize() {
        return -1;
    }

    public int getSlot(int x, int y) {
        return ((9 * y) + x);
    }

    public abstract String getTitle(Player player);

    public abstract Map<Integer, Button> getButtons(Player player);

    public void onOpen(Player player) {
    }

    public void onClose(Player player) {
        ProfileState profileState = AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(player.getUniqueId()).getState();
        if (profileState == ProfileState.PLAYING
                || profileState == ProfileState.PLAYING_EVENT
                || profileState == ProfileState.TOURNAMENT_LOBBY
                || profileState == ProfileState.FIGHTING_BOT
                || profileState == ProfileState.FFA) {
            return;
        }

        AlleyPlugin.getInstance().getService(HotbarService.class).applyHotbarItems(player);
    }

    /**
     * Either fetches the profile of an online player or retrieves the offline profile.
     *
     * @param target The name of the player.
     * @param sender The command sender.
     * @return The profile of the player, or null if not found.
     */
    public Profile getOfflineProfile(String target, CommandSender sender) {
        OfflinePlayer offlinePlayer = PlayerUtil.getOfflinePlayerByName(target);
        if (offlinePlayer == null) {
            sender.sendMessage(CC.translate("&cThat player does not exist."));
            return null;
        }

        UUID uuid = offlinePlayer.getUniqueId();
        if (uuid == null) {
            sender.sendMessage(CC.translate("&cThat player is invalid."));
            return null;
        }

        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Profile profile = profileService.getProfile(uuid);
        if (profile == null) {
            sender.sendMessage(CC.translate("&cThat player does not have a profile."));
            return null;
        }

        return profile;
    }

    /**
     * Fetches the profile of a player by their UUID.
     *
     * @param uuid The UUID of the player.
     * @return The profile of the player, or null if not found.
     */
    public Profile getProfile(UUID uuid) {
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        return profileService.getProfile(uuid);
    }

    /**
     * Gets the admin permission prefix for the bootstrap.
     *
     * @return The admin permission prefix.
     */
    public String getAdminPermission() {
        return this.plugin.getService(PluginConstant.class).getAdminPermissionPrefix();
    }

    /**
     * Gets the locale service.
     *
     * @return The locale service.
     */
    private LocaleService getLocaleService() {
        return this.plugin.getService(LocaleService.class);
    }

    /**
     * Fetches a localized message from the locale service.
     *
     * @param entry The locale entry.
     * @return The localized message.
     */
    public String getString(LocaleEntry entry) {
        return this.getLocaleService().getString(entry);
    }

    /**
     * Fetches a localized list of messages from the locale service.
     *
     * @param entry The locale entry.
     * @return The localized list of messages.
     */
    public List<String> getStringList(LocaleEntry entry) {
        return this.getLocaleService().getStringList(entry);
    }

    /**
     * Fetches a boolean value from the locale service.
     *
     * @param entry The locale entry.
     * @return The boolean value.
     */
    public boolean getBoolean(LocaleEntry entry) {
        return this.getLocaleService().getBoolean(entry);
    }

    /**
     * Fetches an integer value from the locale service.
     *
     * @param entry The locale entry.
     * @return The integer value.
     */
    public int getInt(LocaleEntry entry) {
        return this.getLocaleService().getInt(entry);
    }

    /**
     * Fetches a double value from the locale service.
     *
     * @param entry The locale entry.
     * @return The double value.
     */
    public double getDouble(LocaleEntry entry) {
        return this.getLocaleService().getDouble(entry);
    }
}