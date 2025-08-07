package dev.revere.alley.feature.tournament.internal.helpers;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.party.Party;
import dev.revere.alley.feature.tournament.TournamentService;
import dev.revere.alley.feature.tournament.model.Tournament;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;
import dev.revere.alley.feature.tournament.model.TournamentState;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;

@UtilityClass
public class TournamentValidator {
    /**
     * Checks if a player can join a tournament based on its state,
     * their current tournament status, and party leader status.
     *
     * @param player     The player to check.
     * @param tournament The tournament to join.
     * @return True if the player can join, false otherwise.
     */
    public boolean canPlayerJoin(Player player, Tournament tournament, TournamentService tournamentService) {
        if (tournament.getState() != TournamentState.WAITING) {
            player.sendMessage(CC.translate("&cThis tournament is no longer accepting players."));
            return false;
        }
        if (tournamentService.getPlayerTournament(player) != null) {
            player.sendMessage(CC.translate("&cYou are already in a tournament."));
            return false;
        }
        return true;
    }

    public boolean hasSpaceForParty(Tournament tournament, Party party) {
        int currentPlayers = tournament.getWaitingPool().stream().mapToInt(TournamentParticipant::getSize).sum();
        int maxPlayers = tournament.getMaxTeams() * tournament.getTeamSize();
        return currentPlayers + party.getMembers().size() <= maxPlayers;
    }
}