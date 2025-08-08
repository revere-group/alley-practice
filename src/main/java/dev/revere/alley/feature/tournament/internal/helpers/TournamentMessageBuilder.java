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
     * Generates a broadcast message for a tournament participant.
     * The message is formatted based on the number of team members.
     *
     * @param participant  The tournament participant.
     * @param verbSingular The verb to use if the participant has one member.
     * @param verbPlural   The verb to use if the participant has multiple members.
     * @return A formatted broadcast message.
     */
    public String generateParticipantBroadcast(TournamentParticipant participant, String verbSingular, String verbPlural) {
        String nameList = getNaturalTeamNameListWithProfileColors(participant);
        if (nameList.isEmpty()) return "";

        String verb = participant.getSize() > 1 ? verbPlural : verbSingular;
        return CC.translate("&e" + nameList + " &f" + verb + "!");
    }

    /**
     * Creates a natural-language formatted list of team members with a specified color.
     * e.g., "&cplayer1 &7and &9player2"
     *
     * @param participant The tournament participant.
     * @param nameColor   The color to apply to the names.
     * @return A formatted string of team member names.
     */
    public String getNaturalTeamNameList(TournamentParticipant participant, String nameColor) {
        List<String> memberNames = participant.getMemberUuids().stream()
                .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return formatNameList(memberNames, nameColor);
    }

    /**
     * Creates a natural-language formatted list of team members with their profile colors.
     * e.g., "&cplayer1 &7and &9player2"
     *
     * @param participant The tournament participant.
     * @return A formatted string of team member names with profile colors.
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

    /**
     * Formats a list of names into a natural-language string with a specified color.
     * Handles singular and plural cases appropriately.
     *
     * @param names     The list of names to format.
     * @param nameColor The color to apply to the names.
     * @return A formatted string of names.
     */
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

    /**
     * Formats a list of colored names into a natural-language string.
     * Handles singular and plural cases appropriately.
     *
     * @param coloredNames The list of colored names to format.
     * @return A formatted string of colored names.
     */
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
