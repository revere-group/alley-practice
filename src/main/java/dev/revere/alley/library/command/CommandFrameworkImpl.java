package dev.revere.alley.library.command;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.bootstrap.AlleyContext;
import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.common.constants.PluginConstant;
import dev.revere.alley.common.logger.Logger;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.locale.LocaleService;
import dev.revere.alley.core.locale.internal.impl.SettingsLocaleImpl;
import dev.revere.alley.library.command.annotation.CommandData;
import dev.revere.alley.library.command.annotation.CompleterData;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.help.GenericCommandHelpTopic;
import org.bukkit.help.HelpTopic;
import org.bukkit.help.HelpTopicComparator;
import org.bukkit.help.IndexHelpTopic;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Getter
@Service(provides = CommandFramework.class, priority = 40)
public class CommandFrameworkImpl implements CommandFramework, CommandExecutor {
    private final Map<String, Map.Entry<Method, Object>> commandMap = new HashMap<>();
    private final Map<String, Command> registeredBukkitCommands = new HashMap<>();
    private final Map<String, Map<UUID, Long>> cooldowns = new HashMap<>();

    private CommandMap map;

    private final AlleyPlugin plugin;
    private final PluginConstant pluginConstant;
    private final LocaleService localeService;

    /**
     * DI Constructor for the CommandFrameworkImpl class.
     *
     * @param plugin         the AlleyPlugin instance.
     * @param pluginConstant the Plugin constant.
     * @param localeService  the Locale service.
     */
    public CommandFrameworkImpl(AlleyPlugin plugin, PluginConstant pluginConstant, LocaleService localeService) {
        this.plugin = plugin;
        this.pluginConstant = pluginConstant;
        this.localeService = localeService;
    }

    @Override
    public void setup(AlleyContext context) {
        if (plugin.getServer().getPluginManager() instanceof SimplePluginManager) {
            try {
                SimplePluginManager manager = (SimplePluginManager) plugin.getServer().getPluginManager();
                Field field = SimplePluginManager.class.getDeclaredField("commandMap");
                field.setAccessible(true);
                this.map = (CommandMap) field.get(manager);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to initialize CommandFramework: Could not get CommandMap.", e);
            }
        }
    }

    @Override
    public void initialize(AlleyContext context) {
        ScanResult scanResult = context.getScanResult();
        if (scanResult == null) {
            Logger.error("CommandFramework cannot initialize: ScanResult from context is null.");
            return;
        }

        for (ClassInfo classInfo : scanResult.getSubclasses(BaseCommand.class.getName())) {
            if (classInfo.isAbstract() || classInfo.isInterface()) {
                continue;
            }

            try {
                Object instance = classInfo.loadClass().getDeclaredConstructor().newInstance();
                registerCommands(instance);
            } catch (Exception e) {
                Logger.logException("Failed to instantiate and register command container: " + classInfo.getName(), e);
            }
        }

        registerHelp();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return handleCommand(sender, cmd, label, args);
    }

