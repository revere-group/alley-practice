package dev.revere.alley.feature.party.internal;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.bootstrap.AlleyContext;
import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.common.SoundUtil;
import dev.revere.alley.common.reflect.ReflectionService;
import dev.revere.alley.common.reflect.internal.types.TitleReflectionServiceImpl;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.common.text.ClickableUtil;
import dev.revere.alley.core.locale.LocaleService;
import dev.revere.alley.core.locale.internal.impl.SettingsLocaleImpl;
import dev.revere.alley.core.locale.internal.impl.VisualsLocaleImpl;
import dev.revere.alley.core.locale.internal.impl.message.GameMessagesLocaleImpl;
import dev.revere.alley.core.locale.internal.impl.message.GlobalMessagesLocaleImpl;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.enums.ProfileState;
import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.arena.ArenaService;
import dev.revere.alley.feature.cooldown.Cooldown;
import dev.revere.alley.feature.cooldown.CooldownService;
import dev.revere.alley.feature.cooldown.CooldownType;
import dev.revere.alley.feature.hotbar.HotbarService;
import dev.revere.alley.feature.hotbar.HotbarType;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.match.MatchService;
import dev.revere.alley.feature.match.model.GameParticipant;
import dev.revere.alley.feature.match.model.TeamGameParticipant;
import dev.revere.alley.feature.match.model.internal.MatchGamePlayer;
import dev.revere.alley.feature.party.Party;
import dev.revere.alley.feature.party.PartyRequest;
import dev.revere.alley.feature.party.PartyService;
import dev.revere.alley.feature.queue.Queue;
import dev.revere.alley.feature.queue.QueueProfile;
import dev.revere.alley.feature.visibility.VisibilityService;
import lombok.Getter;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Emmy
 * @project Alley
 * @date 16/11/2024 - 22:57
 */
@Getter
@Service(provides = PartyService.class, priority = 230)
public class PartyServiceImpl implements PartyService {
    private final ProfileService profileService;
    private final HotbarService hotbarService;
    private final ReflectionService reflectionService;
    private final CooldownService cooldownService;
    private final VisibilityService visibilityService;
    private final MatchService matchService;
    private final ArenaService arenaService;
    private final LocaleService localeService;

    private final List<Party> parties = new ArrayList<>();
    private final List<PartyRequest> partyRequests = new ArrayList<>();
    private String chatFormat;

    /**
     * DI Constructor for the PartyServiceImpl class.
     *
     * @param profileService    The ProfileService instance.
     * @param hotbarService     The HotbarService instance.
     * @param reflectionService The ReflectionService instance.
     * @param cooldownService   The CooldownService instance.
     * @param visibilityService The VisibilityService instance.
     * @param matchService      The MatchService instance.
     * @param arenaService      The ArenaService instance.
     * @param localeService     The LocaleService instance.
     */
    public PartyServiceImpl(ProfileService profileService, HotbarService hotbarService, ReflectionService reflectionService, CooldownService cooldownService, VisibilityService visibilityService, MatchService matchService, ArenaService arenaService, LocaleService localeService) {
        this.profileService = profileService;
        this.hotbarService = hotbarService;
        this.reflectionService = reflectionService;
        this.cooldownService = cooldownService;
        this.visibilityService = visibilityService;
        this.matchService = matchService;
        this.arenaService = arenaService;
        this.localeService = localeService;
    }
    @Override
    public void initialize(AlleyContext context) {
        this.chatFormat = this.localeService.getString(SettingsLocaleImpl.SERVER_CHAT_FORMAT_PARTY);
    }

    @Override
    public List<Party> getParties() {
        return Collections.unmodifiableList(this.parties);
    }

