package dev.revere.alley.feature.tournament.model;

import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.match.Match;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author Remi
 * @project alley-practice
 * @date 6/08/2025
 */
@Getter
public class Tournament {
    private final UUID tournamentId;
    private final int numericId;
    private final Kit kit;
    private final String displayName;
    private final int teamSize;
    private final int maxTeams;
    private final int minTeams;
    private final String hostName;

    @Setter private TournamentState state;
    @Setter private int currentRound = 0;
    @Setter private BukkitTask startingTask;
    @Setter private BukkitTask broadcastTask;
    @Setter private BukkitTask inactivityTask;
    @Setter private BukkitTask roundStartTask;

    private final List<TournamentParticipant> waitingPool = new CopyOnWriteArrayList<>();
    private final List<TournamentParticipant> participants = new CopyOnWriteArrayList<>();
    private final List<TournamentParticipant> roundParticipants = new CopyOnWriteArrayList<>();
    private final List<Match> activeMatches = new CopyOnWriteArrayList<>();

    public Tournament(int numericId, String hostName, Kit kit, String displayName, int teamSize, int maxTeams, int minTeams) {
        this.tournamentId = UUID.randomUUID();
        this.numericId = numericId;
        this.hostName = hostName;
        this.kit = kit;
        this.displayName = displayName;
        this.teamSize = teamSize;
        this.maxTeams = maxTeams;
        this.minTeams = minTeams;
        this.state = TournamentState.WAITING;
    }
    public void addToWaitingPool(TournamentParticipant participant) {
        if (state == TournamentState.WAITING) {
            waitingPool.add(participant);
        }
    }

    public void addFinalizedParticipant(TournamentParticipant participant) {
        participants.add(participant);
        roundParticipants.add(participant);
    }

    public void removeParticipant(TournamentParticipant participant) {
        waitingPool.remove(participant);
        participants.remove(participant);
        roundParticipants.remove(participant);
    }

    public void addActiveMatch(Match match) {
        activeMatches.add(match);
    }

    public void removeActiveMatch(Match match) {
        activeMatches.remove(match);
    }

    public List<Player> getAllPlayers() {
        List<TournamentParticipant> sourceList = (state == TournamentState.WAITING || state == TournamentState.STARTING) ? waitingPool : participants;
        return sourceList.stream()
                .flatMap(p -> p.getOnlinePlayers().stream())
                .collect(Collectors.toList());
    }

    public void broadcast(String message) {
        getAllPlayers().forEach(player -> player.sendMessage(message));
    }
}