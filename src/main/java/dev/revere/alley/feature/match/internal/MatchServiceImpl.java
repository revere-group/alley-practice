package dev.revere.alley.feature.match.internal;

import dev.revere.alley.bootstrap.AlleyContext;
import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.common.logger.Logger;
import dev.revere.alley.core.config.ConfigService;
import dev.revere.alley.core.locale.LocaleService;
import dev.revere.alley.core.locale.internal.impl.SettingsLocaleImpl;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.setting.KitSetting;
import dev.revere.alley.feature.kit.setting.types.mode.*;
import dev.revere.alley.feature.match.Match;
import dev.revere.alley.feature.match.MatchService;
import dev.revere.alley.feature.match.internal.types.*;
import dev.revere.alley.feature.match.model.GameParticipant;
import dev.revere.alley.feature.match.model.internal.MatchGamePlayer;
import dev.revere.alley.feature.queue.Queue;
import dev.revere.alley.feature.queue.QueueService;
import dev.revere.alley.feature.match.model.GameParticipant;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.common.logger.Logger;
import dev.revere.alley.feature.tournament.model.Tournament;
import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Remi
 * @project Alley
 * @date 5/21/2024
 */
@Getter
@Service(provides = MatchService.class, priority = 220)
public class MatchServiceImpl implements MatchService {
    @FunctionalInterface
    private interface MatchFactory {
        Match create(Queue queue, Kit kit, Arena arena, boolean isRanked, GameParticipant<MatchGamePlayer> pA, GameParticipant<MatchGamePlayer> pB);
    }

    private final ProfileService profileService;
    private final QueueService queueService;
    private final ConfigService configService;
    private final LocaleService localeService;

    private final List<Match> matches = new CopyOnWriteArrayList<>();
    private final List<String> blockedCommands = new ArrayList<>();
    private final Map<Class<? extends KitSetting>, MatchFactory> matchFactoryRegistry = new LinkedHashMap<>();

    /**
     * DI Constructor for the MatchServiceImpl class.
     *
     * @param profileService The profile service.
     * @param queueService   The queue service.
     * @param configService  The configuration service.
     * @param localeService  The locale service.
     */
    public MatchServiceImpl(ProfileService profileService, QueueService queueService, ConfigService configService, LocaleService localeService) {
        this.profileService = profileService;
        this.queueService = queueService;
        this.configService = configService;
        this.localeService = localeService;
    }

    @Override
    public void initialize(AlleyContext context) {
        this.registerMatchFactories();
        this.loadBlockedCommands();
    }

    @Override
    public void shutdown(AlleyContext context) {
        if (this.matches.isEmpty()) {
            return;
        }
        Logger.info("Ending " + this.matches.size() + " active matches due to server shutdown...");
        new ArrayList<>(this.matches).forEach(Match::endMatch);
        this.matches.clear();
    }

    @Override
    public void addMatch(Match match) {
        if (match != null) {
            this.matches.add(match);
        }
    }

    @Override
    public void removeMatch(Match match) {
        this.matches.remove(match);
    }

    /**
     * Registers all known match types and their creation logic.
     * To add a new gamemode, you only need to add a single line here.
     */
    private void registerMatchFactories() {
        matchFactoryRegistry.put(KitSettingBed.class, BedMatch::new);
        matchFactoryRegistry.put(KitSettingLives.class, LivesMatch::new);
        matchFactoryRegistry.put(KitSettingCheckpoint.class, CheckpointMatch::new);
        matchFactoryRegistry.put(KitSettingHideAndSeek.class, HideAndSeekMatch::new);
        matchFactoryRegistry.put(KitSettingStickFight.class, (q, k, ar, r, pA, pB) -> new RoundsMatch(q, k, ar, r, pA, pB, 5));
        matchFactoryRegistry.put(KitSettingRounds.class, (q, k, ar, r, pA, pB) -> new RoundsMatch(q, k, ar, r, pA, pB, 3));
    }

    @Override
    public void createAndStartMatch(Kit kit, Arena arena, GameParticipant<MatchGamePlayer> participantA, GameParticipant<MatchGamePlayer> participantB, boolean teamMatch, boolean affectStatistics, boolean isRanked) {
        Profile profileA = this.profileService.getProfile(participantA.getPlayers().get(0).getUuid());
        Profile profileB = this.profileService.getProfile(participantB.getPlayers().get(0).getUuid());
        if (profileA.getMatch() != null || profileB.getMatch() != null) {
            return;
        }

        Queue matchingQueue = this.queueService.getQueues().stream()
                .filter(queue -> queue.getKit().equals(kit))
                .findFirst()
                .orElse(null);

        MatchFactory factory = null;
        for (Map.Entry<Class<? extends KitSetting>, MatchFactory> entry : matchFactoryRegistry.entrySet()) {
            if (kit.isSettingEnabled(entry.getKey())) {
                factory = entry.getValue();
                break;
            }
        }

        Match match;
        if (factory != null) {
            match = factory.create(matchingQueue, kit, arena, isRanked, participantA, participantB);
        } else {
            match = new DefaultMatch(matchingQueue, kit, arena, isRanked, participantA, participantB);
        }

        match.setTeamMatch(teamMatch);
        match.setAffectStatistics(affectStatistics);

        this.addMatch(match);
        match.startMatch();
    }

    @Override
    public void createAndStartMatch(Kit kit, Arena arena, List<GameParticipant<MatchGamePlayer>> participants) {
        for (GameParticipant<MatchGamePlayer> participant : participants) {
            Profile profile = this.profileService.getProfile(participant.getLeader().getUuid());
            if (profile != null && profile.getMatch() != null) {
                Logger.warn("Profile " + profile.getName() + " is already in a match. Cannot start a new match.");
                return;
            }
        }

        Queue matchingQueue = this.queueService.getQueues().stream()
                .filter(queue -> queue.getKit().equals(kit))
                .findFirst()
                .orElse(null);

        Match match = new FFAMatch(matchingQueue, kit, arena, participants);
        this.addMatch(match);
        match.startMatch();
    }

    @Override
    public void createTournamentMatch(Tournament tournament, Kit kit, Arena arena, GameParticipant<MatchGamePlayer> participantA, GameParticipant<MatchGamePlayer> participantB) {
        MatchFactory factory = null;
        for (Map.Entry<Class<? extends KitSetting>, MatchFactory> entry : matchFactoryRegistry.entrySet()) {
            if (kit.isSettingEnabled(entry.getKey())) {
                factory = entry.getValue();
                break;
            }
        }

        Match match;
        if (factory != null) {
            match = factory.create(null, kit, arena, false, participantA, participantB);
        } else {
            match = new DefaultMatch(null, kit, arena, false, participantA, participantB);
        }

        match.setTournament(tournament);
        match.setAffectStatistics(false);
        match.setTeamMatch(tournament.getTeamSize() > 1);

        tournament.addActiveMatch(match);

        this.addMatch(match);
        match.startMatch();
    }

    private void loadBlockedCommands() {
        List<String> blockedCommands = this.localeService.getStringListRaw(SettingsLocaleImpl.GAME_BLOCKED_COMMANDS_DURING_MATCH_LIST);
        if (blockedCommands.isEmpty()) {
            Logger.info("No blocked commands found in the configuration. Please check your settings.yml file.");
            return;
        }

        this.blockedCommands.addAll(blockedCommands);
    }
}
