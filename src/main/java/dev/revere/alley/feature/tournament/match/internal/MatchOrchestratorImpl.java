package dev.revere.alley.feature.tournament.match.internal;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.arena.ArenaService;
import dev.revere.alley.feature.match.Match;
import dev.revere.alley.feature.match.MatchService;
import dev.revere.alley.feature.match.internal.types.DefaultMatch;
import dev.revere.alley.feature.match.model.GameParticipant;
import dev.revere.alley.feature.match.model.internal.MatchGamePlayer;
import dev.revere.alley.feature.tournament.broadcast.BroadcastEvent;
import dev.revere.alley.feature.tournament.broadcast.TournamentBroadcaster;
import dev.revere.alley.feature.tournament.match.MatchOrchestrationResult;
import dev.revere.alley.feature.tournament.match.MatchOrchestrator;
import dev.revere.alley.feature.tournament.match.MatchParticipantFactory;
import dev.revere.alley.feature.tournament.match.MatchProcessingResult;
import dev.revere.alley.feature.tournament.model.Tournament;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;
import dev.revere.alley.feature.tournament.participant.ParticipantRegistry;
import dev.revere.alley.feature.tournament.participant.ParticipantStatus;
import dev.revere.alley.feature.tournament.player.PlayerTournamentStateService;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
@Service(provides = MatchOrchestrator.class, priority = 1100)
public class MatchOrchestratorImpl implements MatchOrchestrator {
    private final MatchService matchService;
    private final ArenaService arenaService;
    private final MatchParticipantFactory participantFactory;
    private final ParticipantRegistry participantRegistry;
    private final TournamentBroadcaster broadcaster;
    private final PlayerTournamentStateService playerState;

    public MatchOrchestratorImpl(
            MatchParticipantFactory participantFactory,
            ParticipantRegistry participantRegistry,
            TournamentBroadcaster broadcaster,
            PlayerTournamentStateService playerState) {
        this.matchService = AlleyPlugin.getInstance().getService(MatchService.class);
        this.arenaService = AlleyPlugin.getInstance().getService(ArenaService.class);
        this.participantFactory = participantFactory;
        this.participantRegistry = participantRegistry;
        this.broadcaster = broadcaster;
        this.playerState = playerState;
    }

    @Override
    public MatchOrchestrationResult createRoundMatches(
            Tournament tournament, List<TournamentParticipant> participants) {
        List<Match> createdMatches = new ArrayList<>();
        List<TournamentParticipant> byeParticipants = new ArrayList<>();

        participants.forEach(p -> participantRegistry.updateParticipantStatus(p, ParticipantStatus.ACTIVE));

        List<TournamentParticipant> pool = new ArrayList<>(participants);

        if (pool.size() % 2 != 0) {
            TournamentParticipant bye = pool.remove(pool.size() - 1);
            byeParticipants.add(bye);

            for (Player player : bye.getOnlinePlayers()) {
                player.sendMessage("");
                player.sendMessage(CC.translate("&c&lYou are being skipped this round."));
                player.sendMessage(CC.translate("&c&lWe couldn't find a match for you."));
                player.sendMessage("");
            }
        }

        for (int i = 0; i < pool.size(); i += 2) {
            TournamentParticipant teamA = pool.get(i);
            TournamentParticipant teamB = pool.get(i + 1);

            Arena arena = arenaService.getRandomArena(tournament.getKit());
            if (arena == null) {
                return MatchOrchestrationResult.failure("No available arenas for kit: " + tournament.getKit().getDisplayName());
            }

            GameParticipant<MatchGamePlayer> participantA = participantFactory.buildParticipant(teamA);
            GameParticipant<MatchGamePlayer> participantB = participantFactory.buildParticipant(teamB);

            Set<Match> before = new HashSet<>(tournament.getActiveMatches());
            matchService.createTournamentMatch(tournament, tournament.getKit(), arena, participantA, participantB);
            Set<Match> after = new HashSet<>(tournament.getActiveMatches());
            after.removeAll(before);
            createdMatches.addAll(after);
        }

        return MatchOrchestrationResult.success(createdMatches, byeParticipants);
    }

