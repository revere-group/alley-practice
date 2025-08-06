package dev.revere.alley.feature.tournament.command;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.common.text.ClickableUtil;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.KitService;
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
        Tournament tournament = tournamentService.getPlayerTournament(player);

        if (tournament == null) {
            player.sendMessage(CC.translate("&cYou are not currently in a tournament."));
            return;
        }

        Optional<TournamentParticipant> participantOpt = findParticipantGroupForPlayer(tournament, player.getUniqueId());
        if (!participantOpt.isPresent()) {
            player.sendMessage(CC.translate("&cCould not find your team information."));
            return;
        }
        TournamentParticipant participant = participantOpt.get();
        int currentPlayers = tournament.getWaitingPool().stream().mapToInt(TournamentParticipant::getSize).sum();
        int maxPlayers = tournament.getMaxTeams() * tournament.getTeamSize();

        player.sendMessage(CC.translate("&6&lTournament Information"));
        player.sendMessage(CC.translate(" &6│ &6Type: &f" + tournament.getDisplayName()));
        player.sendMessage(CC.translate(" &6│ &6Kit: &f" + tournament.getKit().getDisplayName()));
        player.sendMessage(CC.translate(" &6│ &6State: &f" + tournament.getState().getDisplayName()));

        if (tournament.getState() == TournamentState.WAITING || tournament.getState() == TournamentState.STARTING) {
            player.sendMessage(CC.translate(" &6│ &6Players: &f" + currentPlayers + "/" + maxPlayers));
        } else {
            player.sendMessage(CC.translate(" &6│ &6Round: &f" + tournament.getCurrentRound()));
            player.sendMessage(CC.translate(" &6│ &6Teams Left: &f" + tournament.getRoundParticipants().size()));
        }

        player.sendMessage(CC.translate(" &6│ &6Your Team: &f" + participant.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.joining(", "))));
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

        Tournament tournamentToStart = findTournamentFromArgs(player, args, 0, waiting);
        if (tournamentToStart == null) return;

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

        Tournament tournamentToCancel = findTournamentFromArgs(player, args, 0, active);
        if (tournamentToCancel == null) return;

        String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "No reason provided.";
        player.sendMessage(CC.translate("&cAttempting to cancel the " + tournamentToCancel.getDisplayName() + " tournament..."));
        tournamentService.cancelTournament(tournamentToCancel, reason);
    }

    @CommandData(name = "tournament.join")
    public void join(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();
        List<Tournament> joinable = tournamentService.getTournaments().stream()
                .filter(t -> t.getState() == TournamentState.WAITING)
                .collect(Collectors.toList());

        if (joinable.isEmpty()) {
            player.sendMessage(CC.translate("&cNo tournaments are available to join."));
            return;
        }

        Tournament tournamentToJoin = findTournamentFromArgs(player, args, 0, joinable);
        if (tournamentToJoin == null) return;

        tournamentService.joinTournament(player, tournamentToJoin);
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

    private Tournament findTournamentFromArgs(Player sender, String[] args, int idArgumentIndex, List<Tournament> candidates) {
        if (args.length > idArgumentIndex) {
            try {
                int id = Integer.parseInt(args[idArgumentIndex]);
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

    private Optional<TournamentParticipant> findParticipantGroupForPlayer(Tournament tournament, UUID playerUuid) {
        return tournament.getWaitingPool().stream()
                .filter(p -> p.containsPlayer(playerUuid))
                .findFirst()
                .map(Optional::of)
                .orElseGet(() -> tournament.getParticipants().stream()
                        .filter(p -> p.containsPlayer(playerUuid))
                        .findFirst());
    }
}