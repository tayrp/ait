package loqor.ait.tardis.console.variant.borealis;

import loqor.ait.AITMod;
import loqor.ait.core.data.schema.console.ConsoleVariantSchema;
import loqor.ait.tardis.console.type.BorealisType;
import loqor.ait.tardis.data.loyalty.Loyalty;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class BorealisVariant extends ConsoleVariantSchema {
	public static final Identifier REFERENCE = new Identifier(AITMod.MOD_ID, "console/borealis");

	public BorealisVariant() {
		super(BorealisType.REFERENCE, REFERENCE, new Loyalty(Loyalty.Type.COMPANION));
	}
}
