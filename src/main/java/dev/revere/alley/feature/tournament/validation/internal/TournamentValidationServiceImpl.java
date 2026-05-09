package dev.revere.alley.feature.tournament.validation.internal;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.cooldown.Cooldown;
import dev.revere.alley.feature.cooldown.CooldownService;
import dev.revere.alley.feature.cooldown.CooldownType;
import dev.revere.alley.feature.tournament.TournamentService;
import dev.revere.alley.feature.tournament.model.Tournament;
import dev.revere.alley.feature.tournament.model.TournamentState;
import dev.revere.alley.feature.tournament.validation.TournamentValidationService;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
@Service(provides = TournamentValidationService.class, priority = 1100)
public class TournamentValidationServiceImpl implements TournamentValidationService {
    private static final UUID GLOBAL_TOURNAMENT_COOLDOWN_KEY = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final CooldownService cooldowns;

    public TournamentValidationServiceImpl(CooldownService cooldowns) {
        this.cooldowns = cooldowns;
    }

    @Override
    public boolean canPlayerHostTournament(Player player) {
        TournamentService tournamentService = AlleyPlugin.getInstance().getService(TournamentService.class);
        if (tournamentService.getPlayerTournament(player) != null) {
            player.sendMessage(CC.translate("&cYou cannot host a tournament while participating in one."));
            return false;
        }
        if (!tournamentService.getTournaments().isEmpty()) {
            player.sendMessage(CC.translate("&cA tournament is already active."));
            return false;
        }

        if (cooldowns != null) {
            Cooldown cooldown = cooldowns.getCooldown(GLOBAL_TOURNAMENT_COOLDOWN_KEY, CooldownType.TOURNAMENT_HOST);
            if (cooldown != null && cooldown.isActive()) {
                player.sendMessage(
                        CC.translate("&cA tournament was hosted recently. Try again in &f"
                                        + cooldown.remainingTimeInMinutes()
                                        + "&c."
                        )
                );
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean canForceStartTournament(Tournament tournament) {
        if (tournament.getState() != TournamentState.WAITING) {
            return false;
        }
        int currentTeams = tournament.getWaitingPool().size();
        return currentTeams >= tournament.getMinTeams();
    }
}