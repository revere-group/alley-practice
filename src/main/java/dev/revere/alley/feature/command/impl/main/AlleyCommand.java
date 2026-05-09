package dev.revere.alley.feature.command.impl.main;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.common.text.ClickableUtil;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import dev.revere.alley.library.command.annotation.CompleterData;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Emmy
 * @project Alley
 * @date 19/04/2024 - 17:39
 */
public class AlleyCommand extends BaseCommand {
    @CompleterData(name = "alley")
    public List<String> alleyCompleter(CommandArgs command) {
        List<String> completion = new ArrayList<>();
        if (command.getArgs().length == 1 && command.getPlayer().hasPermission(this.getAdminPermission())) {
            completion.addAll(Arrays.asList(
                    "reload", "debug", "core"
            ));
        }

        return completion;
    }

    @CommandData(
            name = "alley",
            aliases = {"apractice", "aprac", "practice", "prac", "hamza", "emmy", "remi", "revere"},
            inGameOnly = false,
            usage = "alley",
            description = "Displays information about the plugin."
    )
    @Override
    public void onCommand(CommandArgs command) {
        CommandSender sender = command.getSender();

        Arrays.asList(
                "",
                "     &6&lAlley Practice",
                "      &6&l│ &fCreated by: &6Emmy &7(github.com/hmEmmy)",
                "      &6&l│ &fMaintained by: &6Revere Group. &7(github.com/revere-group)",
                "      &6&l│ &fPrimary Contributors: &6" + this.plugin.getDescription().getAuthors().toString().replace("[", "").replace("]", "").replace(",", "&7,&6"),
                "",
                "      &6&l│ &fLicense: &6CC BY-NC-SA 4.0",
                "      &6&l│ &fVersion: &6" + this.plugin.getDescription().getVersion(),
                ""
        ).forEach(line -> sender.sendMessage(CC.translate(line)));

        if (sender instanceof Player) {
            TextComponent clickable = this.createLinkComponent();
            command.getPlayer().spigot().sendMessage(clickable);
            sender.sendMessage("");
        }
    }

    private @NotNull TextComponent createLinkComponent() {
        TextComponent repositoryComponent = ClickableUtil.createLinkComponent("&f&l[GITHUB]", "https://github.com/hmEmmy/alley-practice", "&aClick to open the GitHub repository.");
        TextComponent discordComponent = ClickableUtil.createLinkComponent("&9&l[DISCORD]", "https://discord.gg/bEsW59UBG8", "&aClick to join the Revere Discord.");
        TextComponent builtByBitComponent = ClickableUtil.createLinkComponent("&b&l[BUILTBYBIT]", "https://builtbybit.com/resources/alley-next-generation-practice-core.73088/", "&aClick to open the BuiltByBit resource page.");
        TextComponent spigotMcComponent = ClickableUtil.createLinkComponent("&e&l[SPIGOTMC]", "https://www.spigotmc.org/resources/alley-next-generation-practice-core.127500/", "&aClick to open the SpigotMC resource page.");

        String SPACING = "  ";

        TextComponent clickable = new TextComponent("     ");
        clickable.addExtra(repositoryComponent);
        clickable.addExtra(SPACING);
        clickable.addExtra(discordComponent);
        clickable.addExtra(SPACING);
        clickable.addExtra(builtByBitComponent);
        clickable.addExtra(SPACING);
        clickable.addExtra(spigotMcComponent);
        return clickable;
    }
}