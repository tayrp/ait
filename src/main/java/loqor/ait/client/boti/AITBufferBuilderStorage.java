/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render;

import java.util.SortedMap;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.util.Util;

import loqor.ait.client.renderers.AITRenderLayers;

@Environment(value=EnvType.CLIENT)
public class AITBufferBuilderStorage extends BufferBuilderStorage {
    private final SortedMap<RenderLayer, BufferBuilder> botiBuilder = Util.make(new Object2ObjectLinkedOpenHashMap(), map -> {
        AITBufferBuilderStorage.assignBufferBuilder(map, AITRenderLayers.getBoti());
    });
    private final VertexConsumerProvider.Immediate botiVertexConsumer = VertexConsumerProvider.immediate(this.botiBuilder, new BufferBuilder(256));

    private static void assignBufferBuilder(Object2ObjectLinkedOpenHashMap<RenderLayer, BufferBuilder> builderStorage, RenderLayer layer) {
        builderStorage.put(layer, new BufferBuilder(layer.getExpectedBufferSize()));
    }

    public VertexConsumerProvider.Immediate getBotiVertexConsumer() {
        return this.botiVertexConsumer;
    }
}
