package loqor.ait.tardis.exterior.variant.classic.client;

import org.joml.Vector3f;

import loqor.ait.core.data.datapack.exterior.BiomeOverrides;
import loqor.ait.tardis.data.BiomeHandler;

public class ClientClassicBoxDefinitiveVariant extends ClientClassicBoxVariant {
    private final BiomeOverrides OVERRIDES = BiomeOverrides.builder(ClientClassicBoxVariant.OVERRIDES)
            .with(type -> type.getTexture(this.texture()), BiomeHandler.BiomeType.CHERRY, BiomeHandler.BiomeType.CHORUS,
                    BiomeHandler.BiomeType.SNOWY, BiomeHandler.BiomeType.SCULK)
            .build();

    public ClientClassicBoxDefinitiveVariant() {
        super("definitive");
    }

    @Override
    public Vector3f sonicItemTranslations() {
        return new Vector3f(0.55f, 1.125f, 1.165f);
    }

    @Override
    public BiomeOverrides overrides() {
        return OVERRIDES;
    }
}
