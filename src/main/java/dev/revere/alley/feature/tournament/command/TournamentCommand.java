package dev.revere.alley.feature.tournament.command;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.common.text.ClickableUtil;
import dev.revere.alley.common.tournament.RoundStageUtil;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.KitService;
import dev.revere.alley.feature.match.Match;
import dev.revere.alley.feature.match.model.GameParticipant;
import dev.revere.alley.feature.match.model.internal.MatchGamePlayer;
import dev.revere.alley.feature.tournament.TournamentService;
import dev.revere.alley.feature.tournament.model.Tournament;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;
import dev.revere.alley.feature.tournament.model.TournamentState;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Remi
 * @project alley-practice
 * @date 6/08/2025
 */
public class TournamentCommand extends BaseCommand {
    private final TournamentService tournamentService = AlleyPlugin.getInstance().getService(TournamentService.class);
    private final KitService kitService = AlleyPlugin.getInstance().getService(KitService.class);

    private final String[][] pages = {
            {
                    " &6│ &6/tournament list &7| View all active tournaments.",
                    " &6│ &6/tournament join [id] &7| Join an available tournament.",
                    " &6│ &6/tournament leave &7| Leave your current tournament.",
                    " &6│ &6/tournament info &7| View info about your tournament."
            },
            {
                    " &6│ &6/tournament start [id] &7| &c(Admin) &7Force start a tournament.",
                    " &6│ &6/tournament host <kit> <size> <max> &7| &c(Admin) &7Host a tournament.",
                    " &6│ &6/tournament cancel [id] <reason> &7| &c(Admin) &7Cancel a tournament."
            }
    };