    @Override
    public void startSplitMatch(Kit kit, Arena arena, Party party) {
        List<Player> allPartyPlayers = party.getMembers().stream()
                .map(Bukkit::getPlayer)
                .collect(Collectors.toList());

        Collections.shuffle(allPartyPlayers);

        Player leaderA = allPartyPlayers.get(0);
        Player leaderB = allPartyPlayers.get(1);

        MatchGamePlayer gameLeaderA = new MatchGamePlayer(leaderA.getUniqueId(), leaderA.getName());
        MatchGamePlayer gameLeaderB = new MatchGamePlayer(leaderB.getUniqueId(), leaderB.getName());

        GameParticipant<MatchGamePlayer> participantA = new TeamGameParticipant<>(gameLeaderA);
        GameParticipant<MatchGamePlayer> participantB = new TeamGameParticipant<>(gameLeaderB);

        int totalPlayers = allPartyPlayers.size();
        int teamATargetSize = totalPlayers / 2;
        int currentTeamACount = 1;

        for (int i = 2; i < allPartyPlayers.size(); i++) {
            Player currentPlayer = allPartyPlayers.get(i);
            MatchGamePlayer gamePlayer = new MatchGamePlayer(currentPlayer.getUniqueId(), currentPlayer.getName());

            if (currentTeamACount < teamATargetSize) {
                participantA.addPlayer(gamePlayer);
                currentTeamACount++;
            } else {
                participantB.addPlayer(gamePlayer);
            }
        }

        this.matchService.createAndStartMatch(
                kit, this.arenaService.selectArenaWithPotentialTemporaryCopy(arena), participantA, participantB, true, false, false
        );
    }

    @Override
    public void startFFAMatch(Kit kit, Arena arena, Party party) {
        List<Player> allPartyPlayers = party.getMembers().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (allPartyPlayers.size() < 2) {
            party.getLeader().sendMessage(CC.translate("&cYou need at least 2 players in your party to start a FFA match."));
            return;
        }

        List<GameParticipant<MatchGamePlayer>> participants = new ArrayList<>();
        for (Player player : allPartyPlayers) {
            MatchGamePlayer gamePlayer = new MatchGamePlayer(player.getUniqueId(), player.getName());
            participants.add(new TeamGameParticipant<>(gamePlayer));
        }

        this.matchService.createAndStartMatch(
                kit, this.arenaService.selectArenaWithPotentialTemporaryCopy(arena), participants
        );
    }

    @Override
    public void createParty(Player player) {
        Profile profile = this.profileService.getProfile(player.getUniqueId());
        if (profile.getState() != ProfileState.LOBBY) {
            player.sendMessage(this.localeService.getString(GlobalMessagesLocaleImpl.ERROR_YOU_MUST_BE_IN_LOBBY));
            return;
        }

        Party party = new Party(player);

        this.parties.add(party);

        profile.setParty(party);

        this.hotbarService.applyHotbarItems(player);

        if (this.localeService.getBoolean(VisualsLocaleImpl.TITLE_PARTY_CREATED_ENABLED_BOOLEAN)) {
            String header = this.localeService.getString(VisualsLocaleImpl.TITLE_PARTY_CREATED_HEADER);
            String footer = this.localeService.getString(VisualsLocaleImpl.TITLE_PARTY_CREATED_FOOTER);

            int fadeIn = this.localeService.getInt(VisualsLocaleImpl.TITLE_PARTY_CREATED_FADE_IN);
            int stay = this.localeService.getInt(VisualsLocaleImpl.TITLE_PARTY_CREATED_STAY);
            int fadeOut = this.localeService.getInt(VisualsLocaleImpl.TITLE_PARTY_CREATED_FADEOUT);

            this.reflectionService.getReflectionService(TitleReflectionServiceImpl.class).sendTitle(
                    player,
                    header,
                    footer,
                    fadeIn, stay, fadeOut
            );
        }

        SoundUtil.playCustomSound(player, Sound.FIREWORK_TWINKLE, 1.0F, 1.0F);

        List<String> createPartyMessage = this.localeService.getStringList(GlobalMessagesLocaleImpl.PARTY_CREATED);
        createPartyMessage.forEach(player::sendMessage);
    }

