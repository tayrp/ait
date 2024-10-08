package loqor.ait.tardis.exterior.variant.doom.client;

import org.joml.Vector3f;

import net.minecraft.util.Identifier;

import loqor.ait.AITMod;
import loqor.ait.client.models.exteriors.DoomExteriorModel;
import loqor.ait.client.models.exteriors.ExteriorModel;
import loqor.ait.client.renderers.exteriors.DoomConstants;
import loqor.ait.core.data.datapack.exterior.BiomeOverrides;
import loqor.ait.core.data.schema.exterior.ClientExteriorVariantSchema;

public class ClientDoomVariant extends ClientExteriorVariantSchema {

    public ClientDoomVariant() {
        super(new Identifier(AITMod.MOD_ID, "exterior/doom"));
    }

    @Override
    public ExteriorModel model() {
        return new DoomExteriorModel(DoomExteriorModel.getTexturedModelData().createModel());
    }

    @Override
    public Vector3f sonicItemTranslations() {
        return new Vector3f(0.5f, 1.5f, 0f);
    }

    @Override
    public Identifier texture() {
        return DoomConstants.DOOM_FRONT_BACK;
    }

    @Override
    public Identifier emission() {
        return DoomConstants.DOOM_TEXTURE_EMISSION;
    }

    @Override
    public BiomeOverrides overrides() {
        return null;
    }
}
