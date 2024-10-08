package loqor.ait.core.commands;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.server.command.ServerCommandSource;

import loqor.ait.AITMod;
import loqor.ait.core.commands.argument.TardisArgumentType;
import loqor.ait.tardis.base.TardisComponent;
import loqor.ait.tardis.data.mood.MoodHandler;
import loqor.ait.tardis.wrapper.server.ServerTardis;

public class TriggerMoodRollCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal(AITMod.MOD_ID).then(literal("trigger-mood-roll")
                .requires(source -> source.hasPermissionLevel(2)).then(argument("tardis", TardisArgumentType.tardis())
                        .executes(TriggerMoodRollCommand::triggerMoodRollCommand))));
    }

    private static int triggerMoodRollCommand(CommandContext<ServerCommandSource> context) {

        ServerTardis tardis = TardisArgumentType.getTardis(context, "tardis");

        tardis.getHandlers().<MoodHandler>get(TardisComponent.Id.MOOD).rollForMoodDictatedEvent();

        return Command.SINGLE_SUCCESS;
    }
}
