package loqor.ait.tardis.data;

import com.google.gson.*;
import loqor.ait.core.data.base.Exclude;
import loqor.ait.core.util.LegacyUtil;
import loqor.ait.tardis.Tardis;
import loqor.ait.tardis.base.TardisComponent;
import loqor.ait.tardis.base.TardisTickable;
import loqor.ait.tardis.control.sequences.SequenceHandler;
import loqor.ait.tardis.data.loyalty.LoyaltyHandler;
import loqor.ait.tardis.data.permissions.PermissionHandler;
import loqor.ait.tardis.data.properties.PropertiesHolder;
import loqor.ait.tardis.util.EnumMap;
import net.minecraft.server.MinecraftServer;

import java.util.Map;
import java.util.function.Consumer;

public class TardisHandlersManager extends TardisLink {

	@Exclude
	private final EnumMap<Id, TardisComponent> handlers = new EnumMap<>(Id::values, TardisComponent[]::new);

	public TardisHandlersManager() {
        super(Id.HANDLERS);
    }

	@Override
	public void init(Tardis tardis, boolean deserialized) {
		super.init(tardis, deserialized);

		if (!deserialized) {
			this.createHandler(new DoorData());
			this.createHandler(new PropertiesHolder());
			this.createHandler(new WaypointHandler());
			this.createHandler(new LoyaltyHandler());
			this.createHandler(new OvergrownData());
			this.createHandler(new ServerHumHandler());
			this.createHandler(new ServerAlarmHandler());
			this.createHandler(new InteriorChangingHandler());
			this.createHandler(new SequenceHandler());
			this.createHandler(new FuelData());
			this.createHandler(new HADSData());
			this.createHandler(new FlightData());
			this.createHandler(new SiegeData());
			this.createHandler(new CloakData());
			this.createHandler(new StatsData());
			this.createHandler(new TardisCrashData());
			this.createHandler(new SonicHandler());
			this.createHandler(new ShieldData());
			this.createHandler(new BiomeHandler());
			this.createHandler(new PermissionHandler());
		}

		this.forEach(component -> component.init(
				tardis, deserialized)
		);
	}

	private void forEach(Consumer<TardisComponent> consumer) {
		for (TardisComponent component : this.handlers.values()) {
			if (component == null)
				continue;

			consumer.accept(component);
		}
	}

	private void createHandler(TardisComponent component) {
		this.handlers.put(component.getId(), component);
	}

	/**
	 * Called on the END of a servers tick
	 *
	 * @param server the current server
	 */
	public void tick(MinecraftServer server) {
		this.forEach(component -> {
			if (!(component instanceof TardisTickable tickable))
				return;

			tickable.tick(server);
		});
	}

	/**
	 * Called on the START of a servers tick
	 *
	 * @param server the current server
	 */
	public void startTick(MinecraftServer server) {
		this.forEach(component -> {
			if (!(component instanceof TardisTickable tickable))
				return;

			tickable.startTick(server);
		});
	}

	@SuppressWarnings("unchecked")
	public <T extends TardisComponent> T get(Id id) {
		return (T) this.handlers.get(id);
	}

	/**
	 * Do NOT use this setter if you don't know what you're doing. Use {@link loqor.ait.tardis.wrapper.client.ClientTardis#set(TardisComponent)}.
	 */
	@Deprecated
	public <T extends TardisComponent> void set(Id id, T t) {
		this.handlers.put(id, t);
	}

	@Deprecated
	public SonicHandler getSonic() {
		return this.tardis().sonic();
	}

	@Deprecated
	public StatsData getStats() {
		return this.tardis().stats();
	}

	@Deprecated
	public DoorData getDoor() {
		return this.tardis().getDoor();
	}

	@Deprecated
	public PropertiesHolder getProperties() {
		return this.tardis().properties();
	}

	@Deprecated
	public CloakData getCloak() {
		return this.get(Id.CLOAK);
	}

	@Deprecated
	public ShieldData getShields() {
		return this.get(Id.SHIELDS);
	}

	@Deprecated
	public ServerAlarmHandler getAlarms() {
		return this.tardis().alarm();
	}

	@Deprecated
	public WaypointHandler getWaypoints() {
		return this.tardis().waypoint();
	}

	@Deprecated
	public SequenceHandler getSequenceHandler() {
		return this.tardis().sequence();
	}

	@Deprecated
	public FlightData getFlight() {
		return this.tardis().flight();
	}

	@Deprecated
	public FuelData getFuel() {
		return this.tardis().fuel();
	}

	@Deprecated
	public TardisCrashData getCrashData() {
		return this.tardis().crash();
	}

	@Deprecated
	public SiegeData getSiege() {
		return this.get(Id.SIEGE);
	}

	@Deprecated
	public LoyaltyHandler getLoyalties() {
		return this.tardis().loyalty();
	}

	@Deprecated
	public OvergrownData getOvergrown() {
		return this.get(Id.OVERGROWN);
	}

	@Deprecated
	public PermissionHandler getPermissions() {
		return this.get(Id.PERMISSIONS);
	}

	@Deprecated
	public InteriorChangingHandler getInteriorChanger() {
		return this.get(Id.INTERIOR);
	}

	@Deprecated
	public EngineHandler getEngine() {
		return this.get(Id.ENGINE);
	}

	public static Object serializer() {
		return new Serializer();
	}

	static class Serializer implements JsonSerializer<TardisHandlersManager>, JsonDeserializer<TardisHandlersManager>  {

		@Override
		public TardisHandlersManager deserialize(JsonElement json, java.lang.reflect.Type type, JsonDeserializationContext context) throws JsonParseException {
			TardisHandlersManager manager = new TardisHandlersManager();
			Map<String, JsonElement> map = json.getAsJsonObject().asMap();

			boolean legacy = LegacyUtil.isHandlersLegacy(map);

			for (Map.Entry<String, JsonElement> entry : map.entrySet()) {
				String key = entry.getKey();
				JsonElement element = entry.getValue();

				// Skip legacy entries like "tardisId".
				if (LegacyUtil.isLegacyComponent(element))
					continue;

				Id id = legacy ? LegacyUtil.getLegacyId(key) : Id.valueOf(key);

				if (id == null) {
					throw new NullPointerException("Can't find a component id with name '" + entry.getKey() + "'!");
				}

				TardisComponent component = context.deserialize(element, id.clazz());

				manager.set(id, component);
			}

			return manager;
		}

		@Override
		public JsonElement serialize(TardisHandlersManager manager, java.lang.reflect.Type type, JsonSerializationContext context) {
			JsonObject result = new JsonObject();

			manager.forEach(component -> result.add(
                    component.getId().toString(),
                    context.serialize(component)
            ));

			return result;
		}
	}
}
