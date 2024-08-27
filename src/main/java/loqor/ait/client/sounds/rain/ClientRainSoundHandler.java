package loqor.ait.client.sounds.rain;

import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;

import loqor.ait.api.TardisComponent;
import loqor.ait.client.sounds.LoopingSound;
import loqor.ait.client.sounds.PositionedLoopingSound;
import loqor.ait.client.sounds.SoundHandler;
import loqor.ait.client.tardis.ClientTardis;
import loqor.ait.client.util.ClientTardisUtil;
import loqor.ait.core.AITSounds;
import loqor.ait.core.tardis.handler.ExteriorEnvironmentHandler;
import loqor.ait.core.tardis.handler.travel.TravelHandlerBase;

public class ClientRainSoundHandler extends SoundHandler {

    public static LoopingSound RAIN_SOUND;

    public LoopingSound getRainSound(ClientTardis tardis) {
        if (RAIN_SOUND == null)
            RAIN_SOUND = this.createRainSound(tardis);

        return RAIN_SOUND;
    }

    private LoopingSound createRainSound(ClientTardis tardis) {
        if (tardis == null || tardis.getDesktop().doorPos().getPos() == null)
            return null;

        return new PositionedLoopingSound(AITSounds.RAIN, SoundCategory.WEATHER,
                tardis.getDesktop().doorPos().getPos(), 0.2f);
    }

    public static ClientRainSoundHandler create() {
        ClientRainSoundHandler handler = new ClientRainSoundHandler();

        handler.generate(ClientTardisUtil.getCurrentTardis());
        return handler;
    }

    private void generate(ClientTardis tardis) {
        if (RAIN_SOUND == null)
            RAIN_SOUND = createRainSound(tardis);

        this.ofSounds(RAIN_SOUND);
    }

    private boolean shouldPlaySounds(ClientTardis tardis) {
        return tardis != null && tardis.travel().getState() == TravelHandlerBase.State.LANDED
                && tardis.<ExteriorEnvironmentHandler>handler(TardisComponent.Id.ENVIRONMENT).isRaining();
    }

    public void tick(MinecraftClient client) {
        ClientTardis tardis = ClientTardisUtil.getCurrentTardis();

        if (this.sounds == null)
            this.generate(tardis);

        if (this.shouldPlaySounds(tardis)) {
            this.startIfNotPlaying(this.getRainSound(tardis));
        } else {
            this.stopSound(RAIN_SOUND);
        }
    }
}
