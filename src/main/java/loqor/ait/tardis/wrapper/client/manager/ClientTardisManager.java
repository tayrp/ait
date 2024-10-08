package loqor.ait.tardis.wrapper.client.manager;

import java.util.*;
import java.util.function.Consumer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.GlobalPos;

import loqor.ait.AITMod;
import loqor.ait.client.sounds.ClientSoundManager;
import loqor.ait.core.data.SerialDimension;
import loqor.ait.core.data.base.Exclude;
import loqor.ait.tardis.Tardis;
import loqor.ait.tardis.TardisManager;
import loqor.ait.tardis.wrapper.client.ClientTardis;

public class ClientTardisManager extends TardisManager<ClientTardis, MinecraftClient> {

    private static ClientTardisManager instance;

    private final Multimap<UUID, Consumer<ClientTardis>> subscribers = ArrayListMultimap.create();

    public static void init() {
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT)
            throw new UnsupportedOperationException("Tried to initialize ClientTardisManager on the server!");

        instance = new ClientTardisManager();
    }

    private ClientTardisManager() {
        ClientPlayNetworking.registerGlobalReceiver(SEND, (client, handler, buf, responseSender) -> this.sync(buf));

        ClientPlayNetworking.registerGlobalReceiver(SEND_BULK,
                (client, handler, buf, responseSender) -> this.syncBulk(buf));

        ClientPlayNetworking.registerGlobalReceiver(REMOVE, (client, handler, buf, responseSender) -> this.remove(buf));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null)
                return;

            for (ClientTardis tardis : this.lookup.values()) {
                tardis.tick(client);
            }

            ClientSoundManager.tick(client);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> this.reset());
        ClientLoginConnectionEvents.DISCONNECT.register((client, reason) -> this.reset());
    }

    private void remove(PacketByteBuf buf) {
        this.lookup.remove(buf.readUuid());
    }

    @Override
    public void loadTardis(MinecraftClient client, UUID uuid, @Nullable Consumer<ClientTardis> consumer) {
        if (uuid == null)
            return;

        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(uuid);

        if (consumer != null)
            this.subscribers.put(uuid, consumer);;

        MinecraftClient.getInstance().executeTask(() -> ClientPlayNetworking.send(ASK, data));
    }

    public void loadTardis(UUID uuid, @Nullable Consumer<ClientTardis> consumer) {
        this.loadTardis(MinecraftClient.getInstance(), uuid, consumer);
    }

    @Override
    @Deprecated
    public @Nullable ClientTardis demandTardis(MinecraftClient client, UUID uuid) {
        ClientTardis result = this.lookup.get(uuid);

        if (result == null)
            this.loadTardis(client, uuid, null);

        return result;
    }

    @Deprecated
    public @Nullable ClientTardis demandTardis(UUID uuid) {
        return this.demandTardis(MinecraftClient.getInstance(), uuid);
    }

    public void getTardis(UUID uuid, Consumer<ClientTardis> consumer) {
        this.getTardis(MinecraftClient.getInstance(), uuid, consumer);
    }

    /**
     * Asks the server for a tardis at an exterior position
     */
    public void askTardis(GlobalPos pos) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeGlobalPos(pos);

        ClientPlayNetworking.send(ASK_POS, data);
    }

    private void sync(UUID uuid, String json) {
        try {
            ClientTardis tardis = this.readTardis(this.networkGson, json);
            tardis.travel(); // get a random element. if its null it will complain

            synchronized (this) {
                ClientTardis old = this.lookup.put(tardis);

                if (old != null)
                    old.age();

                for (Consumer<ClientTardis> consumer : this.subscribers.removeAll(uuid)) {
                    consumer.accept(tardis);
                }
            }
        } catch (Throwable t) {
            AITMod.LOGGER.error("Received malformed JSON file {}", json);
            AITMod.LOGGER.error("Failed to deserialize TARDIS data: ", t);
        }
    }

    private void sync(UUID uuid, PacketByteBuf buf) {
        this.sync(uuid, buf.readString());
    }

    private void sync(PacketByteBuf buf) {
        this.sync(buf.readUuid(), buf);
    }

    private void syncBulk(PacketByteBuf buf) {
        int count = buf.readInt();

        for (int i = 0; i < count; i++) {
            this.sync(buf);
        }
    }

    @Override
    protected GsonBuilder createGsonBuilder(Exclude.Strategy strategy) {
        return super.createGsonBuilder(strategy)
                .registerTypeAdapter(SerialDimension.class, new SerialDimension.ClientSerializer())
                .registerTypeAdapter(Tardis.class, ClientTardis.creator());
    }

    @Override
    public void reset() {
        this.subscribers.clear();

        this.forEach(ClientTardis::dispose);
        super.reset();
    }

    public static ClientTardisManager getInstance() {
        return instance;
    }
}
