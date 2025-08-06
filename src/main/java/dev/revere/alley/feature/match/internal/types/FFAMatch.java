package dev.revere.alley.feature.match.internal.types;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.ListenerUtil;
import dev.revere.alley.common.PlayerUtil;
import dev.revere.alley.common.reflect.ReflectionService;
import dev.revere.alley.common.reflect.internal.types.TitleReflectionServiceImpl;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.config.ConfigService;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.setting.types.mechanic.KitSettingDropItemsImpl;
import dev.revere.alley.feature.match.Match;
import dev.revere.alley.feature.match.model.GameParticipant;
import dev.revere.alley.feature.match.model.internal.MatchGamePlayer;
import dev.revere.alley.feature.match.snapshot.Snapshot;
import dev.revere.alley.feature.queue.Queue;
import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Emmy
 * @project Alley
 * @since 25/04/2025
 */
@Getter
public class FFAMatch extends Match {
    private final List<GameParticipant<MatchGamePlayer>> participants;
    private GameParticipant<MatchGamePlayer> winningParticipant;

    /**
     * Constructor for the MatchFFAImpl class.
     *
     * @param queue        The queue associated with this match.
     * @param kit          The kit used in this match.
     * @param arena        The arena where the match takes place.
     * @param participants The list of participants in the match.
     */
    public FFAMatch(Queue queue, Kit kit, Arena arena, List<GameParticipant<MatchGamePlayer>> participants) {
        super(queue, kit, arena, false);
        this.participants = new ArrayList<>(participants);
        setTeamMatch(false);
        setAffectStatistics(false);
    }

    @Override
    public void setupPlayer(Player player) {
        super.setupPlayer(player);

        Location spawn = this.getArena().getPos1();
        player.teleport(spawn);
    }

    @Override
    public void sendPlayerVersusPlayerMessage() {
        String prefix = CC.translate("&7[&6Match&7] &r");
        String message = CC.translate(prefix + "&6A chaotic fight between &f" + this.getParticipants().size() + " &6players is about to begin!");
        sendMessage(message);
    }

    @Override
    public void handleRespawn(Player player) {
        player.spigot().respawn();
        PlayerUtil.reset(player, false, true);
    }

    @Override
    public List<GameParticipant<MatchGamePlayer>> getParticipants() {
        return this.participants;
    }

    @Override
    public void handleDisconnect(Player player) {
        MatchGamePlayer gamePlayer = this.getGamePlayer(player);
        if (gamePlayer != null) {
            gamePlayer.setDead(true);
            gamePlayer.setEliminated(true);
            gamePlayer.setDisconnected(true);

            if (this.canEndMatch()) {
                this.checkForConclusion(player, null);
            }
        }
    }

    @Override
    public void handleDeathItemDrop(Player player, PlayerDeathEvent event) {
        if (this.getKit().isSettingEnabled(KitSettingDropItemsImpl.class)) {
            ListenerUtil.clearDroppedItemsOnDeath(event, player);
        } else {
            event.getDrops().clear();
        }
    }

    @Override
    public void handleDeath(Player player, EntityDamageEvent.DamageCause cause) {
        AlleyPlugin.getInstance().getService(ReflectionService.class).getReflectionService(TitleReflectionServiceImpl.class).sendTitle(
                player,
                "&c&lDEFEAT!",
                "&fYou have been eliminated!"
        );
        super.handleDeath(player, cause);
    }

    @Override
    public void handleParticipant(Player player, MatchGamePlayer gamePlayer) {
        gamePlayer.setDead(true);
        gamePlayer.setEliminated(true);
    }

    @Override
    public void handleRoundEnd() {
        setWinningParticipant();
        broadcastFFAMatchOutcome();

        if (this.winningParticipant != null) {
            MatchGamePlayer winner = this.winningParticipant.getLeader();
            AlleyPlugin.getInstance().getService(ReflectionService.class).getReflectionService(TitleReflectionServiceImpl.class).sendTitle(
                    winner.getTeamPlayer(),
                    "&a&lVICTORY!",
                    "&fYou are the last player standing!"
            );
        }

        if (!this.getSpectators().isEmpty()) {
            this.broadcastAndStopSpectating();
        }

        super.handleRoundEnd();
    }

    @Override
    public boolean canEndRound() {
        long aliveCount = this.participants.stream()
                .filter(participant -> !participant.isAllDead() && !participant.getAllPlayers().stream().allMatch(MatchGamePlayer::isDisconnected))
                .count();
        return aliveCount <= 1;
    }

    @Override
    public boolean canStartRound() {
        return participants.stream().noneMatch(GameParticipant::isAllDead);
    }

    @Override
    public boolean canEndMatch() {
        return this.canEndRound();
    }

    @Override
    protected boolean handleSpectator(Player player, Profile profile, GameParticipant<MatchGamePlayer> participant) {
        return true;
    }

    @Override
    public boolean isInSameTeam(Player attacker, Player victim) {
        return false;
    }

