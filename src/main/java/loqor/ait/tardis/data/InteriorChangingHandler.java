package loqor.ait.tardis.data;

import loqor.ait.AITMod;
import loqor.ait.core.util.DeltaTimeManager;
import loqor.ait.registry.impl.DesktopRegistry;
import loqor.ait.tardis.Tardis;
import loqor.ait.tardis.data.properties.PropertiesHandler;
import loqor.ait.tardis.data.properties.PropertiesHolder;
import loqor.ait.tardis.util.TardisUtil;
import loqor.ait.tardis.TardisDesktopSchema;
import loqor.ait.tardis.TardisTravel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Random;

public class InteriorChangingHandler extends TardisLink {
	private static final int WARN_TIME = 10 * 40;
	public static final String IS_REGENERATING = "is_regenerating";
	public static final String QUEUED_INTERIOR = "queued_interior";
	public static final Identifier CHANGE_DESKTOP = new Identifier(AITMod.MOD_ID, "change_desktop");
	private static Random random;
	private int ticks; // this shouldnt rly be stored in propertieshandler, will cause packet spam

	public InteriorChangingHandler() {
		super(Id.INTERIOR);
	}

	private void setGenerating(boolean var) {
		PropertiesHandler.set(this.tardis(), IS_REGENERATING, var);
	}

	public boolean isGenerating() {
		return PropertiesHandler.getBool(this.tardis().properties(), IS_REGENERATING);
	}

	private void setTicks(int var) {
		this.ticks = var;
	}

	public int getTicks() {
		return this.ticks;
	}

	public boolean hasReachedMax() {
		return getTicks() >= WARN_TIME;
	}

	private void setQueuedInterior(TardisDesktopSchema schema) {
		PropertiesHandler.set(this.tardis(), QUEUED_INTERIOR, schema.id());
	}

	public TardisDesktopSchema getQueuedInterior() {
		return DesktopRegistry.getInstance().get(PropertiesHandler.getIdentifier(this.tardis().properties(), QUEUED_INTERIOR));
	}

	public void queueInteriorChange(TardisDesktopSchema schema) {
		Tardis tardis = this.tardis();

		if (!tardis.isGrowth() && !tardis.hasPower() && !tardis.crash().isToxic()) return;

		if (tardis.fuel().getCurrentFuel() < 5000 && !(tardis.isGrowth() && tardis.hasGrowthDesktop())) {
			for (PlayerEntity player : TardisUtil.getPlayersInInterior(tardis)) {
				player.sendMessage(Text.translatable("tardis.message.interiorchange.not_enough_fuel").formatted(Formatting.RED), true);
				return;
			}
		}

		setQueuedInterior(schema);
		setTicks(0);
		setGenerating(true);
		DeltaTimeManager.createDelay("interior_change-" + tardis.getUuid().toString(), 100L);
		tardis.alarm().enable();

		tardis.getDesktop().clearConsoles();

		if (!(tardis.hasGrowthDesktop()))
			tardis.removeFuel(5000 * (tardis.tardisHammerAnnoyance + 1));
	}

	private void onCompletion() {
		Tardis tardis = this.tardis();
		PropertiesHolder properties = tardis.properties();

		setGenerating(false);
		clearedOldInterior = false;

		tardis.alarm().disable();

		boolean previouslyLocked = PropertiesHandler.getBool(properties, PropertiesHandler.PREVIOUSLY_LOCKED);
		DoorData.lockTardis(previouslyLocked, tardis, null, false);

		if (tardis.hasGrowthExterior()) {
			PropertiesHandler.set(tardis, PropertiesHandler.HANDBRAKE, false);
			PropertiesHandler.set(tardis, PropertiesHandler.AUTO_LAND, true);

			tardis.getTravel().dematerialise(true, true);
		}
	}

	private void warnPlayers() {
		for (PlayerEntity player : TardisUtil.getPlayersInInterior(this.tardis())) {
			player.sendMessage(Text.translatable("tardis.message.interiorchange.warning").formatted(Formatting.RED), true);
		}
	}

	private boolean isInteriorEmpty() {
		return TardisUtil.getPlayersInInterior(this.tardis()).isEmpty();
	}

	public static Random random() {
		if (random == null)
			random = new Random();

		return random;
	}

	private boolean clearedOldInterior = false;

	@Override
	public void tick(MinecraftServer server) {
		super.tick(server);

		if (!isGenerating())
			return;

		if (DeltaTimeManager.isStillWaitingOnDelay("interior_change-" + this.tardis().getUuid().toString()))
			return;

		TardisTravel travel = this.tardis().getTravel();

		if (travel.getState() == TardisTravel.State.FLIGHT)
			travel.crash();

		if (isGenerating()) {
			if (!this.tardis().alarm().isEnabled())
				this.tardis().alarm().enable();
		}

		if (!this.tardis().hasPower()) {
			setGenerating(false);
			this.tardis().alarm().disable();
			return;
		}

		if (!isInteriorEmpty()) {
			warnPlayers();
			return;
		}

		if (isInteriorEmpty() && !this.tardis().getDoor().locked()) {
			DoorData.lockTardis(true, this.tardis(), null, true);
		}
		if (isInteriorEmpty() && !clearedOldInterior) {
			this.tardis().getDesktop().clearOldInterior(getQueuedInterior());
			DeltaTimeManager.createDelay("interior_change-" + this.tardis().getUuid().toString(), 15000L);
			clearedOldInterior = true;
			return;
		}
		if (isInteriorEmpty() && clearedOldInterior) {
			this.tardis().getDesktop().changeInterior(getQueuedInterior());
			onCompletion();
		}
	}
}
