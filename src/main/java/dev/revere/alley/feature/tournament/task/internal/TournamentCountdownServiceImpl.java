package dev.revere.alley.feature.tournament.task.internal;

import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.feature.tournament.task.TournamentCountdownService;
import dev.revere.alley.feature.tournament.task.TournamentRoundStartTask;
import dev.revere.alley.feature.tournament.task.TournamentStartTask;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
@Setter
@Getter
@Service(provides = TournamentCountdownService.class, priority = 1100)
public class TournamentCountdownServiceImpl implements TournamentCountdownService {
    private volatile TournamentStartTask startTask;
    private volatile TournamentRoundStartTask roundStartTask;

    @Override
    public void clearStartTask() {
        this.startTask = null;
    }

    @Override
    public void clearRoundStartTask() {
        this.roundStartTask = null;
    }
}
