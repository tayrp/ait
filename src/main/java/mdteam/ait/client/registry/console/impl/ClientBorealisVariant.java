package mdteam.ait.client.registry.console.impl;

import mdteam.ait.AITMod;
import mdteam.ait.client.models.consoles.BorealisConsoleModel;
import mdteam.ait.client.models.consoles.ConsoleModel;
import mdteam.ait.client.registry.console.ClientConsoleVariantSchema;
import mdteam.ait.tardis.variant.console.BorealisVariant;
import net.minecraft.util.Identifier;

public class ClientBorealisVariant extends ClientConsoleVariantSchema {
    public static final Identifier TEXTURE = new Identifier(AITMod.MOD_ID, ("textures/blockentities/consoles/borealis_console.png"));
    public static final Identifier EMISSION = new Identifier(AITMod.MOD_ID, ("textures/blockentities/consoles/borealis_console_emission.png"));
    public ClientBorealisVariant() {
        super(BorealisVariant.REFERENCE);
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
        return new BorealisConsoleModel(BorealisConsoleModel.getTexturedModelData().createModel());
    }
}
