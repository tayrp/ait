package loqor.ait.core.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import loqor.ait.tardis.data.landing.LandingPadManager;

public class LandingPadBlock extends Block {
    private static final BooleanProperty ACTIVE = BooleanProperty.of("active"); // whether this block created a region

    public LandingPadBlock(FabricBlockSettings settings) {
        super(settings);

        this.setDefaultState(
                this.getStateManager().getDefaultState().with(ACTIVE, false)
        );
    }


    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);

        builder.add(ACTIVE);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        super.randomDisplayTick(state, world, pos, random);

        Vec3d centre = pos.up().toCenterPos();
        world.addParticle(ParticleTypes.GLOW, centre.getX(), centre.getY() - 0.5, centre.getZ(), 0.0, 0.0, 0.0);

        if (random.nextDouble() < 0.2f)
            world.playSound(centre.getX(), centre.getY(), centre.getZ(), SoundEvents.BLOCK_BELL_RESONATE, SoundCategory.BLOCKS, 0.1f, 1f, true);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (!(world instanceof ServerWorld serverWorld))
            return;

        LandingPadManager manager = LandingPadManager.getInstance(serverWorld);

        if (manager.getRegionAt(pos) != null) {
            world.breakBlock(pos, true);
            return;
        }

        world.setBlockState(pos, state.with(ACTIVE, true));
        manager.claim(pos);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);

        if (!(world instanceof ServerWorld serverWorld))
            return;

        if (!state.get(ACTIVE)) return;

        LandingPadManager.getInstance(serverWorld).releaseAt(pos);
    }
}
