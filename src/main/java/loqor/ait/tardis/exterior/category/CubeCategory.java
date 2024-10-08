package loqor.ait.tardis.exterior.category;

import net.minecraft.util.Identifier;

import loqor.ait.AITMod;
import loqor.ait.core.data.schema.exterior.ExteriorCategorySchema;

public class CubeCategory extends ExteriorCategorySchema {
    public static final Identifier REFERENCE = new Identifier(AITMod.MOD_ID, "exterior/cube");

    public CubeCategory() {
        super(REFERENCE, "cube");
    }
}
