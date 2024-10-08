package loqor.ait.tardis.base;

import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import loqor.ait.tardis.Tardis;

/**
 * An interface for something that can be ticked by a tardis Make sure to add
 * whatever it is that needs ticking to {@link Tardis}
 */
public interface TardisTickable { // todo, actually use this class where its needed eg desktop, exterior,
    // console, etc.
    default void tick(MinecraftServer server) {
    }

    default void tick(ServerWorld world) {
    }

    default void tick(MinecraftClient client) {
    }

    default void startTick(MinecraftServer server) {
    }
}
