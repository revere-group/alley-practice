package dev.revere.alley.feature.party.command;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;

/**
 * @author Emmy
 * @project Alley
 * @date 22/05/2024 - 20:32
 */
public class PartyCommand extends BaseCommand {
    @CommandData(
            name = "party",
            aliases = "p",
            usage = "party",
            description = "Sends a list of party commands."
    )
    @Override
    public void onCommand(CommandArgs command) {

        //TODO: make it page based

        Player player = command.getPlayer();
        player.sendMessage("");
        player.sendMessage(CC.translate("&6&lParty Commands Help:"));
        player.sendMessage(CC.translate(" &6│ &6/party create &7| Create a party"));
        player.sendMessage(CC.translate(" &6│ &6/party disband &7| Disband a party"));
        player.sendMessage(CC.translate(" &6│ &6/party leave &7| Leave a party"));
        player.sendMessage(CC.translate(" &6│ &6/party join &8(&7player&8) &7| Join a public party"));
        player.sendMessage(CC.translate(" &6│ &6/party info &7| Get information about your party"));
        player.sendMessage(CC.translate(" &6│ &6/party chat &8(&7message&8) &7| Chat with your party"));
        player.sendMessage(CC.translate(" &6│ &6/party accept &8(&7player&8) &7| Accept a party invite"));
        player.sendMessage(CC.translate(" &6│ &6/party invite &8(&7player&8) &7| Invite a player to your party"));
        player.sendMessage(CC.translate(" &6│ &6/party kick &8(&7player&8) &7| Kick a player out of your party"));
        player.sendMessage(CC.translate(" &6│ &6/party open &7| Open your party to the public"));
        player.sendMessage(CC.translate(" &6│ &6/party close &7| Close your party to the public"));
        player.sendMessage(CC.translate(" &6│ &6/party ban &8(&7player&8) &7| Ban a player from your party"));
        player.sendMessage(CC.translate(" &6│ &6/party unban &8(&7player&8) &7| Unban a player from your party"));
        player.sendMessage(CC.translate(" &6│ &6/party banlist &7| List all banned players in your party"));
        player.sendMessage(CC.translate(" &6│ &6/party announce &8(&7message&8) &7| Public invitation to your party"));
        player.sendMessage(CC.translate(" &6│ &6/party lookup &8(&7player&8) &7| Lookup a player's party"));
        player.sendMessage("");
    }
}