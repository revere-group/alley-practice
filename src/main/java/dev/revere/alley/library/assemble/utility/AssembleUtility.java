package dev.revere.alley.library.assemble.utility;

import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;

/**
 * @author Remi
 * @project alley-practice
 * @date 12/08/2025
 */
@UtilityClass
public class AssembleUtility {

    public String[] splitTeamText(String raw) {
        if (raw == null) return new String[]{"", ""};

        String input = ChatColor.translateAlternateColorCodes('&', raw);

        LeadStyle style = extractLeadStyle(input);
        String lead = style.minimal;
        String content = style.remainder;

        int budget = Math.max(0, 16 - lead.length());
        String prefixText = takeRaw(content, budget);
        String prefix = safeTail(lead + prefixText);

        String remainder = content.substring(prefixText.length());
        String lastColors = ChatColor.getLastColors(prefix); // includes color + formats (e.g., §8§m)

        boolean remainderStartsWithCode =
                remainder.length() >= 2
                        && remainder.charAt(0) == ChatColor.COLOR_CHAR
                        && isCodeChar(Character.toLowerCase(remainder.charAt(1)));

        String suffixBase = remainderStartsWithCode ? remainder : (lastColors + remainder);
        String suffix = safeTail(takeRaw(suffixBase, 16));

        return new String[]{prefix, suffix};
    }

    private static LeadStyle extractLeadStyle(String s) {
        int i = 0;
        char color = 0;
        boolean obf = false, bold = false, strike = false, underline = false, italic = false;

        while (i + 1 < s.length() && s.charAt(i) == ChatColor.COLOR_CHAR) {
            char code = Character.toLowerCase(s.charAt(i + 1));
            if (code == 'r') {
                color = 0;
                obf = bold = strike = underline = italic = false;
            } else if (isColor(code)) {
                color = code;
                obf = bold = strike = underline = italic = false;
            } else {
                switch (code) {
                    case 'k':
                        obf = true;
                        break;
                    case 'l':
                        bold = true;
                        break;
                    case 'm':
                        strike = true;
                        break;
                    case 'n':
                        underline = true;
                        break;
                    case 'o':
                        italic = true;
                        break;
                }
            }
            i += 2;
        }

        StringBuilder minimal = new StringBuilder(12);
        if (color != 0) minimal.append(ChatColor.COLOR_CHAR).append(color);
        if (obf) minimal.append(ChatColor.COLOR_CHAR).append('k');
        if (bold) minimal.append(ChatColor.COLOR_CHAR).append('l');
        if (strike) minimal.append(ChatColor.COLOR_CHAR).append('m');
        if (underline) minimal.append(ChatColor.COLOR_CHAR).append('n');
        if (italic) minimal.append(ChatColor.COLOR_CHAR).append('o');

        return new LeadStyle(minimal.toString(), s.substring(i));
    }

    private static boolean isColor(char code) {
        return (code >= '0' && code <= '9') || (code >= 'a' && code <= 'f');
    }

    private static boolean isCodeChar(char c) {
        return (c >= '0' && c <= '9')
                || (c >= 'a' && c <= 'f')
                || (c >= 'k' && c <= 'o')
                || c == 'r';
    }

    private static String takeRaw(String s, int rawCount) {
        if (rawCount <= 0 || s.isEmpty()) return "";
        if (s.length() <= rawCount) return s;
        String cut = s.substring(0, rawCount);
        if (cut.charAt(cut.length() - 1) == ChatColor.COLOR_CHAR) cut = cut.substring(0, cut.length() - 1);
        return cut;
    }

    private static String safeTail(String s) {
        if (s.isEmpty()) return s;
        if (s.charAt(s.length() - 1) == ChatColor.COLOR_CHAR) return s.substring(0, s.length() - 1);
        return s;
    }

    private static class LeadStyle {
        final String minimal, remainder;

        LeadStyle(String minimal, String remainder) {
            this.minimal = minimal;
            this.remainder = remainder;
        }
    }
}