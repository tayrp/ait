package loqor.ait.tardis.wrapper.client.manager;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.GsonBuilder;
import loqor.ait.AITMod;
import loqor.ait.client.sounds.ClientSoundManager;
import loqor.ait.core.data.SerialDimension;
import loqor.ait.core.data.base.Exclude;
import loqor.ait.registry.impl.TardisComponentRegistry;
import loqor.ait.tardis.Tardis;
import loqor.ait.tardis.TardisManager;
import loqor.ait.tardis.base.KeyedTardisComponent;
import loqor.ait.tardis.base.TardisComponent;
import loqor.ait.tardis.data.properties.PropertiesHandler;
import loqor.ait.tardis.wrapper.client.ClientTardis;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.GlobalPos;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

public class ClientTardisManager extends TardisManager<ClientTardis, MinecraftClient> {

	private static ClientTardisManager instance;

	private final Multimap<UUID, Consumer<ClientTardis>> subscribers = ArrayListMultimap.create();

	public static void init() {
		if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT)
			throw new UnsupportedOperationException("Tried to initialize ClientTardisManager on the server!");

		instance = new ClientTardisManager();
	}

	private ClientTardisManager() {
		ClientPlayNetworking.registerGlobalReceiver(SEND,
				(client, handler, buf, responseSender) -> this.sync(buf)
		);

		ClientPlayNetworking.registerGlobalReceiver(REMOVE,
				(client, handler, buf, responseSender) -> this.remove(buf)
		);

		ClientPlayNetworking.registerGlobalReceiver(UPDATE,
				(client, handler, buf, responseSender) -> this.update(client, buf)
		);

		ClientPlayNetworking.registerGlobalReceiver(UPDATE_PROPERTY,
				(client, handler, buf, responseSender) -> this.updatePropertyV2(client, buf)
		);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || client.world == null)
				return;

			for (ClientTardis tardis : this.lookup.values()) {
				tardis.tick(client);
			}

			ClientSoundManager.tick(client);
		});

		ClientLoginConnectionEvents.DISCONNECT.register((client, reason) -> this.reset());
	}

	private void remove(PacketByteBuf buf) {
		UUID tardisid = buf.readUuid();
		this.lookup.remove(tardisid);
	}

	@Override
	public void loadTardis(MinecraftClient client, UUID uuid, @Nullable Consumer<ClientTardis> consumer) {
		if (uuid == null)
			return;

		PacketByteBuf data = PacketByteBufs.create();
		data.writeUuid(uuid);

		if (consumer != null)
			this.subscribers.put(uuid, consumer);

		MinecraftClient.getInstance().executeTask(() ->
				ClientPlayNetworking.send(ASK, data));
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
			long start = System.currentTimeMillis();

			ClientTardis tardis = this.readTardis(this.networkGson, json);
			tardis.travel();
			// AITMod.LOGGER.debug("Received {}", tardis);

			synchronized (this) {
				ClientTardis old = this.lookup.put(tardis);

				if (old != null)
					old.age();

				for (Consumer<ClientTardis> consumer : this.subscribers.removeAll(uuid)) {
					consumer.accept(tardis);
				}

				// AITMod.LOGGER.debug("Synced TARDIS on the client in {}ms", System.currentTimeMillis() - start);
			}
		} catch(Throwable t) {
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

	private void updateProperties(ClientTardis tardis, String key, String type, String value) {
		switch (type) {
			case "string" -> PropertiesHandler.set(tardis, key, value, false);
			case "boolean" -> PropertiesHandler.set(tardis, key, Boolean.parseBoolean(value), false);
			case "int" -> PropertiesHandler.set(tardis, key, Integer.parseInt(value), false);
			case "double" -> PropertiesHandler.set(tardis, key, Double.parseDouble(value), false);
			case "float" -> PropertiesHandler.set(tardis, key, Float.parseFloat(value), false);
			case "identifier" -> PropertiesHandler.set(tardis, key, new Identifier(value), false);
		}
	}

	private void update(MinecraftClient client, PacketByteBuf buf) {
		ClientTardis tardis = this.demandTardis(client, buf.readUuid());

		if (tardis == null)
			return;

		TardisComponent.Id typeId = buf.readEnumConstant(TardisComponent.Id.class);

		if (typeId == TardisComponent.Id.PROPERTIES) {
			this.updateProperties(tardis, buf.readString(), buf.readString(), buf.readString());
			return;
		}

		if (!typeId.mutable())
			return;

		typeId.set(tardis, this.networkGson.fromJson(
				buf.readString(), typeId.clazz())
		);
	}

	private void updatePropertyV2(MinecraftClient client, PacketByteBuf buf) {
		ClientTardis tardis = this.demandTardis(client, buf.readUuid());

		if (tardis == null)
			return;

		byte mode = buf.readByte();
		TardisComponent.IdLike typeId = TardisComponentRegistry.getInstance().get(buf.readVarInt());

		String key = buf.readString();

		if (!(typeId.get(tardis) instanceof KeyedTardisComponent keyed)) {
            AITMod.LOGGER.error("Tried to update an un-keyed component: {}", typeId, new IllegalAccessException());
			return;
		}

		try {
			keyed.update(key, buf, mode);
		} catch (Exception e) {
            AITMod.LOGGER.error("Failed to update property for component {}", typeId, e);
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
