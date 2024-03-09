package mdteam.ait.client.screens;

import com.google.common.collect.Lists;
import mdteam.ait.AITMod;
import mdteam.ait.client.util.ClientTardisUtil;
import mdteam.ait.core.item.SonicItem;
import mdteam.ait.registry.CategoryRegistry;
import mdteam.ait.tardis.data.SonicHandler;
import mdteam.ait.tardis.exterior.category.ExteriorCategorySchema;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableTextWidget;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

import java.awt.*;
import java.util.List;
import java.util.UUID;

public class SonicSettingsScreen extends ConsoleScreen {
    private static final Identifier BACKGROUND = new Identifier(AITMod.MOD_ID, "textures/gui/tardis/consoles/monitors/sonic_selection.png");
    private final List<ButtonWidget> buttons = Lists.newArrayList();
    int bgHeight = 126;
    int bgWidth = 201;
    int left, top;
    int choicesCount = 0;
    private final Screen parent;
    private int selectedSonic;

    public SonicSettingsScreen(UUID tardis, UUID console, Screen parent) {
        super(Text.translatable("screen.ait.sonicsettings.title"), tardis, console);
        this.parent = parent;
        updateTardis();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    protected void init() {
        this.selectedSonic = tardis().getHandlers().getSonic().get(SonicHandler.HAS_CONSOLE_SONIC).getOrCreateNbt().getInt(SonicItem.SONIC_TYPE);
        this.top = (this.height - this.bgHeight) / 2; // this means everythings centered and scaling, same for below
        this.left = (this.width - this.bgWidth) / 2;
        this.createButtons();

        super.init();
    }

    private void createButtons() {
        choicesCount = 0;
        this.buttons.clear();

        Text applyText = Text.literal("Apply");
        this.addButton(new PressableTextWidget((int) (left + (bgWidth * 0.21f)), (int) (top + (bgHeight * 0.878f)),
                this.textRenderer.getWidth(applyText), 10, Text.literal("     "), button -> {
            sendSonicChangePacket();
        }, this.textRenderer));

        Text back = Text.translatable("screen.ait.sonicsettings.back");
        this.addButton(new PressableTextWidget((width / 2 - 94), (height / 2 - 58),
                this.textRenderer.getWidth(back), 10, back, button -> backToInteriorSettings(),
                this.textRenderer));

        this.addButton(
                new PressableTextWidget(
                        (int) (left + (bgWidth * 0.06f)),
                        (int) (top + (bgHeight * 0.882f)),
                        this.textRenderer.getWidth("<"),
                        10,
                        Text.literal(" "),
                        button -> this.getLastSelectedSonic(),
                        this.textRenderer
                )
        );
        this.addButton(
                new PressableTextWidget(
                        (int) (left + (bgWidth * 0.47f)),
                        (int) (top + (bgHeight * 0.882f)),
                        this.textRenderer.getWidth(">"),
                        10,
                        Text.literal(" "),
                        button -> this.getNextSelectedSonic(),
                        this.textRenderer
                )
        );

    }

    public void backToInteriorSettings() {
        MinecraftClient.getInstance().setScreen(this.parent);
    }

    public void sendSonicChangePacket() {
        if(!tardis().getHandlers().getSonic().hasSonic(SonicHandler.HAS_CONSOLE_SONIC)) return;
        tardis().getHandlers().getSonic().get(SonicHandler.HAS_CONSOLE_SONIC).getOrCreateNbt().putInt(SonicItem.SONIC_TYPE, this.selectedSonic);
        ClientTardisUtil.changeSonicWithScreen(this.tardisId, this.selectedSonic);
    }


    private <T extends ClickableWidget> void addButton(T button) {
        this.addDrawableChild(button);
        button.active = true; // this whole method is unnecessary bc it defaults to true ( ?? ) - does it though?
        this.buttons.add((ButtonWidget) button);
    }

    private void createTextButton(Text text, ButtonWidget.PressAction onPress) {
        this.addButton(
                new PressableTextWidget(
                        (int) (left + (bgWidth * 0.06f)),
                        (int) (top + (bgHeight * (0.1f * (choicesCount + 1)))),
                        this.textRenderer.getWidth(text),
                        10,
                        text,
                        onPress,
                        this.textRenderer
                )
        );

        choicesCount++;
    }

    @Override
    public void renderBackground(DrawContext context) {
        super.renderBackground(context);
    }

    protected void drawSonicScrewdriver(DrawContext context, int x, int y, float scale) {
        if(!getFromUUID(this.tardisId).getHandlers().getSonic().hasSonic(SonicHandler.HAS_CONSOLE_SONIC)) {
            return;
        }

        ItemStack sonic = tardis().getHandlers().getSonic().get(SonicHandler.HAS_CONSOLE_SONIC);
        NbtCompound nbt = sonic.getOrCreateNbt();

        if(!nbt.contains(SonicItem.SONIC_TYPE)) {
            return;
        }
        if (getFromUUID(tardisId) != null) {

            MatrixStack stack = context.getMatrices();

            ItemStack sonicCopy = sonic.copy();
            NbtCompound copiedNbt = sonicCopy.getOrCreateNbt();

            copiedNbt.putInt(SonicItem.SONIC_TYPE, this.selectedSonic);

            stack.push();

            if(!SonicItem.findSonicType(sonicCopy).equals(SonicItem.SonicTypes.MECHANICAL)) {
                stack.translate(x, y, 0f);
                stack.scale(scale, scale, scale);
            } else {
                float mechanicalScale = scale - 1.5f;
                stack.translate(x + 10f, y + 10f, 0f);
                stack.scale(mechanicalScale, mechanicalScale, mechanicalScale);
            }
            DiffuseLighting.disableGuiDepthLighting();
            context.drawItem(sonicCopy,0, 0);
            DiffuseLighting.enableGuiDepthLighting();

            stack.pop();

            stack.push();
            stack.translate(0, 0, 500f);
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    "Sonic Casing", x + 140, y + 10,
                    Color.WHITE.getRGB());
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    SonicItem.findSonicType(sonicCopy).asString(), x + 140, y + 20,
                    Color.CYAN.getRGB());
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    "Current AU", x + 140, y + 40,
                    Color.WHITE.getRGB());
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    nbt.getDouble(SonicItem.FUEL_KEY) + " AU", x + 140, y + 50,
                    Color.CYAN.getRGB());
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    "Linked TARDIS", x + 140, y + 70,
                    Color.WHITE.getRGB());
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    nbt.getString("tardis").substring(0, 8), x + 140, y + 80,
                    Color.CYAN.getRGB());
            stack.pop();
        }
    }

    public void getNextSelectedSonic() {
        int idx = SonicItem.SonicTypes.values()[this.selectedSonic].ordinal();
        this.selectedSonic = idx + 1 >= SonicItem.SonicTypes.values().length ? 0 : idx + 1;
    }

    public void getLastSelectedSonic() {
        int idx = SonicItem.SonicTypes.values()[this.selectedSonic].ordinal();
        System.out.println(idx - 1 < 0 ? SonicItem.SonicTypes.values().length - 1 : idx - 1);
        this.selectedSonic = idx - 1 < 0 ? SonicItem.SonicTypes.values().length - 1 : idx - 1;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.drawBackground(context);
        this.drawSonicScrewdriver(context, (width / 2 - 92), (height / 2 - 45), 6f);
        if(!this.buttons.get(0).isHovered()) context.drawTexture(BACKGROUND, left + 27, top + 106, 0, 156, 57, 12);
        if(!this.buttons.get(2).isHovered()) context.drawTexture(BACKGROUND, left + 8, top + 88, 0, 126, 15, 30);
        if(!this.buttons.get(3).isHovered()) context.drawTexture(BACKGROUND, left + 88, top + 88, 15, 126, 15, 30);
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawBackground(DrawContext context) {
        context.drawTexture(BACKGROUND, left, top, 0, 0, bgWidth, bgHeight);
        context.drawTexture(BACKGROUND, left + 9, top + 24, 0, 168, 93, 76);
    }
}