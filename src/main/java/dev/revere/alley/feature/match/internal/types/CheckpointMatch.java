package dev.revere.alley.feature.match.internal.types;

import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.queue.Queue;
import dev.revere.alley.feature.match.model.internal.MatchGamePlayer;
import dev.revere.alley.feature.match.model.GameParticipant;
import dev.revere.alley.common.PlayerUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * @author Remi
 * @project alley-practice
 * @date 25/06/2025
 */
public class CheckpointMatch extends DefaultMatch {

    /**
     * Constructor for the MatchRegularImpl class.
     *
     * @param queue        The queue of the match.
     * @param kit          The kit of the match.
     * @param arena        The arena of the match.
     * @param ranked       Whether the match is ranked or not.
     * @param participantA The first participant.
     * @param participantB The second participant.
     */
    public CheckpointMatch(Queue queue, Kit kit, Arena arena, boolean ranked, GameParticipant<MatchGamePlayer> participantA, GameParticipant<MatchGamePlayer> participantB) {
        super(queue, kit, arena, ranked, participantA, participantB);
    }

    @Override
    public boolean canEndRound() {
        return ((this.getParticipantA().isLostCheckpoint() && this.getParticipantA().isAllDead()) || (this.getParticipantB().isLostCheckpoint() && this.getParticipantB().isAllDead()))
                || (this.getParticipantA().getAllPlayers().stream().allMatch(MatchGamePlayer::isDisconnected)
                || this.getParticipantB().getAllPlayers().stream().allMatch(MatchGamePlayer::isDisconnected));
    }

    @Override
    public void handleRespawn(Player player) {
        PlayerUtil.reset(player, true, true);

        MatchGamePlayer gamePlayer = this.getGamePlayer(player);

        Location checkpoint = gamePlayer.getCheckpoint();
        if (checkpoint == null) {
            checkpoint = this.getParticipantA().containsPlayer(player.getUniqueId()) ? getArena().getPos1() : getArena().getPos2();
        }

        player.teleport(checkpoint);

        this.giveLoadout(player, this.getKit());
        this.applyColorKit(player);
    }
}
