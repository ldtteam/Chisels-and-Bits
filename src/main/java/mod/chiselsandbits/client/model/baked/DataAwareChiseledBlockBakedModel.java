package mod.chiselsandbits.client.model.baked;

import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.render.NullBakedModel;
import mod.chiselsandbits.render.chiseledblock.ChiselRenderType;
import mod.chiselsandbits.render.chiseledblock.ChiseledBlockBaked;
import mod.chiselsandbits.render.chiseledblock.ChiseledBlockSmartModel;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.ForgeRenderTypes;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Random;

public class DataAwareChiseledBlockBakedModel extends BaseSmartModel
{
    private final ModelProperty<ChiseledBlockBaked> MODEL_PROP = new ModelProperty<>();

    @Override
    public boolean func_230044_c_()
    {
        return false;
    }

    @Override
    public IBakedModel handleBlockState(final BlockState state, final Random random, final IModelData modelData)
    {
        if (!modelData.hasProperty(MODEL_PROP))
            return NullBakedModel.instance;

        return modelData.getData(MODEL_PROP);
    }

    @NotNull
    @Override
    public IModelData getModelData(
      @NotNull final IBlockDisplayReader world, @NotNull final BlockPos pos, @NotNull final BlockState state, @NotNull final IModelData tileData)
    {
        final ChiseledBlockBaked model = ChiseledBlockSmartModel.getCachedModel(
          (TileEntityBlockChiseled) Objects.requireNonNull(world.getTileEntity(pos)),
          ChiselRenderType.fromLayer(
            MinecraftForgeClient.getRenderLayer(),
            false
          )
        );

        return new ModelDataMap.Builder().withInitial(MODEL_PROP, model).build();
    }
}
