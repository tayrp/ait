package mdteam.ait.client.sounds.flight;

import mdteam.ait.client.sounds.LoopingSound;
import mdteam.ait.client.util.ClientTardisUtil;
import mdteam.ait.core.AITDimensions;
import mdteam.ait.core.AITSounds;
import mdteam.ait.tardis.Tardis;
import mdteam.ait.tardis.TardisTravel;
import mdteam.ait.tardis.data.properties.PropertiesHandler;
import mdteam.ait.tardis.util.SoundHandler;
import mdteam.ait.tardis.util.TardisUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.sound.SoundCategory;

import java.util.ArrayList;

// All this is CLIENT ONLY!!
// Loqor, if you dont understand DONT TOUCH or ask me! - doozoo

// todo this is not positioned at the console anymore, as checking to see if an individual sound is playing at each console doesnt appear to be possible (?)
// todo or just make it play from the closest console ( do this one )
public class ClientFlightHandler extends SoundHandler {
    public static double MAX_DISTANCE = 16; // distance from console before the sound stops
    public static LoopingSound FLIGHT;
    protected ClientFlightHandler() {}

    public LoopingSound getFlightLoop() {
        if (FLIGHT == null) FLIGHT = createFlightSound();

        return FLIGHT;
    }
    private LoopingSound createFlightSound() {
        if(tardis().getHandlers().getCrashData().isToxic() || tardis().getHandlers().getCrashData().isUnstable()) {
            return new FlightSound(AITSounds.UNSTABLE_FLIGHT_LOOP, SoundCategory.BLOCKS, 1f);
        }
        return new FlightSound(AITSounds.FLIGHT_LOOP, SoundCategory.BLOCKS, 1f);
    }
    public static ClientFlightHandler create() {
        if (MinecraftClient.getInstance().player == null) return null;

        ClientFlightHandler handler = new ClientFlightHandler();
        handler.generate();
        return handler;
    }

    private void generate() {
        if (tardis() == null) return;

        if (FLIGHT == null) FLIGHT = createFlightSound();

        this.sounds = new ArrayList<>();
        this.sounds.add(
                FLIGHT
        );
    }

    public boolean isPlayerInATardis() {
        if (MinecraftClient.getInstance().world == null || MinecraftClient.getInstance().world.getRegistryKey() != AITDimensions.TARDIS_DIM_WORLD) return false;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        Tardis found = TardisUtil.findTardisByInterior(player.getBlockPos(), false);

        return found != null;
    }

    public Tardis tardis() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return null;
        return TardisUtil.findTardisByInterior(player.getBlockPos(), false);
    }

    private void playFlightSound() {
        this.startIfNotPlaying(this.getFlightLoop());
        this.getFlightLoop().tick();
    }

    private boolean shouldPlaySounds() {
        return (ClientTardisUtil.distanceFromConsole() < MAX_DISTANCE) && (inFlight() || hasThrottleAndHandbrakeDown()) && tardis().hasPower();
    }

    private boolean inFlight() {
        return (isPlayerInATardis() && tardis() != null && tardis().getTravel().getState() == TardisTravel.State.FLIGHT);
    }
    public boolean hasThrottleAndHandbrakeDown() {
        return (isPlayerInATardis() && tardis() != null && tardis().getTravel().getSpeed() > 0 && PropertiesHandler.getBool(tardis().getHandlers().getProperties(), PropertiesHandler.HANDBRAKE));
    }

    public void tick(MinecraftClient client) {
        if (this.sounds == null) this.generate();

        if (shouldPlaySounds()) {
            this.playFlightSound();
        }
        else {
            this.stopSounds();
        }
    }
}
