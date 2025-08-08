package dev.revere.alley.feature.tournament.participant.internal;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.feature.party.Party;
import dev.revere.alley.feature.party.PartyService;
import dev.revere.alley.feature.tournament.model.Tournament;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;
import dev.revere.alley.feature.tournament.participant.ParticipantRegistry;
import dev.revere.alley.feature.tournament.participant.ParticipantStatus;
import dev.revere.alley.feature.tournament.player.PlayerTournamentStateService;
import dev.revere.alley.feature.tournament.validation.ParticipantValidator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
@Service(provides = ParticipantRegistry.class, priority = 1100)
public class ParticipantRegistryImpl implements ParticipantRegistry {
    private final Map<TournamentParticipant, ParticipantStatus> participantStatusMap = new ConcurrentHashMap<>();

    private final PartyService partyService;
    private final ParticipantValidator validator;
    private final PlayerTournamentStateService playerState;

    public ParticipantRegistryImpl(ParticipantValidator validator, PlayerTournamentStateService playerState) {
        this.partyService = AlleyPlugin.getInstance().getService(PartyService.class);
        this.validator = validator;
        this.playerState = playerState;
    }

    @Override
    public TournamentParticipant registerParticipant(Player player, Tournament tournament) {
        if (!validator.canPlayerJoin(player, tournament)) {
            return null;
        }

        TournamentParticipant participant = createParticipantFromPlayer(player);
        if (participant != null) {
            participantStatusMap.put(participant, ParticipantStatus.WAITING);
            participant.getOnlinePlayers().forEach(p -> playerState.setPlayerTournamentState(p, tournament));
        }
        return participant;
    }

    @Override
    public void unregisterParticipant(TournamentParticipant participant, Tournament tournament) {
        participantStatusMap.remove(participant);
        tournament.removeParticipant(participant);
        participant.getOnlinePlayers().forEach(playerState::resetPlayerStateToLobby);
    }

    @Override
    public void purgeTournament(Tournament tournament) {
        Set<TournamentParticipant> all = new HashSet<>();
        all.addAll(tournament.getWaitingPool());
        all.addAll(tournament.getParticipants());
        all.addAll(tournament.getRoundParticipants());
        all.addAll(tournament.getPlacementList());

        participantStatusMap.keySet().removeAll(all);
    }

    @Override
    public TournamentParticipant findParticipantByPlayer(Player player, Tournament tournament) {
        return getAllTournamentParticipants(tournament).stream()
                .filter(p -> p.containsPlayer(player.getUniqueId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void updateParticipantStatus(TournamentParticipant participant, ParticipantStatus status) {
        participantStatusMap.put(participant, status);
    }

    @Override
    public List<TournamentParticipant> getParticipantsByStatus(Tournament tournament, ParticipantStatus status) {
        return getAllTournamentParticipants(tournament).stream()
                .filter(p -> participantStatusMap.getOrDefault(p, ParticipantStatus.WAITING) == status)
                .collect(Collectors.toList());
    }

    /**
     * Creates a TournamentParticipant from a player, respecting party leadership.
     *
     * @param player The player attempting to join.
     * @return A TournamentParticipant for the solo player or their party, or
     * {@code null} if the player is in a party but is not the leader.
     */
    private TournamentParticipant createParticipantFromPlayer(Player player) {
        Party party = partyService.getParty(player);
        if (party == null) {
            return new TournamentParticipant(player);
        }

        if (!party.isLeader(player)) {
            return null;
        }

        List<Player> partyMembers =
                party.getMembers().stream()
                        .map(Bukkit::getPlayer)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

        return new TournamentParticipant(player, partyMembers);
    }

    /**
     * Aggregates all participants from waiting, participants, and round pools.
     *
     * @param tournament The tournament to inspect.
     * @return Combined participant list.
     */
    private List<TournamentParticipant> getAllTournamentParticipants(Tournament tournament) {
        List<TournamentParticipant> all = new ArrayList<>();
        all.addAll(tournament.getWaitingPool());
        all.addAll(tournament.getParticipants());
        all.addAll(tournament.getRoundParticipants());
        return all;
    }
}
