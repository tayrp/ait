package loqor.ait.client.screens;

import java.awt.*;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableTextWidget;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import loqor.ait.AITMod;
import loqor.ait.client.util.ClientTardisUtil;
import loqor.ait.core.data.schema.SonicSchema;
import loqor.ait.core.item.SonicItem;
import loqor.ait.registry.impl.SonicRegistry;
import loqor.ait.tardis.wrapper.client.ClientTardis;

public class SonicSettingsScreen extends ConsoleScreen {
    private static final Identifier BACKGROUND = new Identifier(AITMod.MOD_ID,
            "textures/gui/tardis/consoles/monitors/sonic_selection.png");
    private final List<ButtonWidget> buttons = Lists.newArrayList();
    int bgHeight = 126;
    int bgWidth = 201;
    int left, top;
    int choicesCount = 0;
    private final Screen parent;
    private int selectedSonic;

    public SonicSettingsScreen(ClientTardis tardis, BlockPos console, Screen parent) {
        super(Text.translatable("screen.ait.sonicsettings.title"), tardis, console);
        this.parent = parent;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    protected void init() {
        NbtCompound nbt = tardis().sonic().getConsoleSonic().getOrCreateNbt();

        SonicSchema schema = SonicItem.findSchema(nbt);
        this.selectedSonic = SonicRegistry.getInstance().toList().indexOf(schema);
        this.top = (this.height - this.bgHeight) / 2; // this means everythings centered and scaling, same for below
        this.left = (this.width - this.bgWidth) / 2;
        this.createButtons();

        super.init();
    }

    private void createButtons() {
        choicesCount = 0;
        this.buttons.clear();

        Text applyText = Text.literal("Apply");
        this.addButton(new AITPressableTextWidget((int) (left + (bgWidth * 0.21f)), (int) (top + (bgHeight * 0.878f)),
                this.textRenderer.getWidth(applyText), 10, Text.literal("     "), button -> {
                    sendSonicChangePacket();
                }, this.textRenderer));

        Text back = Text.translatable("screen.ait.sonicsettings.back");
        this.addButton(new PressableTextWidget((width / 2 - 94), (height / 2 - 58), this.textRenderer.getWidth(back),
                10, back, button -> backToInteriorSettings(), this.textRenderer));

        this.addButton(new AITPressableTextWidget((int) (left + (bgWidth * 0.06f)), (int) (top + (bgHeight * 0.882f)),
                this.textRenderer.getWidth("<"), 10, Text.literal(" "), button -> this.getLastSelectedSonic(),
                this.textRenderer));
        this.addButton(new AITPressableTextWidget((int) (left + (bgWidth * 0.47f)), (int) (top + (bgHeight * 0.882f)),
                this.textRenderer.getWidth(">"), 10, Text.literal(" "), button -> this.getNextSelectedSonic(),
                this.textRenderer));
    }

    public void backToInteriorSettings() {
        MinecraftClient.getInstance().setScreen(this.parent);
    }

    public void sendSonicChangePacket() {
        if (this.tardis().sonic().getConsoleSonic() == null)
            return;

        SonicSchema schema = SonicRegistry.getInstance().toList().get(this.selectedSonic);

        if (!this.tardis().isUnlocked(schema))
            return;

        SonicItem.setSchema(tardis().sonic().getConsoleSonic(), schema);
        ClientTardisUtil.changeSonicWithScreen(this.tardis().getUuid(), schema);
    }

    private <T extends ClickableWidget> void addButton(T button) {
        this.addDrawableChild(button);
        button.active = true; // this whole method is unnecessary bc it defaults to true ( ?? ) - does it
        // though?
        this.buttons.add((ButtonWidget) button);
    }

    private void createTextButton(Text text, ButtonWidget.PressAction onPress) {
        this.addButton(new PressableTextWidget((int) (left + (bgWidth * 0.06f)),
                (int) (top + (bgHeight * (0.1f * (choicesCount + 1)))), this.textRenderer.getWidth(text), 10, text,
                onPress, this.textRenderer));

        choicesCount++;
    }

    @Override
    public void renderBackground(DrawContext context) {
        super.renderBackground(context);
    }

    protected void drawSonicScrewdriver(DrawContext context, int x, int y, float scale) {
        if (this.tardis() == null)
            return;

        if (this.tardis().sonic().getConsoleSonic() == null)
            return;

        ItemStack sonic = this.tardis().sonic().getConsoleSonic();
        NbtCompound nbt = sonic.getOrCreateNbt();

        if (this.tardis() != null) {
            MatrixStack stack = context.getMatrices();

            ItemStack sonicCopy = sonic.copy();
            SonicSchema schema = SonicRegistry.getInstance().toList().get(this.selectedSonic);

            SonicItem.setSchema(sonicCopy, schema);

            stack.push();
            stack.translate(50f, 50f, 1000f);
            context.drawCenteredTextWithShadow(this.textRenderer, (tardis().isUnlocked(schema)) ? "" : "\uD83D\uDD12",
                    x, y, Color.WHITE.getRGB());
            stack.pop();

            stack.push();

            SonicSchema.Rendering rendering = schema.rendering();
            SonicSchema.Rendering.Offset positionOffset = rendering.getPositionOffset();
            SonicSchema.Rendering.Offset scaleOffset = rendering.getScaleOffset();

            stack.translate(x + positionOffset.x(), y + positionOffset.y(), positionOffset.z());
            stack.scale(scale + scaleOffset.x(), scale + scaleOffset.y(), scale + scaleOffset.z());

            boolean isSonicUnlocked = tardis().isUnlocked(schema);

            float base = isSonicUnlocked ? 1f : 0.1f;

            RenderSystem.setShaderColor(base, base, base, 1f);
            DiffuseLighting.disableGuiDepthLighting();
            context.drawItem(sonicCopy, 0, 0);
            DiffuseLighting.enableGuiDepthLighting();
            RenderSystem.setShaderColor(1, 1, 1, 1);

            stack.pop();

            stack.push();
            stack.translate(0, 0, 500f);
            context.drawCenteredTextWithShadow(this.textRenderer, "Sonic Casing", x + 140, y + 10,
                    Color.WHITE.getRGB());
            context.drawCenteredTextWithShadow(this.textRenderer, SonicItem.findSchema(sonicCopy).name(), x + 140,
                    y + 20, Color.CYAN.getRGB());
            context.drawCenteredTextWithShadow(this.textRenderer, "Current AU", x + 140, y + 40, Color.WHITE.getRGB());
            context.drawCenteredTextWithShadow(this.textRenderer, nbt.getDouble(SonicItem.FUEL_KEY) + " AU", x + 140,
                    y + 50, Color.CYAN.getRGB());
            context.drawCenteredTextWithShadow(this.textRenderer, "Linked TARDIS", x + 140, y + 70,
                    Color.WHITE.getRGB());
            context.drawCenteredTextWithShadow(this.textRenderer, nbt.getString("tardis").substring(0, 8), x + 140,
                    y + 80, Color.CYAN.getRGB());

            stack.pop();
        }
    }

    public void getNextSelectedSonic() {
        this.selectedSonic = this.selectedSonic + 1 >= SonicRegistry.getInstance().size() ? 0 : this.selectedSonic + 1;
    }

    public void getLastSelectedSonic() {
        this.selectedSonic = this.selectedSonic - 1 < 0
                ? SonicRegistry.getInstance().size() - 1
                : this.selectedSonic - 1;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.drawBackground(context);
        this.drawSonicScrewdriver(context, (width / 2 - 92), (height / 2 - 45), 6f);
        if (!this.buttons.get(0).isHovered())
            context.drawTexture(BACKGROUND, left + 27, top + 106, 0, 156, 57, 12);
        if (!this.buttons.get(2).isHovered())
            context.drawTexture(BACKGROUND, left + 8, top + 88, 0, 126, 15, 30);
        if (!this.buttons.get(3).isHovered())
            context.drawTexture(BACKGROUND, left + 88, top + 88, 15, 126, 15, 30);
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawBackground(DrawContext context) {
        context.drawTexture(BACKGROUND, left, top, 0, 0, bgWidth, bgHeight);
        context.drawTexture(BACKGROUND, left + 9, top + 24, 0, 168, 93, 76);
    }

    public static class AITPressableTextWidget extends ButtonWidget {
        private final TextRenderer textRenderer;
        private final Text text;
        // private final Text hoverText;
        public AITPressableTextWidget(int x, int y, int width, int height, Text text, ButtonWidget.PressAction onPress,
                TextRenderer textRenderer) {
            super(x, y, width, height, text, onPress, DEFAULT_NARRATION_SUPPLIER);
            this.textRenderer = textRenderer;
            this.text = text;
            // this.hoverText = Texts.setStyleIfAbsent(text.copy(),
            // Style.EMPTY.withUnderline(true));
        }

        public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
            Text text = /* this.isSelected() ? this.hoverText : */ this.text;
            context.drawTextWithShadow(this.textRenderer, text, this.getX(), this.getY(),
                    16777215 | MathHelper.ceil(this.alpha * 255.0F) << 24);
        }
    }
}
