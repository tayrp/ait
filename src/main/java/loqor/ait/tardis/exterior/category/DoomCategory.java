package loqor.ait.tardis.exterior.category;

import net.minecraft.util.Identifier;

import loqor.ait.AITMod;
import loqor.ait.core.data.schema.exterior.ExteriorCategorySchema;

public class DoomCategory extends ExteriorCategorySchema {
    public static final Identifier REFERENCE = new Identifier(AITMod.MOD_ID, "exterior/doom");

    public DoomCategory() {
        super(REFERENCE, "doom");
    }
}
