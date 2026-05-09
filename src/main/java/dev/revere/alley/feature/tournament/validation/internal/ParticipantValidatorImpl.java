package dev.revere.alley.feature.tournament.validation.internal;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.party.Party;
import dev.revere.alley.feature.party.PartyService;
import dev.revere.alley.feature.tournament.TournamentService;
import dev.revere.alley.feature.tournament.model.Tournament;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;
import dev.revere.alley.feature.tournament.model.TournamentState;
import dev.revere.alley.feature.tournament.validation.ParticipantValidator;
import org.bukkit.entity.Player;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
@Service(provides = ParticipantValidator.class, priority = 1100)
public class ParticipantValidatorImpl implements ParticipantValidator {
    private final PartyService partyService;

    public ParticipantValidatorImpl() {
        this.partyService = AlleyPlugin.getInstance().getService(PartyService.class);
    }

    @Override
    public boolean canPlayerJoin(Player player, Tournament tournament) {
        if (tournament.getState() != TournamentState.WAITING && tournament.getState() != TournamentState.STARTING) {
            player.sendMessage(CC.translate("&cThis tournament is no longer accepting players."));
            return false;
        }

        TournamentService tournamentService = AlleyPlugin.getInstance().getService(TournamentService.class);
        if (tournamentService.getPlayerTournament(player) != null) {
            player.sendMessage(CC.translate("&cYou are already in a tournament."));
            return false;
        }

        Party party = partyService.getParty(player);
        if (party != null && !party.isLeader(player)) {
            player.sendMessage(CC.translate("&cOnly the party leader can join tournaments."));
            return false;
        }

        return hasSpaceForParty(player, tournament);
    }

    @Override
    public boolean hasSpaceForParty(Player player, Tournament tournament) {
        Party party = partyService.getParty(player);
        int partySize = party != null ? party.getMembers().size() : 1;

        int currentPlayers = tournament.getWaitingPool().stream().mapToInt(TournamentParticipant::getSize).sum();
        int maxPlayers = tournament.getMaxTeams() * tournament.getTeamSize();

        if (currentPlayers + partySize > maxPlayers) {
            player.sendMessage(CC.translate("&cThere is not enough space in this tournament for your party."));
            return false;
        }
        return true;
    }
}