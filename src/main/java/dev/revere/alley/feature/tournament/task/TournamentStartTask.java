package dev.revere.alley.feature.tournament.task;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.tournament.engine.TournamentEngine;
import dev.revere.alley.feature.tournament.engine.TournamentEvent;
import dev.revere.alley.feature.tournament.model.Tournament;
import lombok.Getter;
import org.bukkit.Sound;

/**
 * @author Remi
 * @project alley-practice
 * @date 6/08/2025
 */
@Getter
public class TournamentStartTask implements Runnable {
    private final Tournament tournament;
    private final TournamentEngine engine;
    private final TournamentCountdownService countdowns;

    private int countdown = 20;

    public TournamentStartTask(Tournament tournament) {
        this.tournament = tournament;
        this.engine = AlleyPlugin.getInstance().getService(TournamentEngine.class);
        this.countdowns = AlleyPlugin.getInstance().getService(TournamentCountdownService.class);
    }

    @Override
    public void run() {
        if (countdown <= 0) {
            if (tournament.getStartingTask() != null) {
                tournament.getStartingTask().cancel();
                tournament.setStartingTask(null);
            }
            countdowns.clearStartTask();
            engine.processEvent(tournament, new TournamentEvent.StartCountdownFinished());
            return;
        }

        if (countdown <= 5 || countdown % 5 == 0 || countdown >= 20) {
            String message = CC.translate("&6Round 1 &fstarts in &6" + countdown + "&f.");
            tournament
                    .getAllPlayers()
                    .forEach(
                            p -> {
                                p.sendMessage(message);
                                p.playSound(p.getLocation(), Sound.NOTE_PLING, 1.0F, 1.0F);
                            });
        }

        countdown--;
    }
}