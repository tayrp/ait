package loqor.ait.tardis.door;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import loqor.ait.AITMod;
import loqor.ait.core.data.schema.door.DoorSchema;

public class ClassicDoorVariant extends DoorSchema {
    public static final Identifier REFERENCE = new Identifier(AITMod.MOD_ID, "door/classic");

    public ClassicDoorVariant() {
        super(REFERENCE);
    }

    @Override
    public boolean isDouble() {
        return true;
    }

    @Override
    public Vec3d adjustPortalPos(Vec3d pos, Direction direction) {
        return switch (direction) {
            case DOWN, UP -> pos;
            case NORTH -> pos.add(0, 0.075, -0.47);
            case SOUTH -> pos.add(0, 0.075, 0.47);
            case WEST -> pos.add(-0.47, 0.075, 0);
            case EAST -> pos.add(0.47, 0.075, 0);
        };
    }
}
