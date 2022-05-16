package org.dimdev.jeid.mixin.modsupport.scapeandrunparasites;

import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.dimdev.jeid.INewChunk;
import org.dimdev.jeid.network.BiomeChangeMessage;
import org.dimdev.jeid.network.MessageManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

@Pseudo
@Mixin(ParasiteEventWorld.class)
public class MixinSpreadBiome {
    @Shadow public abstract Biome getBiome();

    //Overwriting the vanilla SRP SpreadBiome class, which is called by blocks.
    //
    //Keeping this a close as we possibly can with SRP's SpreadBiome class to ensure that things work properly,
    //this is Dhantry's code, I do not know how his code works, so I will leave the rest to him.
    //All I've done is replaced SRPMain.network.sendToDimension() & SRPPacketBiomeChange() with JEID's packet handlers.
    @Overwrite(remap = false)
    public static void SpreadBiome(World worldIn, BlockPos pos, int age) {

        if(!SRPConfig.nodesActivated || !SRPConfig.biomeRegster) return;
        SRPWorldData data = SRPWorldData.get(worldIn);
        int distance = data.getDistanceSpreadByAge(age, false);

        for(int x = pos.func_177958_n()-4; x <= pos.func_177958_n()+4; x++) {
            for(int z = pos.func_177952_p()-4; z <= pos.func_177952_p()+4; z++) {

                BlockPos convert = new BlockPos(x, pos.func_177956_o(), z);
                if(data.isInRangeOfHeart(convert, distance)!=-1) {

                    if(ParasiteEventEntity.checkName(worldIn.func_180494_b(convert).getRegistryName().toString(),
                            SRPConfig.biomeBlackList, SRPConfig.biomeBlackListInverted)) {

                        continue;
                    }

                    positionToParasiteBiome(worldIn, convert);
                    // We are having to replace this with JEID's packet handler. I'm not sure if this will break things.
                    //SRPMain.network.sendToDimension(new SRPPacketBiomeChange(convert, true), worldIn.field_73011_w.getDimension());
                    MessageManager.CHANNEL.sendToAllAround(
                            new BiomeChangeMessage(pos.getX(), pos.getZ(), Biome.getIdForBiome(this.getBiome())),
                            new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), 128.0D, pos.getZ(), 128.0D)
                    );

                }
            }
        }
    }
}