    @Override
    @CommandData(name = "tournament", description = "Base tournament command.", aliases = {"t", "tourney"})
    public void onCommand(CommandArgs command) {
        CommandSender sender = command.getSender();
        String[] args = command.getArgs();
        int page = 1;

        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(CC.translate("&cInvalid page number. Please use '/tournament list' to see tournaments."));
                return;
            }
        }

        if (page > this.pages.length || page < 1) {
            sender.sendMessage(CC.translate("&cNo more pages available."));
            return;
        }

        sender.sendMessage("");
        sender.sendMessage(CC.translate("&6&lTournament Commands &8(&7Page &f" + page + "&7/&f" + this.pages.length + "&8)"));
        for (String string : this.pages[page - 1]) {
            sender.sendMessage(CC.translate(string));
        }
        sender.sendMessage("");

        if (sender instanceof Player) {
            ClickableUtil.sendPageNavigation((Player) sender, page, this.pages.length, "/tournament", false, true);
        }
    }

    @CommandData(name = "tournament.list")
    public void listTournaments(CommandArgs command) {
        Player player = command.getPlayer();
        List<Tournament> tournaments = tournamentService.getTournaments().stream()
                .filter(t -> t.getState() != TournamentState.ENDED)
                .collect(Collectors.toList());

        player.sendMessage("");
        player.sendMessage(CC.translate("     &6&lActive Tournaments &f(" + tournaments.size() + ")"));

        if (tournaments.isEmpty()) {
            player.sendMessage(CC.translate("      &7│ &cNo active tournaments."));
            player.sendMessage("");
            return;
        }

        tournaments.stream()
                .sorted(Comparator.comparing(Tournament::getNumericId))
                .forEach(tournament -> {
                    int numericId = tournament.getNumericId();
                    int currentPlayers = tournament.getWaitingPool().stream().mapToInt(TournamentParticipant::getSize).sum();
                    int maxPlayers = tournament.getMaxTeams() * tournament.getTeamSize();

                    ComponentBuilder hover = new ComponentBuilder("")
                            .append(CC.translate("&6&lTournament Info &7[" + tournament.getState().getDisplayName() + "]\n"))
                            .append(CC.translate(" &6│ &fHost: &6" + tournament.getHostName() + "\n"))
                            .append(CC.translate(" &6│ &fType: &6" + tournament.getDisplayName() + "\n"))
                            .append(CC.translate(" &6│ &fKit: &6" + tournament.getKit().getDisplayName() + "\n"));

                    if (tournament.getState() == TournamentState.WAITING || tournament.getState() == TournamentState.STARTING) {
                        hover.append(CC.translate(" &6│ &fPlayers: &6" + currentPlayers + "/" + maxPlayers + "\n"));
                    } else {
                        hover.append(CC.translate(" &6│ &fRound: &6" + tournament.getCurrentRound() + "\n"));
                        hover.append(CC.translate(" &6│ &fTeams Left: &6" + tournament.getRoundParticipants().size() + "\n"));
                    }
                    hover.append(CC.translate(" &6│ &fID: &7#" + numericId));

                    ComponentBuilder message = new ComponentBuilder("      ");
                    message.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover.create()));
                    message.append("│ ").color(ChatColor.GOLD.asBungee());
                    message.append(tournament.getDisplayName() + " (ID: #" + numericId + ")").color(ChatColor.WHITE.asBungee());
                    message.append(" - ").color(ChatColor.GRAY.asBungee());

                    if (tournament.getState() == TournamentState.WAITING && player.hasPermission("alley.tournament.join")) {
                        message.append("[Join] ").color(ChatColor.GREEN.asBungee())
                                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tournament join " + numericId))
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to join tournament #" + numericId).color(ChatColor.GREEN.asBungee()).create()));
                    }

                    if (player.hasPermission("alley.tournament.admin.cancel")) {
                        message.append("[X]").color(ChatColor.RED.asBungee())
                                .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tournament cancel " + numericId + " "))
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to cancel tournament #" + numericId).color(ChatColor.RED.asBungee()).create()));
                    }

                    player.spigot().sendMessage(message.create());
                });

        player.sendMessage("");
    }

    @CommandData(name = "tournament.info")
    public void info(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();

        Tournament tournament = resolveInfoTarget(player, args);
        if (tournament == null) return;

        int teamSize = Math.max(1, tournament.getTeamSize());
        int maxPlayers = tournament.getMaxTeams() * teamSize;

        int currentPlayers =
                (tournament.getState() == TournamentState.WAITING || tournament.getState() == TournamentState.STARTING)
                        ? tournament.getWaitingPool().stream().mapToInt(TournamentParticipant::getSize).sum()
                        : tournament.getRoundParticipants().stream().mapToInt(TournamentParticipant::getSize).sum();

        String modeStr = tournament.getKit().getDisplayName() + " " + teamSize + "v" + teamSize;
        String roundLabel = getRoundLabel(tournament);

        player.sendMessage("");
        player.sendMessage(CC.translate("&6&lTournament"));

        List<Match> matches = tournament.getActiveMatches();
        if (!matches.isEmpty()) {
            player.sendMessage(CC.translate(" &6│ &fMatches &7(&6" + matches.size() + "&7):"));
            matches.forEach(match -> {
                List<GameParticipant<MatchGamePlayer>> parts = match.getParticipants();
                if (parts == null || parts.size() < 2) return;

                String left = formatParticipantNames(parts.get(0));
                String right = formatParticipantNames(parts.get(1));
                player.sendMessage(CC.translate("   &6" + left + " &fvs. &6" + right));
            });
            player.sendMessage(CC.translate(""));
        }

        player.sendMessage(CC.translate(" &6│ &fPlayers: &6" + currentPlayers + "&7/&6" + maxPlayers));
        player.sendMessage(CC.translate(" &6│ &fHost: &6" + tournament.getHostName()));
        player.sendMessage(CC.translate(" &6│ &fMode: &6" + modeStr));
        player.sendMessage(CC.translate(" &6│ &fRound: &6" + roundLabel));
        player.sendMessage("");
    }

    @CommandData(name = "tournament.start", permission = "alley.tournament.admin.start")
    public void start(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();
        List<Tournament> waiting = tournamentService.getTournaments().stream()
                .filter(t -> t.getState() == TournamentState.WAITING)
                .collect(Collectors.toList());

        if (waiting.isEmpty()) {
            player.sendMessage(CC.translate("&cThere are no tournaments waiting to be started."));
            return;
        }

        Tournament tournamentToStart = findTournamentFromArgs(player, args, waiting);
        if (tournamentToStart == null) return;

        if (tournamentToStart.getState() != TournamentState.WAITING) {
            player.sendMessage(CC.translate("&cThat tournament is not in a startable state."));
            return;
        }

        int minTeams = tournamentToStart.getMinTeams();
        int currentTeams = tournamentToStart.getWaitingPool().size();
        if (currentTeams < minTeams) {
            player.sendMessage(
                    CC.translate("&cNot enough teams to force-start. &7(Need at least &f" + minTeams
                            + "&7 teams, currently &f" + currentTeams + "&7)"));
            return;
        }

        player.sendMessage(CC.translate("&aAttempting to force-start the " + tournamentToStart.getDisplayName() + " tournament..."));
        tournamentService.forceStartTournament(tournamentToStart);
    }

    @CommandData(name = "tournament.cancel", permission = "alley.tournament.admin.cancel")
    public void cancel(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();
        List<Tournament> active = tournamentService.getTournaments().stream()
                .filter(t -> t.getState() != TournamentState.ENDED)
                .collect(Collectors.toList());

        if (active.isEmpty()) {
            player.sendMessage(CC.translate("&cThere are no active tournaments to cancel."));
            return;
        }

        Tournament tournamentToCancel = findTournamentFromArgs(player, args, active);
        if (tournamentToCancel == null) return;

        String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "No reason provided.";
        player.sendMessage(CC.translate("&cAttempting to cancel the " + tournamentToCancel.getDisplayName() + " tournament..."));
        tournamentService.cancelTournament(tournamentToCancel, reason);
    }

    @CommandData(name = "tournament.join")
    public void join(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();

        List<Tournament> joinable =
                tournamentService.getTournaments().stream()
                        .filter(t -> t.getState() == TournamentState.WAITING)
                        .collect(Collectors.toList());

        if (joinable.isEmpty()) {
            player.sendMessage(CC.translate("&cNo tournaments are available to join."));
            return;
        }

        Tournament target = findJoinTarget(player, args, joinable);
        if (target == null) return;

        tournamentService.joinTournament(player, target);
    }

    @CommandData(name = "tournament.leave")
    public void leave(CommandArgs command) {
        tournamentService.handlePlayerDeparture(command.getPlayer());
    }

    @CommandData(name = "tournament.host", permission = "alley.tournament.admin.host")
    public void host(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();
        if (args.length < 3) {
            player.sendMessage(CC.translate("&cUsage: /tournament host <kit> <teamSize> <maxTeams>"));
            return;
        }

        Kit kit = kitService.getKit(args[0]);
        if (kit == null) {
            player.sendMessage(CC.translate("&cKit '" + args[0] + "' not found."));
            return;
        }

        try {
            int teamSize = Integer.parseInt(args[1]);
            int maxTeams = Integer.parseInt(args[2]);
            tournamentService.adminHostTournament(player, kit, teamSize, maxTeams);
        } catch (NumberFormatException e) {
            player.sendMessage(CC.translate("&cTeam size and max teams must be valid numbers."));
        }
    }

    /**
     * Gets a user-friendly round label based on the tournament state.
     *
     * @param tournament The tournament to get the round label for.
     * @return A string representing the current round label.
     */
    private @NotNull String getRoundLabel(Tournament tournament) {
        String roundLabel;
        if (tournament.getState() == TournamentState.WAITING) {
            roundLabel = "Waiting for players...";
        } else if (tournament.getState() == TournamentState.STARTING) {
            roundLabel = "Starting...";
        } else if (tournament.getState() == TournamentState.INTERMISSION) {
            roundLabel = "Waiting for next round...";
        } else {
            int teamsLeft = tournament.getRoundParticipants().size();
            roundLabel = RoundStageUtil.getRoundStageName(teamsLeft);
        }
        return roundLabel;
    }

    /**
     * Resolves the target tournament for the info command.
     * - If an ID is provided, it tries to find that tournament.
     * - If no ID is provided, it checks if the player is in a tournament.
     * - If not in a tournament, it lists active tournaments and prompts for selection.
     *
     * @param sender The player executing the command.
     * @param args   Command arguments after "info".
     * @return The resolved Tournament or null if not found.
     */
    private Tournament resolveInfoTarget(Player sender, String[] args) {
        if (args.length > 0) {
            try {
                int id = Integer.parseInt(args[0]);
                Tournament t =
                        tournamentService.getTournaments().stream()
                                .filter(x -> x.getNumericId() == id && x.getState() != TournamentState.ENDED)
                                .findFirst()
                                .orElse(null);
                if (t == null) {
                    sender.sendMessage(CC.translate("&cNo active tournament with ID #" + id + " was found."));
                }
                return t;
            } catch (NumberFormatException e) {
                sender.sendMessage(CC.translate("&cUsage: /tournament info <id>"));
                return null;
            }
        }

        Tournament playerT = tournamentService.getPlayerTournament(sender);
        if (playerT != null) return playerT;

        List<Tournament> active =
                tournamentService.getTournaments().stream()
                        .filter(t -> t.getState() != TournamentState.ENDED)
                        .collect(java.util.stream.Collectors.toList());

        if (active.size() == 1) return active.get(0);

        sender.sendMessage(CC.translate("&cPlease specify a tournament: &f/tournament info <id>"));
        return null;
    }

    /**
     * Finds a tournament based on the provided arguments.
     * - If an ID is given, it searches for that tournament.
     * - If no ID is given, it checks if there's only one active tournament.
     * - If multiple tournaments are active, it prompts the user to specify an ID.
     *
     * @param sender     The player executing the command.
     * @param args       Command arguments after the command name.
     * @param candidates List of candidate tournaments to search from.
     * @return The found Tournament or null if not resolvable.
     */
    private Tournament findTournamentFromArgs(Player sender, String[] args, List<Tournament> candidates) {
        if (args.length > 0) {
            try {
                int id = Integer.parseInt(args[0]);
                Optional<Tournament> found = candidates.stream()
                        .filter(t -> t.getNumericId() == id)
                        .findFirst();
                if (!found.isPresent()) {
                    sender.sendMessage(CC.translate("&cNo tournament with ID #" + id + " was found."));
                    return null;
                }
                return found.get();
            } catch (NumberFormatException e) {
                sender.sendMessage(CC.translate("&cInvalid tournament ID. Please use the numeric ID shown in /tournament list."));
                return null;
            }
        } else {
            if (candidates.size() > 1) {
                sender.sendMessage(CC.translate("&cMultiple tournaments are active. Please specify an ID. Use /tournament list."));
                return null;
            }
            if (candidates.isEmpty()) {
                sender.sendMessage(CC.translate("&cThere are no relevant tournaments."));
                return null;
            }
            return candidates.get(0);
        }
    }

    /**
     * Resolves a join target from args:
     * - /tournament join <id>
     * - /tournament join host <name>
     * - /tournament join <name> (host name without keyword)
     *
     * @param sender     The player executing the command.
     * @param args       Command args after "join".
     * @param candidates Waiting tournaments list.
     * @return The resolved tournament or null when not resolvable.
     */
    private Tournament findJoinTarget(Player sender, String[] args, List<Tournament> candidates) {
        if (args.length == 0) {
            if (candidates.size() == 1) {
                return candidates.get(0);
            }
            sender.sendMessage(CC.translate(
                    "&cMultiple tournaments are available. Use &f/tournament join <id> &cor "
                            + "&f/tournament join host <name>&c.")
            );
            return null;
        }

        if (args[0].equalsIgnoreCase("host")) {
            if (args.length < 2) {
                sender.sendMessage(CC.translate("&cUsage: /tournament join host <hostName>"));
                return null;
            }
            String hostName = args[1];
            List<Tournament> byHost = candidates.stream()
                    .filter(tournament -> tournament.getHostName().equalsIgnoreCase(hostName))
                    .collect(Collectors.toList());

            if (byHost.isEmpty()) {
                sender.sendMessage(CC.translate("&cNo waiting tournaments hosted by &f" + hostName + "&c."));
                return null;
            }

            if (byHost.size() > 1) {
                sender.sendMessage(CC.translate(
                        "&cMultiple tournaments found for host &f" + hostName
                                + "&c. Please use the ID with &f/tournament join <id>&c.")
                );
                return null;
            }
            return byHost.get(0);
        }

        try {
            int id = Integer.parseInt(args[0]);
            return candidates.stream()
                    .filter(tournament -> tournament.getNumericId() == id)
                    .findFirst()
                    .orElseGet(() -> {
                        sender.sendMessage(CC.translate("&cNo tournament with ID #" + id + " was found."));
                        return null;
                    });
        } catch (NumberFormatException ignored) {
            String hostName = args[0];
            List<Tournament> byHost = candidates.stream()
                    .filter(tournament -> tournament.getHostName().equalsIgnoreCase(hostName))
                    .collect(Collectors.toList());

            if (byHost.isEmpty()) {
                sender.sendMessage(CC.translate(
                        "&cNo waiting tournaments found for &f" + hostName
                                + "&c. Use &f/tournament join <id> &cor "
                                + "&f/tournament join host <name>&c.")
                );
                return null;
            }
            if (byHost.size() > 1) {
                sender.sendMessage(CC.translate(
                        "&cMultiple tournaments found for host &f" + hostName
                                + "&c. Please use the ID with &f/tournament join <id>&c.")
                );
                return null;
            }
            return byHost.get(0);
        }
    }

    /**
     * Builds a natural-language list of usernames from a match participant.
     *
     * @param gameParticipant The match participant.
     * @return Conjoined names like "name1 and name2" or "name1, name2 and name3".
     */
    private String formatParticipantNames(GameParticipant<MatchGamePlayer> gameParticipant) {
        List<MatchGamePlayer> allPlayers = gameParticipant.getAllPlayers();
        List<String> names = new ArrayList<>();
        for (MatchGamePlayer gamePlayer : allPlayers) {
            names.add(gamePlayer.getUsername());
        }
        int size = names.size();
        if (size == 0) return "Unknown";
        if (size == 1) return names.get(0);
        if (size == 2) return names.get(0) + " and " + names.get(1);
        String almostAll = String.join(", ", names.subList(0, size - 1));
        return almostAll + " and " + names.get(size - 1);
    }
}