    @Override
    public void disbandParty(Player leader) {
        Party party = this.getPartyByLeader(leader);
        if (party == null) {
            leader.sendMessage(this.localeService.getString(GlobalMessagesLocaleImpl.ERROR_YOU_NOT_IN_PARTY));
            return;
        }

        party.notifyParty(this.localeService.getString(GlobalMessagesLocaleImpl.PARTY_DISBANDED)
                .replace("{player}", leader.getName())
                .replace("{name-color}", String.valueOf(this.profileService.getProfile(leader.getUniqueId()).getNameColor()))
        );

        for (UUID memberId : new ArrayList<>(party.getMembers())) {
            Player member = Bukkit.getPlayer(memberId);

            Profile profile = this.profileService.getProfile(memberId);
            if (profile != null && profile.getQueueProfile() != null) {
                Queue queue = profile.getQueueProfile().getQueue();
                if (queue != null) {
                    queue.removePlayer(profile.getQueueProfile());
                }
            }

            if (member != null && member.isOnline()) {
                this.setupProfile(member, false);
            }
        }

        this.parties.remove(party);

        Cooldown cooldown = this.cooldownService.getCooldown(leader.getUniqueId(), CooldownType.PARTY_ANNOUNCE_COOLDOWN);
        if (cooldown != null && cooldown.isActive()) {
            cooldown.resetCooldown();
        }

        if (this.localeService.getBoolean(VisualsLocaleImpl.TITLE_PARTY_DISBANDED_ENABLED_BOOLEAN)) {
            String header = this.localeService.getString(VisualsLocaleImpl.TITLE_PARTY_DISBANDED_HEADER);
            String footer = this.localeService.getString(VisualsLocaleImpl.TITLE_PARTY_DISBANDED_FOOTER);

            int fadeIn = this.localeService.getInt(VisualsLocaleImpl.TITLE_PARTY_DISBANDED_FADE_IN);
            int stay = this.localeService.getInt(VisualsLocaleImpl.TITLE_PARTY_DISBANDED_STAY);
            int fadeOut = this.localeService.getInt(VisualsLocaleImpl.TITLE_PARTY_DISBANDED_FADEOUT);

            this.reflectionService.getReflectionService(TitleReflectionServiceImpl.class).sendTitle(
                    leader,
                    header,
                    footer,
                    fadeIn, stay, fadeOut
            );
        }

        SoundUtil.playBanHammer(leader);
    }

    @Override
    public void leaveParty(Player player) {
        Party party = this.getPartyByMember(player.getUniqueId());
        if (party == null) {
            if (player.isOnline()) {
                player.sendMessage(this.localeService.getString(GlobalMessagesLocaleImpl.ERROR_YOU_NOT_IN_PARTY));
            }
            return;
        }

        Profile profile = this.profileService.getProfile(player.getUniqueId());
        QueueProfile queueProfile = profile.getQueueProfile();

        if (queueProfile != null) {
            this.handlePartyMemberLeave(player);
        }

        party.getMembers().remove(player.getUniqueId());

        party.notifyParty(this.localeService.getString(GlobalMessagesLocaleImpl.PARTY_PLAYER_LEFT)
                .replace("{player}", player.getName())
                .replace("{name-color}", String.valueOf(profile.getNameColor()))
                .replace("{current-size}", String.valueOf(party.getMembers().size()))
                .replace("{max-size}", "30") //TODO: Implement party size limit with permissions ect...
        );

        if (this.localeService.getBoolean(VisualsLocaleImpl.TITLE_PARTY_LEFT_ENABLED_BOOLEAN)) {
            String title = this.localeService.getString(VisualsLocaleImpl.TITLE_PARTY_LEFT_HEADER)
                    .replace("{leader}", party.getLeader().getName())
                    .replace("{name-color}", String.valueOf(this.profileService.getProfile(party.getLeader().getUniqueId()).getNameColor()));
            String footer = this.localeService.getString(VisualsLocaleImpl.TITLE_PARTY_LEFT_FOOTER)
                    .replace("{leader}", party.getLeader().getName())
                    .replace("{name-color}", String.valueOf(this.profileService.getProfile(party.getLeader().getUniqueId()).getNameColor()));
            int fadeIn = this.localeService.getInt(VisualsLocaleImpl.TITLE_PARTY_LEFT_FADE_IN);
            int stay = this.localeService.getInt(VisualsLocaleImpl.TITLE_PARTY_LEFT_STAY);
            int fadeOut = this.localeService.getInt(VisualsLocaleImpl.TITLE_PARTY_LEFT_FADEOUT);

            this.reflectionService.getReflectionService(TitleReflectionServiceImpl.class).sendTitle(
                    player,
                    title,
                    footer,
                    fadeIn, stay, fadeOut
            );
        }

        this.setupProfile(player, false);
    }

