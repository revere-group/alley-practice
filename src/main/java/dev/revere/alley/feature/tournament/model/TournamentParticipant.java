package dev.revere.alley.feature.tournament.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
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

    public TournamentParticipant(Player leader, List<Player> members) {
        this.groupId = UUID.randomUUID();
        this.leaderUuid = leader.getUniqueId();
        this.leaderName = leader.getName();
        members.forEach(m -> this.memberUuids.add(m.getUniqueId()));
    }

    public TournamentParticipant(Player soloPlayer) {
        this.groupId = UUID.randomUUID();
        this.leaderUuid = soloPlayer.getUniqueId();
        this.leaderName = soloPlayer.getName();
        this.memberUuids.add(soloPlayer.getUniqueId());
    }

    public List<Player> getOnlinePlayers() {
        return memberUuids.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public int getSize() {
        return memberUuids.size();
    }

    public void merge(TournamentParticipant other) {
        other.getMemberUuids().forEach(uuid -> {
            if (!this.memberUuids.contains(uuid)) {
                this.memberUuids.add(uuid);
            }
        });
    }

    public boolean containsPlayer(UUID uuid) {
        return memberUuids.contains(uuid);
    }

    /**
     * Removes a player from this group. If the leader leaves, a new leader is promoted.
     *
     * @param memberUuid The UUID of the member to remove.
     * @return The name of the new leader if a promotion occurred, otherwise null.
     */
    public String removeMember(UUID memberUuid) {
        memberUuids.remove(memberUuid);

        boolean wasLeader = this.leaderUuid.equals(memberUuid);

        if (wasLeader && !memberUuids.isEmpty()) {
            UUID newLeaderUuid = memberUuids.get(0);
            Player newLeaderPlayer = Bukkit.getPlayer(newLeaderUuid);

            setLeaderUuid(newLeaderUuid);
            if (newLeaderPlayer != null) {
                setLeaderName(newLeaderPlayer.getName());
                return newLeaderPlayer.getName();
            }
        }
        return null;
    }

    public boolean isEmpty() {
        return memberUuids.isEmpty();
    }
}