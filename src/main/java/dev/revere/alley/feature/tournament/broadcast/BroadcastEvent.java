package dev.revere.alley.feature.tournament.broadcast;

import dev.revere.alley.feature.tournament.model.Tournament;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;
import lombok.Getter;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
public abstract class BroadcastEvent {
    /**
     * Broadcast when a tournament is hosted.
     */
    @Getter
    public static class TournamentHosted extends BroadcastEvent {
        private final Tournament tournament;

        public TournamentHosted(Tournament tournament) {
            this.tournament = tournament;
        }
    }

    /**
     * Broadcast when a participant joins the waiting pool.
     */
    @Getter
    public static class ParticipantJoined extends BroadcastEvent {
        private final Tournament tournament;
        private final TournamentParticipant participant;

        public ParticipantJoined(Tournament tournament, TournamentParticipant participant) {
            this.tournament = tournament;
            this.participant = participant;
        }

    }

    /**
     * Broadcast when a round is starting.
     */
    @Getter
    public static class RoundStarting extends BroadcastEvent {
        private final Tournament tournament;
        private final int roundNumber;

        public RoundStarting(Tournament tournament, int roundNumber) {
            this.tournament = tournament;
            this.roundNumber = roundNumber;
        }
    }

    /**
     * Broadcast when a participant is eliminated.
     */
    @Getter
    public static class ParticipantEliminated extends BroadcastEvent {
        private final Tournament tournament;
        private final TournamentParticipant eliminated;
        private final TournamentParticipant winner;
        private final int placement;


        public ParticipantEliminated(
                Tournament tournament,
                TournamentParticipant eliminated,
                TournamentParticipant winner,
                int placement
        ) {
            this.tournament = tournament;
            this.eliminated = eliminated;
            this.winner = winner;
            this.placement = placement;
        }
    }
}