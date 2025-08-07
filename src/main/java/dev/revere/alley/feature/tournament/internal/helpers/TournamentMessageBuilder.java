package dev.revere.alley.feature.tournament.internal.helpers;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@UtilityClass
public class TournamentMessageBuilder {

    /**
     * Generates a natural language message for a participant group joining or leaving.
     *
     * @param participant  The group in question.
     * @param verbSingular The verb for a single player (e.g., "has joined").
     * @param verbPlural   The verb for multiple players (e.g., "have joined").
     * @return A formatted string for broadcasting.
     */
    public String generateParticipantBroadcast(TournamentParticipant participant, String verbSingular, String verbPlural) {
        List<String> memberNames = participant.getMemberUuids().stream()
                .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        int size = memberNames.size();
        if (size == 0) return "";

        String nameList;
        String verb;

        if (size == 1) {
            nameList = memberNames.get(0);
            verb = verbSingular;
        } else {
            verb = verbPlural;
            if (size == 2) {
                nameList = memberNames.get(0) + " and " + memberNames.get(1);
            } else {
                String almostAll = String.join(", ", memberNames.subList(0, size - 1));
                nameList = almostAll + " and " + memberNames.get(size - 1);
            }
        }

        return CC.translate("&6[Tournament] &e" + nameList + " &f" + verb + "!");
    }
}
