package dev.revere.alley.feature.tournament.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author Remi
 * @project alley-practice
 * @date 6/08/2025
 */
@Getter
@EqualsAndHashCode(of = "groupId")
public class TournamentParticipant {
    private final UUID groupId;
    @Setter
    private UUID leaderUuid;
    @Setter private String leaderName;

    private final List<UUID> memberUuids = new CopyOnWriteArrayList<>();

    /**
     * Constructs a new TournamentParticipant for a party of players.
     *
     * @param leader The player designated as the party leader.
     * @param members A list of all members in the party, including the leader.
     */
    public TournamentParticipant(Player leader, List<Player> members) {
        this.groupId = UUID.randomUUID();
        this.leaderUuid = leader.getUniqueId();
        this.leaderName = leader.getName();
        members.forEach(m -> this.memberUuids.add(m.getUniqueId()));
    }

    /**
     * Constructs a new TournamentParticipant for a single, solo player.
     *
     * @param soloPlayer The solo player.
     */
    public TournamentParticipant(Player soloPlayer) {
        this.groupId = UUID.randomUUID();
        this.leaderUuid = soloPlayer.getUniqueId();
        this.leaderName = soloPlayer.getName();
        this.memberUuids.add(soloPlayer.getUniqueId());
    }

    /**
     * Gets a list of all currently online players in this participant group.
     *
     * @return A list of online Player objects.
     */
    public List<Player> getOnlinePlayers() {
        return memberUuids.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Gets the total number of members in this participant group.
     *
     * @return The size of the group.
     */
    public int getSize() {
        return memberUuids.size();
    }

    /**
     * Merges another participant group into this one. This is used for combining
     * smaller groups during team formation.
     *
     * @param other The other participant group to merge.
     */
    public void merge(TournamentParticipant other) {
        other.getMemberUuids().forEach(uuid -> {
            if (!this.memberUuids.contains(uuid)) {
                this.memberUuids.add(uuid);
            }
        });
    }

    /**
     * Checks if a specific player is a member of this group.
     *
     * @param uuid The UUID of the player to check.
     * @return {@code true} if the player is in the group, {@code false} otherwise.
     */
    public boolean containsPlayer(UUID uuid) {
        return memberUuids.contains(uuid);
    }

    /**
     * Removes a player from this group. If the leader leaves, a new leader is promoted.
     *
     * @param memberUuid The UUID of the member to remove.
     */
    public void removeMember(UUID memberUuid) {
        memberUuids.remove(memberUuid);

        boolean wasLeader = this.leaderUuid.equals(memberUuid);

        if (wasLeader && !memberUuids.isEmpty()) {
            UUID newLeaderUuid = memberUuids.get(0);
            Player newLeaderPlayer = Bukkit.getPlayer(newLeaderUuid);

            setLeaderUuid(newLeaderUuid);
            if (newLeaderPlayer != null) {
                setLeaderName(newLeaderPlayer.getName());
                newLeaderPlayer.getName();
            }
        }
    }

    /**
     * Checks if the participant group has any members left.
     *
     * @return {@code true} if the group is empty, {@code false} otherwise.
     */
    public boolean isEmpty() {
        return memberUuids.isEmpty();
    }
}