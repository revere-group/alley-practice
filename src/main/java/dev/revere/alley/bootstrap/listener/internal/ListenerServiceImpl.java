package dev.revere.alley.bootstrap.listener.internal;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.adapter.core.listener.CoreChatListener;
import dev.revere.alley.feature.cosmetic.CosmeticListener;
import dev.revere.alley.feature.tournament.listener.TournamentListener;
import dev.revere.alley.library.menu.MenuListener;
import dev.revere.alley.feature.arena.listener.ArenaListener;
import dev.revere.alley.feature.combat.listener.CombatListener;
import dev.revere.alley.feature.hotbar.listener.HotbarListener;
import dev.revere.alley.feature.queue.listener.QueueListener;
import dev.revere.alley.feature.server.listener.CraftingListener;
import dev.revere.alley.feature.spawn.listener.SpawnListener;
import dev.revere.alley.bootstrap.listener.ListenerService;
import dev.revere.alley.feature.emoji.listener.EmojiListener;
import dev.revere.alley.feature.item.listener.ItemListener;
import dev.revere.alley.feature.layout.listener.LayoutListener;
import dev.revere.alley.feature.ffa.listener.FFAListener;
import dev.revere.alley.feature.ffa.listener.FFABlockListener;
import dev.revere.alley.feature.ffa.listener.FFACuboidListener;
import dev.revere.alley.feature.ffa.listener.FFADamageListener;
import dev.revere.alley.feature.ffa.listener.FFADisconnectListener;
import dev.revere.alley.feature.match.listener.MatchListener;
import dev.revere.alley.feature.match.listener.types.*;
import dev.revere.alley.feature.match.snapshot.listener.SnapshotListener;
import dev.revere.alley.feature.party.listener.PartyListener;
import dev.revere.alley.bootstrap.AlleyContext;
import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.core.profile.listener.ProfileListener;

import java.util.Arrays;

/**
 * @author Emmy
 * @project alley-practice
 * @since 16/07/2025
 */
@Service(provides = ListenerService.class, priority = 1000)
public class ListenerServiceImpl implements ListenerService {

    @Override
    public void initialize(AlleyContext context) {
        this.registerListeners(context.getPlugin());
    }

    @Override
    public void registerListeners(AlleyPlugin plugin) {
        Arrays.asList(
                new ProfileListener(), // try registering last to see if order changes
                new HotbarListener(),
                new PartyListener(),
                new ArenaListener(),
                new MenuListener(),
                new SpawnListener(),

                new EmojiListener(),
                new CombatListener(),
                new QueueListener(),
                new CoreChatListener(),
                new LayoutListener(),
                new SnapshotListener(),
                new CraftingListener(),

                new ItemListener(),

                new FFAListener(), new FFACuboidListener(),
                new FFABlockListener(), new FFADamageListener(), new FFADisconnectListener(),

                new MatchListener(), new MatchInteractListener(),
                new MatchPearlListener(), new MatchDisconnectListener(),
                new MatchDamageListener(), new MatchChatListener(), new MatchBlockListener(),

                new CosmeticListener(),

                new TournamentListener()

        ).forEach(listener -> plugin.getServer().getPluginManager().registerEvents(listener, plugin));
    }
}