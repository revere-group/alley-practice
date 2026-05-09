package dev.revere.alley.feature.tournament.formation.assembly.internal;

import dev.revere.alley.feature.tournament.formation.assembly.PartyMatcher;
import dev.revere.alley.feature.tournament.formation.assembly.TeamAssembler;
import dev.revere.alley.feature.tournament.formation.model.TeamDistribution;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PartyAwareTeamAssembler implements TeamAssembler {
    private final PartyMatcher partyMatcher;

    public PartyAwareTeamAssembler() {
        this.partyMatcher = new BestFitPartyMatcher();
    }

    @Override
    public List<TournamentParticipant> assembleTeams(List<TournamentParticipant> participantPool,
                                                     TeamDistribution distribution,
                                                     int maxTeamSize) {
        List<TournamentParticipant> teams = new ArrayList<>();
        List<TournamentParticipant> availableParties = new ArrayList<>(participantPool);

        availableParties.sort(Comparator.comparingInt(TournamentParticipant::getSize).reversed());

        for (int targetSize : distribution.getTeamSizes()) {
            TournamentParticipant team = partyMatcher.createTeamOfSize(availableParties, targetSize, maxTeamSize);
            if (team != null) {
                teams.add(team);
            }
        }

        return teams;
    }
}