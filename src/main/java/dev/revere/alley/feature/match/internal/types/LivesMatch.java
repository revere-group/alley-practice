package dev.revere.alley.feature.match.internal.types;

import dev.revere.alley.common.PlayerUtil;
import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.match.model.GameParticipant;
import dev.revere.alley.feature.match.model.MatchGamePlayerData;
import dev.revere.alley.feature.match.model.internal.MatchGamePlayer;
import dev.revere.alley.feature.queue.Queue;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * @author Emmy
 * @project Alley
 * @date 5/21/2024
 */
public class LivesMatch extends DefaultMatch {

    /**
     * Constructor for the MatchLivesImpl class.
     *
     * @param queue        The queue of the match.
     * @param kit          The kit of the match.
     * @param arena        The arena of the match.
     * @param ranked       Whether the match is ranked.
     * @param participantA The first participant.
     * @param participantB The second participant.
     */
    public LivesMatch(Queue queue, Kit kit, Arena arena, boolean ranked, GameParticipant<MatchGamePlayer> participantA, GameParticipant<MatchGamePlayer> participantB) {
        super(queue, kit, arena, ranked, participantA, participantB);
    }

    @Override
    public boolean canStartRound() {
        return getParticipantA().getLeader().getData().getLives() > 0 && getParticipantB().getLeader().getData().getLives() > 0;
    }

    @Override
    public boolean canEndRound() {
        return (getParticipantA().isAllEliminated() || getParticipantB().isAllEliminated())
                || (this.getParticipantA().getAllPlayers().stream().allMatch(MatchGamePlayer::isDisconnected)
                || this.getParticipantB().getAllPlayers().stream().allMatch(MatchGamePlayer::isDisconnected));
    }

    @Override
    public void setupPlayer(Player player) {
        super.setupPlayer(player);

        MatchGamePlayerData data = this.getGamePlayer(player).getData();
        player.setMaxHealth(data.getLives() * 2);
        player.setHealth(player.getMaxHealth());
    }

    /**
     * Reduces the life count of a player in the match.
     *
     * @param player The player whose life is to be reduced.
     * @param data   The MatchGamePlayerData of the player whose life is to be reduced.
     */
    public void reduceLife(Player player, MatchGamePlayerData data) {
        data.setLives(data.getLives() - 1);
        player.setMaxHealth(data.getLives() <= 0 ? 20 : data.getLives() * 2);
        player.setHealth(player.getMaxHealth());
    }

    @Override
    public void handleParticipant(Player player, MatchGamePlayer gamePlayer) {
        MatchGamePlayerData data = this.getGamePlayer(player).getData();
        this.reduceLife(player, data);

        if (data.getLives() <= 0) {
            gamePlayer.setEliminated(true);
        }

        super.handleParticipant(player, gamePlayer);
    }

    @Override
    public void handleRespawn(Player player) {
        PlayerUtil.reset(player, true, false);

        Location spawnLocation = this.getParticipants().get(0).containsPlayer(player.getUniqueId()) ? this.getArena().getPos1() : this.getArena().getPos2();
        player.teleport(spawnLocation);

        this.giveLoadout(player, this.getKit());
        this.applyColorKit(player);
    }
}
