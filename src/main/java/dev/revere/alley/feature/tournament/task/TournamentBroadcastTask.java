package dev.revere.alley.feature.tournament.task;

import dev.revere.alley.common.text.CC;
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
    private long lastProgressBroadcast = 0L;

    /**
     * Creates a new broadcast task for the given tournament.
     *
     * @param tournament The tournament to broadcast.
     */
    public TournamentBroadcastTask(Tournament tournament) {
        this.tournament = tournament;
    }

    /**
     * Renders the broadcast card for all players across tournament phases.
     * Cancels only when the tournament ends. After starting, broadcasts are
     * throttled to once every 30 seconds to avoid spam.
     */
    @Override
    public void run() {
        TournamentState state = tournament.getState();

        if (state == TournamentState.ENDED) {
            if (tournament.getBroadcastTask() != null) {
                tournament.getBroadcastTask().cancel();
            }
            return;
        }

        if (state == TournamentState.IN_PROGRESS || state == TournamentState.INTERMISSION) {
            long now = System.currentTimeMillis();
            if ((now - lastProgressBroadcast) < 30_000L) {
                return;
            }
            lastProgressBroadcast = now;
        }

        TextComponent title =
                new TextComponent(
                        CC.translate("&6Tournament &7(" + tournament.getDisplayName() + ")"));

        TextComponent typeComponent =
                new TextComponent(
                        CC.translate("&6│ &fKit: &6" + tournament.getKit().getDisplayName()));

        TextComponent hostComponent =
                new TextComponent(
                        CC.translate("&6│ &fHosted By: &6" + tournament.getHostName()));

        int currentPlayers =
                (state == TournamentState.WAITING || state == TournamentState.STARTING)
                        ? tournament.getWaitingPool().stream()
                        .mapToInt(TournamentParticipant::getSize)
                        .sum()
                        : tournament.getRoundParticipants().stream()
                        .mapToInt(TournamentParticipant::getSize)
                        .sum();

        int maxPlayers = tournament.getMaxTeams() * tournament.getTeamSize();
        TextComponent playersComponent =
                new TextComponent(
                        CC.translate("&6│ &fPlayers: &6" + currentPlayers + "/" + maxPlayers));

        String status;
        switch (state) {
            case STARTING:
                status = " &7Starting...";
                break;
            case IN_PROGRESS:
                status = " &7In Progress...";
                break;
            case INTERMISSION:
                status = " &7Waiting for next round...";
                break;
            default:
                status = " &7Waiting for players...";
        }
        TextComponent statusComponent = new TextComponent(CC.translate(status));

        StringBuilder hoverText = new StringBuilder(CC.translate("&6&lJoined Players:\n"));
        List<String> playerNames = tournament.getWaitingPool().stream()
                .flatMap(participant -> participant.getOnlinePlayers().stream())
                .map(Player::getName)
                .collect(Collectors.toList());

        if (playerNames.isEmpty()) {
            hoverText.append(CC.translate("&7No one has joined yet."));
        } else {
            hoverText.append(CC.translate("&e" + String.join(", ", playerNames)));
        }

        playersComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{
                new TextComponent(hoverText.toString())
        }));

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            boolean alreadyJoined = tournament.getWaitingPool().stream()
                    .anyMatch(participant -> participant.containsPlayer(viewer.getUniqueId()));

            TextComponent actionComponent;
            if (state == TournamentState.WAITING || state == TournamentState.STARTING) {
                actionComponent =
                        new TextComponent(
                                CC.translate(alreadyJoined ? " &a&lJOINED" : " &a&l(Click to Join)"));

                if (alreadyJoined) {
                    actionComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{
                            new TextComponent(CC.translate("&aYou have already joined this tournament."))
                    }));
                } else {
                    actionComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{
                            new TextComponent(CC.translate("&aClick here to join the tournament!"))
                    }));
                    actionComponent.setClickEvent(new ClickEvent(
                            ClickEvent.Action.RUN_COMMAND, "/tournament join " + tournament.getNumericId()));
                }
            } else {
                actionComponent = new TextComponent(CC.translate(" &a&l(Click to View)"));
                actionComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{
                        new TextComponent(CC.translate("&aClick to view the tournament status"))
                }));
                actionComponent.setClickEvent(new ClickEvent(
                        ClickEvent.Action.RUN_COMMAND, "/tournament info " + tournament.getNumericId()));
            }

            viewer.sendMessage("");
            viewer.spigot().sendMessage(title);
            viewer.spigot().sendMessage(typeComponent);
            viewer.spigot().sendMessage(hostComponent);
            viewer.spigot().sendMessage(playersComponent);
            viewer.spigot().sendMessage(statusComponent);
            viewer.spigot().sendMessage(actionComponent);
            viewer.sendMessage("");
        }
    }
}