    @Override
    public void kickMember(Player leader, Player member) {
        Party party = this.getPartyByLeader(leader);
        if (party == null) {

            leader.sendMessage(this.localeService.getString(GlobalMessagesLocaleImpl.ERROR_YOU_NOT_PARTY_LEADER));
            return;
        }

        if (party.getLeader().equals(member)) {
            leader.sendMessage(CC.translate("&cYou cannot kick the party leader."));
            return;
        }

        if (!party.getMembers().contains(member.getUniqueId())) {
            leader.sendMessage(CC.translate("&cThat player is not in your party."));
            return;
        }

        Profile profile = this.profileService.getProfile(member.getUniqueId());
        QueueProfile queueProfile = profile.getQueueProfile();

        party.getMembers().remove(member.getUniqueId());

        party.notifyParty(this.localeService.getString(GlobalMessagesLocaleImpl.PARTY_PLAYER_KICKED)
                .replace("{player}", member.getName())
                .replace("{name-color}", String.valueOf(profile.getNameColor()))
                .replace("{current-size}", String.valueOf(party.getMembers().size()))
                .replace("{max-size}", "30") //TODO: Implement party size limit with permissions ect...
        );

        if (queueProfile != null) {
            this.handlePartyMemberLeave(member);
        }

        this.setupProfile(member, false);
    }

    @Override
    public void banMember(Player leader, Player target) {
        Party party = this.getPartyByLeader(leader);
        if (party == null) {
            leader.sendMessage(this.localeService.getString(GlobalMessagesLocaleImpl.ERROR_YOU_NOT_PARTY_LEADER));
            return;
        }

        if (party.getLeader().equals(target)) {
            leader.sendMessage(CC.translate("&cYou cannot ban the party leader."));
            return;
        }

        if (!party.getMembers().contains(target.getUniqueId())) {
            leader.sendMessage(CC.translate("&cThat player is not in your party."));
            return;
        }

        Profile profile = this.profileService.getProfile(target.getUniqueId());
        QueueProfile queueProfile = profile.getQueueProfile();

        party.getBannedPlayers().add(target.getUniqueId());
        party.getMembers().remove(target.getUniqueId());

        if (queueProfile != null) {
            this.handlePartyMemberLeave(target);
        }

        this.setupProfile(target, false);

        party.notifyParty(this.localeService.getString(GlobalMessagesLocaleImpl.PARTY_PLAYER_BANNED)
                .replace("{player}", target.getName())
                .replace("{name-color}", String.valueOf(profile.getNameColor()))
                .replace("{current-size}", String.valueOf(party.getMembers().size()))
                .replace("{max-size}", "30") //TODO: Implement party size limit with permissions ect...
        );
    }

    @Override
    public void unbanMember(Player leader, Player target) {
        Party party = this.getPartyByLeader(leader);
        if (party == null) {
            leader.sendMessage(this.localeService.getString(GlobalMessagesLocaleImpl.ERROR_YOU_NOT_PARTY_LEADER));
            return;
        }

        if (!party.getBannedPlayers().contains(target.getUniqueId())) {
            leader.sendMessage(CC.translate("&cThat player is not banned from your party."));
            return;
        }

        party.getBannedPlayers().remove(target.getUniqueId());
        party.notifyParty(CC.translate("&6" + target.getName() + " &ahas been unbanned from the party and is now able to join again."));
        target.sendMessage(CC.translate("&aYou have been unbanned from &6" + party.getLeader().getName() + "'s &aparty."));
    }

