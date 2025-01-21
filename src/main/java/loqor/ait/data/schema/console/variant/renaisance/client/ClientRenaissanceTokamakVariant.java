package loqor.ait.data.schema.console.variant.renaisance.client;

import loqor.ait.client.models.consoles.RenaissanceConsoleModel;
import org.joml.Vector3f;

import net.minecraft.util.Identifier;

import loqor.ait.AITMod;
import loqor.ait.client.models.consoles.ConsoleModel;
import loqor.ait.data.schema.console.ClientConsoleVariantSchema;
import loqor.ait.data.schema.console.variant.renaisance.RenaissanceTokamakVariant;

public class ClientRenaissanceTokamakVariant extends ClientConsoleVariantSchema {
    public static final Identifier TEXTURE = new Identifier(AITMod.MOD_ID,
            ("textures/blockentities/consoles/renaisance_tokamak.png"));
    public static final Identifier EMISSION = new Identifier(AITMod.MOD_ID,
            ("textures/blockentities/consoles/renaisance_tokamak_emission.png"));

    public ClientRenaissanceTokamakVariant() {
        super(RenaissanceTokamakVariant.REFERENCE, RenaissanceTokamakVariant.REFERENCE);
    }

    @Override
    public Identifier texture() {
        return TEXTURE;
    }

    @Override
    public Identifier emission() {
        return EMISSION;
    }

    @Override
    public ConsoleModel model() {
        return new RenaissanceConsoleModel(RenaissanceConsoleModel.getTexturedModelData().createModel());
    }

    @Override
    public Vector3f sonicItemTranslations() {
        return new Vector3f(-0.013f, 1.2f, -0.895f);
    }

    @Override
    public float[] sonicItemRotations() {
        return new float[]{-180f, -30f};
    }

    @Override
    public Vector3f handlesTranslations() {
        return new Vector3f(-0.01f, 1.45f, -0.04f);
    }

    @Override
    public float[] handlesRotations() {
        return new float[]{-180f, 120f};
    }
}
