package loqor.ait.tardis.link;

import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import loqor.ait.tardis.Tardis;
import loqor.ait.tardis.TardisManager;
import loqor.ait.tardis.wrapper.client.manager.ClientTardisManager;

public abstract class LinkableItem extends Item {

    private final boolean showTooltip;

    public LinkableItem(Settings settings, boolean showTooltip) {
        super(settings);
        this.showTooltip = showTooltip;
    }

    public void link(ItemStack stack, Tardis tardis) {
        this.link(stack, tardis.getUuid());
    }

    public void link(ItemStack stack, UUID uuid) {
        NbtCompound nbt = stack.getOrCreateNbt();

        // FIXME why the fuck is it a string?
        nbt.putString("tardis", uuid.toString());
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        this.handleTooltip(stack, tooltip);
        super.appendTooltip(stack, world, tooltip, context);
    }

    private void handleTooltip(ItemStack stack, List<Text> tooltip) {
        if (!showTooltip)
            return;

        NbtCompound nbt = stack.getOrCreateNbt();
        NbtElement id = nbt.get("tardis");

        if (id == null)
            return;

        if (!Screen.hasShiftDown()) {
            tooltip.add(Text.translatable("tooltip.ait.remoteitem.holdformoreinfo").formatted(Formatting.GRAY)
                    .formatted(Formatting.ITALIC));
            return;
        }

        ClientTardisManager.getInstance().getTardis(UUID.fromString(id.asString()), tardis -> {
            if (tardis != null) {
                tooltip.add(Text.literal("TARDIS: ").formatted(Formatting.BLUE));
                tooltip.add(Text.literal("> " + tardis.stats().getName()));
                tooltip.add(Text.literal("> " + tardis.getUuid().toString().substring(0, 8))
                        .formatted(Formatting.DARK_GRAY));
            }
        });
    }

    public static boolean isOf(World world, ItemStack stack, Tardis tardis) {
        return LinkableItem.getTardis(world, stack) == tardis;
    }

    public static Tardis getTardis(World world, ItemStack stack) {
        return LinkableItem.getTardisFromString(world, stack, "tardis");
    }

    public static UUID getTardisIdFromString(ItemStack stack, String path) {
        NbtCompound nbt = stack.getOrCreateNbt();

        if (!(nbt.contains(path)))
            return null;

        return UUID.fromString(nbt.getString(path));
    }

    public static UUID getTardisIdFromUuid(ItemStack stack, String path) {
        NbtCompound nbt = stack.getOrCreateNbt();

        if (!(nbt.contains(path)))
            return null;

        return nbt.getUuid(path);
    }

    public static Tardis getTardisFromString(World world, ItemStack stack, String path) {
        return LinkableItem.getTardis(world, LinkableItem.getTardisIdFromString(stack, path));
    }

    public static <C> Tardis getTardisFromString(TardisManager<?, C> manager, C c, ItemStack stack, String path) {
        return LinkableItem.getTardis(LinkableItem.getTardisIdFromString(stack, path), c, manager);
    }

    public static Tardis getTardisFromUuid(World world, ItemStack stack, String path) {
        return LinkableItem.getTardis(world, LinkableItem.getTardisIdFromUuid(stack, path));
    }

    public static <C> Tardis getTardisFromUuid(TardisManager<?, C> manager, C c, ItemStack stack, String path) {
        return LinkableItem.getTardis(LinkableItem.getTardisIdFromUuid(stack, path), c, manager);
    }

    public static Tardis getTardis(World world, UUID uuid) {
        return TardisManager.with(world, (o, manager) -> LinkableItem.getTardis(uuid, o, manager));
    }

    public static <C> Tardis getTardis(UUID uuid, C c, TardisManager<?, C> manager) {
        return manager.demandTardis(c, uuid);
    }
}