    @SuppressWarnings("all")
    public boolean handleCommand(CommandSender sender, Command cmd, String label, String[] args) {
        for (int i = args.length; i >= 0; i--) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(label.toLowerCase());
            for (int x = 0; x < i; x++) {
                buffer.append(".").append(args[x].toLowerCase());
            }
            String cmdLabel = buffer.toString();
            if (this.commandMap.containsKey(cmdLabel)) {
                Method method = this.commandMap.get(cmdLabel).getKey();
                Object methodObject = this.commandMap.get(cmdLabel).getValue();
                CommandData commandData = method.getAnnotation(CommandData.class);

                if (sender instanceof Player && commandData.cooldown() > 0) {
                    Player player = (Player) sender;
                    UUID uuid = player.getUniqueId();
                    long cooldownMillis = commandData.cooldown() * 1000L;

                    this.cooldowns.computeIfAbsent(cmdLabel, k -> new HashMap<>());

                    Map<UUID, Long> commandCooldowns = this.cooldowns.get(cmdLabel);

                    if (commandCooldowns.containsKey(uuid)) {
                        long lastUsed = commandCooldowns.get(uuid);
                        long timeLeft = (lastUsed + cooldownMillis) - System.currentTimeMillis();

                        if (timeLeft > 0) {
                            String message = CC.translate("&cPlease wait " + String.format("%.1f", timeLeft / 1000.0) + " seconds.");
                            player.sendMessage(message);
                            return true;
                        }
                    }

                    commandCooldowns.put(uuid, System.currentTimeMillis());
                }

                String noPermission = pluginConstant.getPermissionLackMessage();
                if (commandData.isAdminOnly() && !sender.hasPermission(pluginConstant.getAdminPermissionPrefix())) {
                    sender.sendMessage(noPermission);
                    return true;
                }

                if (!commandData.permission().isEmpty() && (!sender.hasPermission(commandData.permission()))) {
                    sender.sendMessage(CC.translate(noPermission));
                    return true;
                }
                if (commandData.inGameOnly() && !(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "This command is only performable in game.");
                    return true;
                }

                try {
                    method.invoke(methodObject,
                            new CommandArgs(sender, cmd, label, args, cmdLabel.split("\\.").length - 1));
                } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                    Bukkit.getConsoleSender().sendMessage("Failed to execute command: " + cmdLabel + " - " + e.getMessage());
                    e.printStackTrace();
                }
                return true;
            }
        }
        this.defaultCommand(new CommandArgs(sender, cmd, label, args, 0));
        return true;
    }

    public void registerCommands(Object obj) {
        for (Method m : obj.getClass().getMethods()) {
            if (m.getAnnotation(CommandData.class) != null) {
                CommandData commandData = m.getAnnotation(CommandData.class);
                if (m.getParameterTypes().length > 1 || m.getParameterTypes()[0] != CommandArgs.class) {
                    System.out.println("Unable to register command " + m.getName() + ". Unexpected method arguments");
                    continue;
                }
                registerCommand(commandData, Objects.requireNonNull(commandData).name(), m, obj);
                for (String alias : commandData.aliases()) {
                    registerCommand(commandData, alias, m, obj);
                }
            } else if (m.getAnnotation(CompleterData.class) != null) {
                CompleterData comp = m.getAnnotation(CompleterData.class);
                if (m.getParameterTypes().length != 1
                        || m.getParameterTypes()[0] != CommandArgs.class) {
                    System.out.println(
                            "Unable to register tab completer " + m.getName() + ". Unexpected method arguments");
                    continue;
                }
                if (m.getReturnType() != List.class) {
                    System.out.println("Unable to register tab completer " + m.getName() + ". Unexpected return type");
                    continue;
                }
                registerCompleter(Objects.requireNonNull(comp).name(), m, obj);
                for (String alias : comp.aliases()) {
                    registerCompleter(alias, m, obj);
                }
            }
        }
    }

    public void registerHelp() {
        Set<HelpTopic> help = new TreeSet<>(HelpTopicComparator.helpTopicComparatorInstance());
        for (Command cmd : this.registeredBukkitCommands.values()) {
            HelpTopic topic = new GenericCommandHelpTopic(cmd);
            help.add(topic);
        }
        IndexHelpTopic topic = new IndexHelpTopic(pluginConstant.getName(), "All commands for " + pluginConstant.getName(), null, help,
                "Below is a list of all " + pluginConstant.getName() + " commands:");
        Bukkit.getServer().getHelpMap().addTopic(topic);
    }

    public void unregisterCommands(Object obj) {
        for (Method m : obj.getClass().getMethods()) {
            if (m.getAnnotation(CommandData.class) != null) {
                CommandData commandData = m.getAnnotation(CommandData.class);
                this.commandMap.remove(Objects.requireNonNull(commandData).name().toLowerCase());
                this.commandMap.remove(pluginConstant.getName() + ":" + commandData.name().toLowerCase());
                Command bukkitCmd = registeredBukkitCommands.remove(commandData.name().toLowerCase());
                if (bukkitCmd != null) {
                    bukkitCmd.unregister(map);
                }
            }
        }
    }

    public void registerCommand(CommandData commandData, String label, Method m, Object obj) {
        this.commandMap.put(label.toLowerCase(), new AbstractMap.SimpleEntry<>(m, obj));
        this.commandMap.put(pluginConstant.getName() + ':' + label.toLowerCase(),
                new AbstractMap.SimpleEntry<>(m, obj));
        String cmdLabel = label.replace(".", ",").split(",")[0].toLowerCase();

        if (!registeredBukkitCommands.containsKey(cmdLabel)) {
            Command cmd = new BukkitCommand(cmdLabel, this, plugin);
            map.register(pluginConstant.getName(), cmd);
            registeredBukkitCommands.put(cmdLabel, cmd);
        }

        Command registeredCmd = registeredBukkitCommands.get(cmdLabel);
        if (registeredCmd != null) {
            if (!commandData.description().equalsIgnoreCase("") && cmdLabel.equals(label)) {
                registeredCmd.setDescription(commandData.description());
            }
            if (!commandData.usage().equalsIgnoreCase("") && cmdLabel.equals(label)) {
                registeredCmd.setUsage(commandData.usage());
            }
        }
    }

    public void registerCompleter(String label, Method m, Object obj) {
        String cmdLabel = label.replace(".", ",").split(",")[0].toLowerCase();
        Command command = registeredBukkitCommands.get(cmdLabel);

        if (command == null) {
            command = new BukkitCommand(cmdLabel, this, plugin);
            map.register(pluginConstant.getName(), command);
            registeredBukkitCommands.put(cmdLabel, command);
        }

        if (command instanceof BukkitCommand) {
            BukkitCommand bukkitCommand = (BukkitCommand) command;
            if (bukkitCommand.completer == null) {
                bukkitCommand.completer = new BukkitCompleter();
            }
            bukkitCommand.completer.addCompleter(label, m, obj);
        } else if (command instanceof PluginCommand) {
            try {
                Field field = command.getClass().getDeclaredField("completer");
                field.setAccessible(true);
                if (field.get(command) == null) {
                    BukkitCompleter completer = new BukkitCompleter();
                    completer.addCompleter(label, m, obj);
                    field.set(command, completer);
                } else if (field.get(command) instanceof BukkitCompleter) {
                    BukkitCompleter completer = (BukkitCompleter) field.get(command);
                    completer.addCompleter(label, m, obj);
                } else {
                    System.out.println("Unable to register tab completer " + m.getName()
                            + ". A tab completer is already registered for that command!");
                }
            } catch (Exception exception) {
                System.out.println("Failed to register tab completer " + m.getName() + " for command " + label + ": " + exception.getMessage());
            }
        }
    }

    /**
     * Method to handle colon syntax command execution.
     * Reference: "pluginName:command" is restricted, only users with specific permission can use it.
     *
     * @param args The command arguments.
     */
    private void defaultCommand(CommandArgs args) {
        String label = args.getLabel();
        String[] parts = label.split(":");

        if (args.getSender().hasPermission(this.localeService.getString(SettingsLocaleImpl.PERMISSION_COMMAND_SYNTAX_BYPASS))) {
            if (parts.length > 1) {
                String commandToExecute = parts[1];

                StringBuilder commandBuilder = new StringBuilder(commandToExecute);
                for (String arg : args.getArgs()) {
                    commandBuilder.append(" ").append(arg);
                }
                String command = commandBuilder.toString();

                if (args.getSender() instanceof Player) {
                    args.getPlayer().performCommand(command);
                } else {
                    args.getSender().getServer().dispatchCommand(args.getSender(), command);
                }
            } else {
                args.getSender().sendMessage(CC.translate("&cMissing arguments / Wrong format or Internal error."));
            }
        } else {
            args.getSender().sendMessage(this.localeService.getString(SettingsLocaleImpl.COMMAND_ANTI_SYNTAX_MESSAGE).replace("{argument}", args.getLabel()));
        }
    }
}