    @Override
    public void joinParty(Player player, Player leader) {
        Profile profile = this.profileService.getProfile(player.getUniqueId());
        if (profile.getState() != ProfileState.LOBBY) {
            player.sendMessage(AlleyPlugin.getInstance().getService(LocaleService.class).getString(GlobalMessagesLocaleImpl.ERROR_YOU_MUST_BE_IN_LOBBY));
            return;
        }
        Party party = this.getPartyByLeader(leader);
        if (party == null) {
            player.sendMessage(CC.translate("&cThis party does not exist."));
            return;
        }

        Party yourParty = this.getPartyByLeader(player);
        if (yourParty != null) {
            player.sendMessage(localeService.getString(GlobalMessagesLocaleImpl.ERROR_YOU_ALREADY_IN_PARTY));
            return;
        }

        if (party.getLeader() == player) {
            player.sendMessage(CC.translate("&cYou cannot join your own party."));
            return;
        }

        if (party.getMembers().contains(player.getUniqueId())) {
            player.sendMessage(localeService.getString(GlobalMessagesLocaleImpl.ERROR_YOU_ALREADY_IN_THIS_PARTY));
            return;
        }

        if (party.getBannedPlayers().contains(player.getUniqueId())) {
            player.sendMessage(localeService.getString(GlobalMessagesLocaleImpl.ERROR_YOU_BANNED_FROM_PARTY)
                    .replace("{player}", leader.getName())
                    .replace("{name-color", String.valueOf(profileService.getProfile(leader.getUniqueId()).getNameColor()))
            );
            return;
        }

        Profile leaderProfile = this.profileService.getProfile(leader.getUniqueId());
        QueueProfile queueProfile = leaderProfile.getQueueProfile();

        if (queueProfile != null) {
            player.sendMessage(CC.translate("&cYou can't join the party as they are already in a queue."));
            return;
        }

        party.getMembers().add(player.getUniqueId());

        party.notifyParty(localeService.getString(GlobalMessagesLocaleImpl.PARTY_PLAYER_JOINED)
                .replace("{player}", player.getName())
                .replace("{name-color}", String.valueOf(profile.getNameColor()))
                .replace("{current-size}", String.valueOf(party.getMembers().size()))
                .replace("{max-size}", "30") //TODO: Implement party size limit with permissions ect...
        );

        List<String> joinedMessage = this.localeService.getStringList(GlobalMessagesLocaleImpl.PARTY_YOU_JOINED);
        for (String line : joinedMessage) {
            player.sendMessage(line
                    .replace("{leader}", leader.getName())
                    .replace("{name-color}", String.valueOf(leaderProfile.getNameColor()))
            );
        }

        if (this.localeService.getBoolean(VisualsLocaleImpl.TITLE_PARTY_JOINED_ENABLED_BOOLEAN)) {
            String title = this.localeService.getString(VisualsLocaleImpl.TITLE_PARTY_JOINED_HEADER)
                    .replace("{leader}", leader.getName())
                    .replace("{name-color}", String.valueOf(this.profileService.getProfile(leader.getUniqueId()).getNameColor()));
            String footer = this.localeService.getString(VisualsLocaleImpl.TITLE_PARTY_JOINED_FOOTER)
                    .replace("{leader}", leader.getName())
                    .replace("{name-color}", String.valueOf(this.profileService.getProfile(leader.getUniqueId()).getNameColor()));
            int fadeIn = this.localeService.getInt(VisualsLocaleImpl.TITLE_PARTY_JOINED_FADE_IN);
            int stay = this.localeService.getInt(VisualsLocaleImpl.TITLE_PARTY_JOINED_STAY);
            int fadeOut = this.localeService.getInt(VisualsLocaleImpl.TITLE_PARTY_JOINED_FADEOUT);

            this.reflectionService.getReflectionService(TitleReflectionServiceImpl.class).sendTitle(
                    player,
                    title,
                    footer,
                    fadeIn, stay, fadeOut
            );
        }

        this.setupProfile(player, true);
    }

