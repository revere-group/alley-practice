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
public class TournamentStartTask implements Runnable {
    private final Tournament tournament;
    private final TournamentServiceImpl service;

    @Getter
    private int countdown = 10;

    public TournamentStartTask(Tournament tournament, TournamentServiceImpl service) {
        this.tournament = tournament;
        this.service = service;
    }

    @Override
    public void run() {
        if (countdown <= 0) {
            tournament.getStartingTask().cancel();
            service.startTournament(tournament);
            return;
        }

        if (countdown <= 5 || countdown == 10) {
            String message = CC.translate("&6[Tournament] &fStarting in &e" + countdown + " &fsecond" + (countdown == 1 ? "" : "s") + "...");
            tournament.broadcast(message);
            tournament.getAllPlayers().forEach(p -> p.playSound(p.getLocation(), Sound.NOTE_PLING, 1.0F, 1.0F));
        }

        countdown--;
    }
}