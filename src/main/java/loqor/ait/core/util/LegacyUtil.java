package loqor.ait.core.util;

import java.util.HashSet;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;

import loqor.ait.AITMod;
import loqor.ait.core.item.SonicItem;
import loqor.ait.registry.impl.SonicRegistry;
import loqor.ait.tardis.TardisHandlersManager;
import loqor.ait.tardis.base.TardisComponent;

/**
 * Omega utility class for handling legacy stuff.
 */
public class LegacyUtil {

    private static final Map<String, TardisComponent.Id> LEGACY_IDS = Map.of("loyalties", TardisComponent.Id.LOYALTY,
            "crashData", TardisComponent.Id.CRASH_DATA, "sequenceHandler", TardisComponent.Id.SEQUENCE);

    public static TardisComponent.Id getLegacyId(String raw) {
        TardisComponent.Id id = LEGACY_IDS.get(raw);

        if (id == null)
            return TardisComponent.Id.valueOf(raw.toUpperCase());

        return id;
    }

    /**
     * @return `true` if the element representing a serialized
     *         {@link TardisComponent} is legacy.
     */
    public static boolean isLegacyComponent(JsonElement element) {
        return !element.isJsonObject();
    }

    /**
     * @return `true` if the map, representing the serialized
     *         {@link TardisHandlersManager} object is legacy.
     */
    public static boolean isHandlersLegacy(Map<String, JsonElement> map) {
        return map.containsKey("tardisId");
    }

    public static boolean shouldFixSonicType(NbtCompound nbt) {
        return nbt.contains(SonicItem.SONIC_TYPE) && nbt.get(SonicItem.SONIC_TYPE).getType() == NbtElement.INT_TYPE;
    }

    /**
     * Fixes the old schema format for sonics.
     */
    public static void fixSonicType(NbtCompound compound) {
        if (compound.get(SonicItem.SONIC_TYPE).getType() != NbtElement.INT_TYPE)
            return;

        AITMod.LOGGER.info("Fixing old sonic data...");
        int id = compound.getInt(SonicItem.SONIC_TYPE);

        compound.remove(SonicItem.SONIC_TYPE);
        compound.putString(SonicItem.SONIC_TYPE, SonicRegistry.getInstance().toList().get(id).id().toString());
    }

    public static Consoles flatConsoles(JsonArray array, JsonDeserializationContext context) {
        Consoles pos = new Consoles();

        array.getAsJsonArray().forEach(element -> pos.add(context.<Console>deserialize(element, Console.class).pos()));

        return pos;
    }

    private record Console(BlockPos pos) {
    }

    public static class Consoles extends HashSet<BlockPos> {
    }
}
