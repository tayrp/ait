package loqor.ait.tardis.console.variant.toyota;

import net.minecraft.util.Identifier;

import loqor.ait.AITMod;
import loqor.ait.core.data.schema.console.ConsoleVariantSchema;
import loqor.ait.tardis.console.type.ToyotaType;
import loqor.ait.tardis.data.loyalty.Loyalty;

public class ToyotaBlueVariant extends ConsoleVariantSchema {
    public static final Identifier REFERENCE = new Identifier(AITMod.MOD_ID, "console/toyota_blue");

    public ToyotaBlueVariant() {
        super(ToyotaType.REFERENCE, REFERENCE, new Loyalty(Loyalty.Type.PILOT));
    }
}
