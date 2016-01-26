package mcjty.deepresonance.blocks.gencontroller;

import elec332.core.world.WorldHelper;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import mcjty.deepresonance.DeepResonance;
import mcjty.deepresonance.blocks.generator.GeneratorConfiguration;
import net.minecraft.block.Block;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class GeneratorShutdownSound extends MovingSound {
    private final EntityPlayer player;
    private final World world;
    private final BlockPos pos;

    public GeneratorShutdownSound(EntityPlayer player, World world, int x, int y, int z) {
        super(new ResourceLocation(DeepResonance.MODID + ":engine_shutdown"));
        this.player = player;
        this.world = world;
        this.pos = new BlockPos(x, y, z);

        this.xPosF = x;
        this.yPosF = y;
        this.zPosF = z;

        this.attenuationType = AttenuationType.LINEAR;
        this.repeat = true;
        this.repeatDelay = 0;
    }

    @Override
    public void update() {
        Block block = WorldHelper.getBlockAt(world, pos);
        if (block != GeneratorControllerSetup.generatorControllerBlock) {
            donePlaying = true;
            return;
        }
        volume = GeneratorConfiguration.baseGeneratorVolume;
    }
}