    @Override
    public MatchProcessingResult processMatchResult(Match match, Tournament tournament) {
        tournament.removeActiveMatch(match);

        TournamentParticipant winner = resolveParticipantFromMatch(tournament, match, true);
        TournamentParticipant loser = resolveParticipantFromMatch(tournament, match, false);

        if (winner == null || loser == null) {
            return MatchProcessingResult.failure("Could not determine match participants");
        }

        int teamsInCurrentRound = tournament.getRoundParticipants().size();
        sendWinnerMessage(winner);
        sendLoserMessage(loser, teamsInCurrentRound);

        winner.getOnlinePlayers().forEach(p -> playerState.setPlayerTournamentState(p, tournament));

        participantRegistry.updateParticipantStatus(loser, ParticipantStatus.ELIMINATED);
        tournament.removeParticipant(loser);
        tournament.getPlacementList().add(0, loser);
        participantRegistry.unregisterParticipant(loser, tournament);

        int placement = tournament.getPlacementList().size();
        broadcaster.broadcast(new BroadcastEvent.ParticipantEliminated(tournament, loser, winner, placement));

        return MatchProcessingResult.success(winner, loser);
    }

    @Override
    public boolean isRoundComplete(Tournament tournament) {
        return tournament.getActiveMatches().isEmpty();
    }

    /**
     * Resolves a tournament participant from a completed match by deferring to the
     * concrete match's winner/loser participants and mapping via leader UUID,
     * matching the original system's behavior.
     *
     * @param tournament The tournament context.
     * @param match      The completed match.
     * @param winner     True to resolve the winner, false for the loser.
     * @return The resolved TournamentParticipant, or null if not found.
     */
    private TournamentParticipant resolveParticipantFromMatch(Tournament tournament, Match match, boolean winner) {
        if (!(match instanceof DefaultMatch)) {
            return null;
        }

        DefaultMatch defaultMatch = (DefaultMatch) match;

        GameParticipant<MatchGamePlayer> participant = winner ? defaultMatch.getWinner() : defaultMatch.getLoser();

        if (participant == null || participant.getLeader() == null) {
            return null;
        }

        UUID leaderUuid = participant.getLeader().getUuid();
        return findParticipantGroupForUuid(tournament, leaderUuid);
    }


    /**
     * Finds the tournament participant group containing a specific player UUID by
     * checking current participants and round participants, then falling back to
     * the waiting pool.
     *
     * @param tournament The tournament context.
     * @param playerUuid The player UUID to search for.
     * @return The TournamentParticipant containing the UUID, or null if none.
     */
    private TournamentParticipant findParticipantGroupForUuid(Tournament tournament, UUID playerUuid) {
        TournamentParticipant participant =
                tournament.getParticipants().stream()
                        .filter(tg -> tg.containsPlayer(playerUuid))
                        .findFirst()
                        .orElse(null);

        if (participant != null) {
            return participant;
        }

        participant = tournament.getRoundParticipants().stream()
                .filter(tg -> tg.containsPlayer(playerUuid))
                .findFirst()
                .orElse(null);

        if (participant != null) {
            return participant;
        }

        return tournament.getWaitingPool().stream()
                .filter(tp -> tp.containsPlayer(playerUuid))
                .findFirst()
                .orElse(null);
    }

    /**
     * Sends the winner's personal round-win message with a clickable status line.
     *
     * @param winner The winning tournament participant.
     */
    private void sendWinnerMessage(TournamentParticipant winner) {
        TextComponent statusLine = new TextComponent(CC.translate(" &aView Status: "));
        TextComponent clickPart = new TextComponent(CC.translate("&7&o(Click Here)"));
        clickPart.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tournament info"));
        clickPart.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{
                new TextComponent(CC.translate("&eClick to view tournament status."))
        }));
        statusLine.addExtra(clickPart);

        winner.getOnlinePlayers().forEach(player ->
        {
            player.sendMessage(CC.translate("&a&lYOU'VE WON THE ROUND"));
            player.spigot().sendMessage(statusLine);
            player.sendMessage("");
        });
    }

    /**
     * Sends the loser's personal elimination message with a clickable status
     * line and placement number.
     *
     * @param loser        The losing participant.
     * @param teamsInRound The number of teams in the round (for placement).
     */
    private void sendLoserMessage(TournamentParticipant loser, int teamsInRound) {
        TextComponent statusLine = new TextComponent(CC.translate(" &cView Status: "));
        TextComponent clickPart = new TextComponent(CC.translate("&7&o(Click Here)"));
        clickPart.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tournament info"));
        clickPart.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{
                new TextComponent(CC.translate("&eClick to view tournament status."))
        }));
        statusLine.addExtra(clickPart);
        loser.getOnlinePlayers().forEach(player ->
        {
            player.sendMessage(CC.translate("&c&lYOU'VE BEEN ELIMINATED"));
            player.spigot().sendMessage(statusLine);
            player.sendMessage(CC.translate("&7You placed #" + teamsInRound + "!"));
            player.sendMessage("");
        });
    }
}