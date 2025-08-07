package dev.revere.alley.feature.tournament.internal.helpers;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@UtilityClass
public class TournamentMessageBuilder {
    /**
     * Generates a natural language message for a participant group joining or leaving.
     * e.g., "yczu and hmEmmy have joined!"
     */
    public String generateParticipantBroadcast(TournamentParticipant participant, String verbSingular, String verbPlural) {
        String nameList = getNaturalTeamNameListWithProfileColors(participant);
        if (nameList.isEmpty()) return "";

        String verb = participant.getSize() > 1 ? verbPlural : verbSingular;
        return CC.translate("&e" + nameList + " &f" + verb + "!");
    }

    /**
     * Creates a natural-language formatted list of team members with a single, uniform color.
     * e.g., "&eplayer1 &7and &eplayer2"
     */
    public String getNaturalTeamNameList(TournamentParticipant participant, String nameColor) {
        List<String> memberNames = participant.getMemberUuids().stream()
                .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return formatNameList(memberNames, nameColor);
    }

    /**
     * Creates a natural-language formatted list of team members using their individual profile name colors.
     * e.g., "&cplayer1 &7and &9player2"
     */
    public String getNaturalTeamNameListWithProfileColors(TournamentParticipant participant) {
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);

        List<String> coloredNames = participant.getMemberUuids().stream()
                .map(uuid -> {
                    Profile profile = profileService.getProfile(uuid);
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);

                    if (offlinePlayer.getName() == null) return null;

                    String color = (profile != null) ? profile.getNameColor().toString() : "&7";
                    return color + offlinePlayer.getName();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return formatColoredNameList(coloredNames);
    }

    private String formatNameList(List<String> names, String nameColor) {
        int size = names.size();
        if (size == 0) return "An unknown team";
        if (size == 1) return nameColor + names.get(0);

        if (size == 2) {
            return nameColor + names.get(0) + " &fand " + nameColor + names.get(1);
        } else {
            String almostAll = names.subList(0, size - 1).stream()
                    .map(name -> nameColor + name)
                    .collect(Collectors.joining("&f, "));
            return almostAll + " &fand " + nameColor + names.get(size - 1);
        }
    }

    private String formatColoredNameList(List<String> coloredNames) {
        int size = coloredNames.size();
        if (size == 0) return "&fAn unknown team";
        if (size == 1) return coloredNames.get(0);

        if (size == 2) {
            return coloredNames.get(0) + " &fand " + coloredNames.get(1);
        } else {
            String almostAll = String.join("&f, ", coloredNames.subList(0, size - 1));
            return almostAll + " &fand " + coloredNames.get(size - 1);
        }
    }
}