    public MatchGamePlayer getFromAllGamePlayers(OfflinePlayer player) {
        if (player == null) return null;
        return this.getParticipants().stream()
                .map(GameParticipant::getAllPlayers)
                .flatMap(List::stream)
                .filter(gamePlayer -> gamePlayer.getUuid().equals(player.getUniqueId()))
                .findFirst()
                .orElse(null);
    }


    private void broadcastFFAMatchOutcome() {
        ConfigService configService = AlleyPlugin.getInstance().getService(ConfigService.class);
        FileConfiguration messagesConfig = configService.getMessagesConfig();
        String path = "match.ended.match-result.ffa.";

        List<String> header = messagesConfig.getStringList(path + "format.header");
        String winnerFormat = messagesConfig.getString(path + "format.winner", " &a&lWinner: &f{player}");
        String secondPlaceFormat = messagesConfig.getString(path + "format.second_place", " &e2nd Place: &f{player}");
        String thirdPlaceFormat = messagesConfig.getString(path + "format.third_place", " &63rd Place: &f{player}");
        String otherLosersLineFormat = messagesConfig.getString(path + "format.other_losers_line", " &7Other Players: &f{losers_list}");
        String otherLosersSeparator = messagesConfig.getString(path + "format.other_losers_separator", "&7, &f");
        List<String> footer = messagesConfig.getStringList(path + "format.footer");
        String playerCommand = messagesConfig.getString(path + "player.command", "/inventory {player}");
        String playerHover = messagesConfig.getString(path + "player.hover", "&eClick to view {player}'s inventory");

        header.forEach(this::sendMessage);

        if (this.winningParticipant != null) {
            MatchGamePlayer winner = this.winningParticipant.getLeader();
            sendComponentMessage(createPlayerLineComponent(winner, winnerFormat, playerCommand, playerHover));
        }

        List<Snapshot> loserSnapshots = getSnapshots().stream()
                .filter(s -> s.getHealth() == 0)
                .collect(Collectors.toList());

        Collections.reverse(loserSnapshots);

        if (!loserSnapshots.isEmpty()) {
            MatchGamePlayer player = getFromAllGamePlayers(Bukkit.getOfflinePlayer(loserSnapshots.get(0).getUuid()));
            if (player != null) {
                sendComponentMessage(createPlayerLineComponent(player, secondPlaceFormat, playerCommand, playerHover));
            }
        }

        if (loserSnapshots.size() >= 2) {
            MatchGamePlayer player = getFromAllGamePlayers(Bukkit.getOfflinePlayer(loserSnapshots.get(1).getUuid()));
            if (player != null) {
                sendComponentMessage(createPlayerLineComponent(player, thirdPlaceFormat, playerCommand, playerHover));
            }
        }

        if (loserSnapshots.size() > 2) {
            List<MatchGamePlayer> otherPlayers = loserSnapshots.subList(2, loserSnapshots.size()).stream()
                    .map(snapshot -> getFromAllGamePlayers(Bukkit.getOfflinePlayer(snapshot.getUuid())))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!otherPlayers.isEmpty()) {
                String[] parts = otherLosersLineFormat.split("\\{losers}");
                TextComponent line = new TextComponent(CC.translate(parts[0]));

                for (int i = 0; i < otherPlayers.size(); i++) {
                    line.addExtra(createPlayerNameComponent(otherPlayers.get(i), playerCommand, playerHover));
                    if (i < otherPlayers.size() - 1) {
                        line.addExtra(new TextComponent(CC.translate(otherLosersSeparator)));
                    }
                }

                if (parts.length > 1) line.addExtra(new TextComponent(CC.translate(parts[1])));
                sendComponentMessage(line);
            }
        }

        footer.forEach(this::sendMessage);
    }

    private TextComponent createPlayerLineComponent(MatchGamePlayer gamePlayer, String format, String commandTemplate, String hoverTemplate) {
        String playerName = gamePlayer.getUsername();
        String command = commandTemplate.replace("{player}", playerName);
        String hover = hoverTemplate.replace("{player}", playerName);

        String[] parts = format.split("\\{player}");
        TextComponent lineComponent = new TextComponent(CC.translate(parts[0]));

        TextComponent nameComponent = new TextComponent(playerName);
        nameComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        nameComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(CC.translate(hover)).create()));

        lineComponent.addExtra(nameComponent);

        if (parts.length > 1) {
            lineComponent.addExtra(new TextComponent(CC.translate(parts[1])));
        }

        return lineComponent;
    }

    private TextComponent createPlayerNameComponent(MatchGamePlayer gamePlayer, String commandTemplate, String hoverTemplate) {
        String playerName = gamePlayer.getUsername();
        String command = commandTemplate.replace("{player}", playerName);
        String hover = hoverTemplate.replace("{player}", playerName);

        TextComponent nameComponent = new TextComponent(playerName);
        nameComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        nameComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(CC.translate(hover)).create()));

        return nameComponent;
    }

    private void setWinningParticipant() {
        this.winningParticipant = this.participants.stream()
                .filter(participant -> !participant.isAllDead() && !participant.getAllPlayers().stream().allMatch(MatchGamePlayer::isDisconnected))
                .findFirst()
                .orElse(null);
    }
}