    @Override
    public PartyRequest getRequest(Player player) {
        return this.partyRequests.stream()
                .filter(request -> request.getTarget().equals(player))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void removeRequest(PartyRequest request) {
        this.partyRequests.remove(request);
    }

    @Override
    public String getChatFormat() {
        return chatFormat;
    }

    /**
     * Sets up the profile of a player.
     *
     * @param player The player to set up the profile for.
     * @param join   Whether the player is joining a party.
     */
    private void setupProfile(Player player, boolean join) {
        Profile profile = this.profileService.getProfile(player.getUniqueId());
        profile.setParty(join ? this.getPartyByMember(player.getUniqueId()) : null);

        if (profile.getMatch() != null) {
            return;
        }

        profile.setState(ProfileState.LOBBY);
        this.hotbarService.applyHotbarItems(player, join ? HotbarType.PARTY : HotbarType.LOBBY);
        this.visibilityService.updateVisibility(player);
    }

    @Override
    public void sendInvite(Party party, Player sender, Player target) {
        if (party == null) return;

        PartyRequest request = new PartyRequest(sender, target);
        this.partyRequests.add(request);

        List<String> sentMessage = this.localeService.getStringList(GameMessagesLocaleImpl.PARTY_INVITATION_SENT);
        sentMessage = sentMessage.stream().map(line -> line
                .replace("{target}", target.getName())
                .replace("{name-color}", String.valueOf(this.profileService.getProfile(target.getUniqueId()).getNameColor()))
                .replace("{sender}", sender.getName())
                .replace("{party-size}", String.valueOf(party.getMembers().size()))
        ).collect(Collectors.toList());
        sentMessage.forEach(line -> sender.sendMessage(CC.translate(line)));

        String clickableFormat = this.localeService.getString(GameMessagesLocaleImpl.PARTY_INVITATION_RECEIVED_CLICKABLE_FORMAT)
                .replace("{sender}", sender.getName());
        String command = this.localeService.getString(GameMessagesLocaleImpl.PARTY_INVITATION_RECEIVED_CLICKABLE_COMMAND)
                .replace("{sender}", sender.getName());
        String hover = this.localeService.getString(GameMessagesLocaleImpl.PARTY_INVITATION_RECEIVED_CLICKABLE_HOVER)
                .replace("{sender}", sender.getName());

        TextComponent clickableComponent = ClickableUtil.createComponent(
                clickableFormat,
                command,
                hover
        );

        List<String> message = this.localeService.getStringList(GameMessagesLocaleImpl.PARTY_INVITATION_RECEIVED);
        message = message.stream().map(line -> line
                .replace("{sender}", sender.getName())
                .replace("{name-color}", String.valueOf(this.profileService.getProfile(sender.getUniqueId()).getNameColor()))
                .replace("{party-size}", String.valueOf(party.getMembers().size()))
        ).collect(Collectors.toList());
        message.forEach(line -> {
            if (line.contains("{clickable}")) {
                target.spigot().sendMessage(clickableComponent);
            } else {
                target.sendMessage(CC.translate(line));
            }
        });
    }

    @Override
    public Party getPartyByLeader(Player player) {
        return this.parties.stream()
                .filter(party -> party.getLeader().equals(player))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Party getPartyByMember(UUID uuid) {
        return this.parties.stream()
                .filter(party -> party.getMembers().contains(uuid))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Party getParty(Player player) {
        Party party = getPartyByLeader(player);
        if (party != null) {
            return party;
        }
        return getPartyByMember(player.getUniqueId());
    }

    @Override
    public void announceParty(Party party) {
        String clickableFormat = this.localeService.getString(GameMessagesLocaleImpl.PARTY_ANNOUNCEMENT_CLICKABLE_FORMAT)
                .replace("{leader}", party.getLeader().getName());
        String command = this.localeService.getString(GameMessagesLocaleImpl.PARTY_ANNOUNCEMENT_CLICKABLE_COMMAND)
                .replace("{leader}", party.getLeader().getName());
        String hover = this.localeService.getString(GameMessagesLocaleImpl.PARTY_ANNOUNCEMENT_CLICKABLE_HOVER)
                .replace("{leader}", party.getLeader().getName());

        TextComponent clickableComponent = ClickableUtil.createComponent(
                clickableFormat,
                command,
                hover
        );

        AlleyPlugin.getInstance().getServer().getOnlinePlayers().forEach(player -> {
            List<String> message = this.localeService.getStringList(GameMessagesLocaleImpl.PARTY_ANNOUNCEMENT);
            message = message.stream()
                    .map(line -> line.replace("{leader}", party.getLeader().getName())
                            .replace("{name-color}", String.valueOf(this.profileService.getProfile(party.getLeader().getUniqueId()).getNameColor()))
                            .replace("{party-size}", String.valueOf(party.getMembers().size()))
                    )
                    .collect(Collectors.toList());
            message.forEach(line -> {
                if (line.contains("{clickable}")) {
                    player.spigot().sendMessage(clickableComponent);
                } else {
                    player.sendMessage(CC.translate(line));
                }
            });
        });
    }

    /**
     * Handles a player leaving a party, specifically notifying the QueueService if they were queuing.
     * This method should be called whenever a party member disconnects or leaves their party.
     *
     * @param player The player who left the party (or disconnected).
     */
    public void handlePartyMemberLeave(Player player) {
        if (player == null) return;

        Profile profile = profileService.getProfile(player.getUniqueId());
        if (profile == null || profile.getQueueProfile() == null || profile.getMatch() != null) {
            return;
        }

        QueueProfile associatedQueueProfile = profile.getQueueProfile();
        Queue queue = associatedQueueProfile.getQueue();
        if (queue == null) {
            return;
        }

        if (queue.isDuos()) {
            Player leader = Bukkit.getPlayer(associatedQueueProfile.getUuid());
            Party party = getPartyByLeader(leader);
            if (party.getMembers().size() < 2) {
                leader.sendMessage(CC.translate("&eA party member has left/disconnected. You are now queuing solo for duos."));
            }
        }

        profile.setQueueProfile(null);
    }
}