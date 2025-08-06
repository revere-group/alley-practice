package dev.revere.alley.feature.tournament.task;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.tournament.internal.TournamentServiceImpl;
import dev.revere.alley.feature.tournament.model.Tournament;
import lombok.Getter;
import org.bukkit.Sound;

/**
 * @author Remi
 * @project alley-practice
 * @date 6/08/2025
 */
public class TournamentRoundStartTask implements Runnable{
    private final Tournament tournament;
    private final TournamentServiceImpl service;

    @Getter
    private int countdown = 20;

    public TournamentRoundStartTask(Tournament tournament, TournamentServiceImpl service) {
        this.tournament = tournament;
        this.service = service;
    }

    @Override
    public void run() {
        if (countdown <= 0) {
            tournament.getRoundStartTask().cancel();
            service.checkForNextRound(tournament);
            return;
        }

        if (countdown <= 5 || countdown % 5 == 0) {
            String message = CC.translate("&6[Tournament] &aThe next round will begin in &e" + countdown + " &aseconds...");
            tournament.broadcast(message);
            tournament.getAllPlayers().forEach(player -> player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 1.2f));
        }

        countdown--;
    }
}
