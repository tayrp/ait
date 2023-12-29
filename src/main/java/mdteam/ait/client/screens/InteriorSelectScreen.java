package mdteam.ait.client.screens;

import mdteam.ait.AITMod;
import mdteam.ait.registry.DesktopRegistry;
import mdteam.ait.tardis.TardisDesktopSchema;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableTextWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.UUID;

import static mdteam.ait.tardis.handler.InteriorChangingHandler.CHANGE_DESKTOP;

public class InteriorSelectScreen extends TardisScreen {
    private static final Identifier BACKGROUND = new Identifier(AITMod.MOD_ID, "textures/gui/tardis/interior.png");
    int bgHeight = 166;
    int bgWidth = 256;
    int left, top;

    private TardisDesktopSchema selectedDesktop;

    // i want to do what tr did but i dont want to copy them so im making it a boring arrows and text
    // ( by what tr did i mean the cool images )
    public InteriorSelectScreen(UUID tardis) {
        super(Text.translatable("screen." + AITMod.MOD_ID + ".interior"), tardis);
        updateTardis();
    }

    @Override
    protected void init() {
        this.selectedDesktop = findFirstSchema();
        this.top = (this.height - this.bgHeight) / 2; // this means everythings centered and scaling, same for below
        this.left = (this.width - this.bgWidth) / 2;
        this.createButtons();

        super.init();
    }

    private static TardisDesktopSchema findFirstSchema() {
        return DesktopRegistry.REGISTRY.get(0);
    }

    private void createButtons() {
        this.addButton(
                new PressableTextWidget(
                        (int) (left + (bgWidth * 0.1f)),
                        (int) (top + (bgHeight * 0.3f)),
                        this.textRenderer.getWidth("<"),
                        10,
                        Text.literal("<"),
                        button -> previousDesktop(),
                        this.textRenderer
                )
        );
        this.addButton(
                new PressableTextWidget(
                        (int) (left + (bgWidth * 0.38f)),
                        (int) (top + (bgHeight * 0.3f)),
                        this.textRenderer.getWidth(">"),
                        10,
                        Text.literal(">"),
                        button -> nextDesktop(),
                        this.textRenderer
                )
        );
        this.addButton(
                new PressableTextWidget(
                        (int) (left + (bgWidth * 0.25f)) - (textRenderer.getWidth("apply") / 2),
                        (int) (top + (bgHeight * 0.4f)),
                        this.textRenderer.getWidth("apply"),
                        10,
                        Text.literal("apply"),
                        button -> applyDesktop(),
                        this.textRenderer
                )
        );
    }

    private <T extends ClickableWidget> void addButton(T button) {
        this.addDrawableChild(button);
        button.active = true; // this whole method is unnecessary bc it defaults to true ( ?? )
    }

    private void applyDesktop() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(tardis().getUuid());
        buf.writeIdentifier(this.selectedDesktop.id());

        ClientPlayNetworking.send(CHANGE_DESKTOP, buf);

        MinecraftClient.getInstance().setScreen(null);
    }

    private static TardisDesktopSchema nextDesktop(TardisDesktopSchema current) {
        List<TardisDesktopSchema> list = DesktopRegistry.REGISTRY.stream().toList();

        int idx = list.indexOf(current);
        if (idx < 0 || idx + 1 == list.size()) return list.get(0);
        return list.get(idx + 1);
    }

    private void nextDesktop() {
        this.selectedDesktop = nextDesktop(this.selectedDesktop);
    }

    private static TardisDesktopSchema previousDesktop(TardisDesktopSchema current) {
        List<TardisDesktopSchema> list = DesktopRegistry.REGISTRY.stream().toList();

        int idx = list.indexOf(current);
        if (idx <= 0) return list.get(list.size() - 1);
        return list.get(idx - 1);
    }

    private void previousDesktop() {
        this.selectedDesktop = previousDesktop(this.selectedDesktop);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.drawBackground(context); // the grey backdrop
        this.renderDesktop(context);
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawBackground(DrawContext context) {
        context.drawTexture(BACKGROUND, left, top, 0, 0, bgWidth, bgHeight);
    }
    private void renderDesktop(DrawContext context) {
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            this.selectedDesktop.name(),
            (int) (left + (bgWidth * 0.25f)),
            (int) (top + (bgHeight * 0.3f)),
            0xadcaf7
        );

        context.drawTexture(this.selectedDesktop.previewTexture().texture(), left + 119, top + 20,128,128,0,0, this.selectedDesktop.previewTexture().width * 2, this.selectedDesktop.previewTexture().height * 2,this.selectedDesktop.previewTexture().width * 2, this.selectedDesktop.previewTexture().height * 2);
    }
}