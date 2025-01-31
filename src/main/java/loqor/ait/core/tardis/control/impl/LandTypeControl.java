package loqor.ait.core.tardis.control.impl;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import loqor.ait.core.AITSounds;
import loqor.ait.core.blockentities.ConsoleBlockEntity;
import loqor.ait.core.tardis.Tardis;
import loqor.ait.core.tardis.control.Control;
import loqor.ait.core.tardis.handler.travel.TravelHandlerBase;
import loqor.ait.data.schema.console.variant.renaissance.*;

public class LandTypeControl extends Control {

    private SoundEvent soundEvent = AITSounds.LAND_TYPE;

    public LandTypeControl() {
        super("land_type");
    }

    @Override
    public boolean runServer(Tardis tardis, ServerPlayerEntity player, ServerWorld world, BlockPos console, boolean leftClick) {
        if (tardis.sequence().hasActiveSequence() && tardis.sequence().controlPartOfSequence(this)) {
            this.addToControlSequence(tardis, player, console);
            return false;
        }

        if (leftClick) {
            tardis.travel().horizontalSearch().flatMap(value -> {
                value = !value;
                messageXPlayer(player, value);
                return value;
            });

            return false;
        }

        tardis.travel().verticalSearch().flatMap(value -> {
            value = value.next();
            messageYPlayer(player, value);
            return value;
        });


        if (world.getBlockEntity(console) instanceof ConsoleBlockEntity consoleBlockEntity) {
            if (isRenaissanceVariant(consoleBlockEntity)) {
                this.soundEvent = AITSounds.RENAISSANCE_LAND_TYPE_ALT;
            }
        }

        return false;
    }

    @Override
    public SoundEvent getSound() {
        return this.soundEvent;
    }

    public void messageYPlayer(ServerPlayerEntity player, TravelHandlerBase.GroundSearch value) {
        player.sendMessage(Text.translatable("message.ait.control.ylandtype", value), true);
    }

    public void messageXPlayer(ServerPlayerEntity player, boolean var) {
        Text on = Text.translatable("message.ait.control.xlandtype.on");
        Text off = Text.translatable("message.ait.control.xlandtype.off");
        player.sendMessage(var ? on : off, true);
    }

    private boolean isRenaissanceVariant(ConsoleBlockEntity consoleBlockEntity) {
        return consoleBlockEntity.getVariant() instanceof RenaissanceTokamakVariant ||
                consoleBlockEntity.getVariant() instanceof RenaissanceVariant ||
                consoleBlockEntity.getVariant() instanceof RenaissanceIdentityVariant ||
                consoleBlockEntity.getVariant() instanceof RenaissanceFireVariant;
    }
}
