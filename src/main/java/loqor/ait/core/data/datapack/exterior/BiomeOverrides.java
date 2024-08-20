package loqor.ait.core.data.datapack.exterior;

import java.util.Map;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import loqor.ait.tardis.data.BiomeHandler;
import loqor.ait.tardis.util.EnumMap;
import net.minecraft.util.StringIdentifiable;

public record BiomeOverrides(EnumMap<BiomeHandler.BiomeType, Identifier> lookup) {

    public static BiomeOverrides EMPTY = new BiomeOverrides(new EnumMap<>(
            () -> new BiomeHandler.BiomeType[] {}, i -> null
    ));

    private static EnumMap<BiomeHandler.BiomeType, Identifier> createMap() {
        return new EnumMap<>(() -> BiomeHandler.BiomeType.VALUES, Identifier[]::new);
    }

    private BiomeOverrides(Map<BiomeHandler.BiomeType, Identifier> map) {
        this(createMap());
        this.lookup.putAll(map);
    }

    public Identifier get(BiomeHandler.BiomeType type) {
        return this.lookup.get(type);
    }

    @Environment(EnvType.CLIENT)
    public void validate() {
        ResourceManager manager = MinecraftClient.getInstance().getResourceManager();

        this.lookup.apply(identifier -> {
            if (identifier == null)
                return null;

            return manager.getResource(identifier).isPresent() ? identifier : null;
        });
    }

    public static BiomeOverrides of(Function<BiomeHandler.BiomeType, Identifier> func) {
        EnumMap<BiomeHandler.BiomeType, Identifier> map = createMap();

        for (BiomeHandler.BiomeType type : BiomeHandler.BiomeType.VALUES) {
            map.put(type, func.apply(type));
        }

        return new BiomeOverrides(map);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(BiomeOverrides overrides) {
        return new Builder(overrides);
    }

    public static final MapCodec<BiomeOverrides> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.simpleMap(
                            BiomeHandler.BiomeType.CODEC, Identifier.CODEC,
                            StringIdentifiable.toKeyable(BiomeHandler.BiomeType.VALUES)
                    ).forGetter(overrides -> overrides.lookup)
            ).apply(instance, BiomeOverrides::new));

    public static class Builder {

        private final EnumMap<BiomeHandler.BiomeType, Identifier> map = new EnumMap<>(
                () -> BiomeHandler.BiomeType.VALUES, Identifier[]::new);

        private Builder() { }

        private Builder(BiomeOverrides overrides) {
            for (BiomeHandler.BiomeType type : BiomeHandler.BiomeType.VALUES) {
                this.with(type, overrides.lookup().get(type));
            }
        }

        public Builder with(BiomeHandler.BiomeType type, Identifier id) {
            map.put(type, id);
            return this;
        }

        public Builder with(Function<BiomeHandler.BiomeType, Identifier> func, BiomeHandler.BiomeType... types) {
            for (BiomeHandler.BiomeType type : types) {
                this.with(type, func.apply(type));
            }

            return this;
        }

        public BiomeOverrides build() {
            return new BiomeOverrides(map);
        }
    }
}
