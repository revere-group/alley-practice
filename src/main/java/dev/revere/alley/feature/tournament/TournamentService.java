package dev.revere.alley.feature.tournament;

import dev.revere.alley.bootstrap.lifecycle.Service;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.match.Match;
import dev.revere.alley.feature.tournament.model.Tournament;
import dev.revere.alley.feature.tournament.model.TournamentType;
import dev.revere.alley.feature.tournament.task.TournamentRoundStartTask;
import dev.revere.alley.feature.tournament.task.TournamentStartTask;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * @author Remi
 * @project alley-practice
 * @date 6/08/2025
 */
public interface TournamentService extends Service {
    void hostTournament(Player host, TournamentType type, Kit kit);

    void adminHostTournament(Player host, Kit kit, int teamSize, int maxTeams);

    void joinTournament(Player player, Tournament tournament);

    void handlePlayerDeparture(Player player);

    void forceStartTournament(Tournament tournament);

    void cancelTournament(Tournament tournament, String reason);

    void handleMatchEnd(Match match);

    Tournament getTournament(UUID tournamentId);

    Tournament getPlayerTournament(Player player);

    List<Tournament> getTournaments();

    /**
     * Returns the currently active tournament start task instance.
     *
     * @return The TournamentStartTask, or null if none is running.
     */
    TournamentStartTask getTournamentStartTask();

    TournamentRoundStartTask getTournamentRoundStartTask();
}