package dev.revere.alley.feature.tournament.task;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.tournament.TournamentService;
import dev.revere.alley.feature.tournament.model.Tournament;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;
import dev.revere.alley.feature.tournament.model.TournamentState;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Remi
 * @project alley-practice
 * @date 6/08/2025
 */
public class TournamentBroadcastTask implements Runnable {
    private final Tournament tournament;

    public TournamentBroadcastTask(Tournament tournament) {
        this.tournament = tournament;
    }

    @Override
    public void run() {
        if (tournament.getState() != TournamentState.WAITING) {
            tournament.getBroadcastTask().cancel();
            return;
        }

        TextComponent title = new TextComponent(CC.translate("&6&lTournament"));
        TextComponent typeComponent = new TextComponent(CC.translate("&6&l│ &fType: &6" + tournament.getDisplayName() + " " + tournament.getKit().getDisplayName()));
        TextComponent hostComponent = new TextComponent(CC.translate("&6&l│ &fHosted By: &6" + tournament.getHostName()));

        int currentPlayers = tournament.getWaitingPool().stream().mapToInt(TournamentParticipant::getSize).sum();
        int maxPlayers = tournament.getMaxTeams() * tournament.getTeamSize();
        TextComponent playersComponent = new TextComponent(CC.translate("&6&l│ &fPlayers: &6" + currentPlayers + "/" + maxPlayers));

        StringBuilder hoverText = new StringBuilder(CC.translate("&6&lJoined Players:\n"));
        List<String> playerNames = tournament.getWaitingPool().stream()
                .flatMap(p -> p.getOnlinePlayers().stream())
                .map(Player::getName)
                .collect(Collectors.toList());

        if (playerNames.isEmpty()) {
            hoverText.append(CC.translate("&7No one has joined yet."));
        } else {
            hoverText.append(CC.translate("&e" + String.join(", ", playerNames)));
        }
        playersComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent(hoverText.toString())}));

        TextComponent joinComponent = new TextComponent(CC.translate("&a&l[Click to Join]"));
        joinComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent(CC.translate("&aClick here to join the tournament!"))}));
        joinComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tournament join " + tournament.getNumericId()));

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage("");
            player.spigot().sendMessage(title);
            player.spigot().sendMessage(typeComponent);
            player.spigot().sendMessage(hostComponent);
            player.spigot().sendMessage(playersComponent);
            player.spigot().sendMessage(joinComponent);
            player.sendMessage("");
        }
    }
}