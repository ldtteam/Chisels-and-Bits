package mod.chiselsandbits.utils;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

@OnlyIn(Dist.CLIENT)
public final class FluidCuboidHelper
{

    private FluidCuboidHelper()
    {
        throw new IllegalStateException("Tried to initialize: FluidCuboidHelper but this is a Utility class.");
    }


    /**
     * Renders a fluid block with offset from the matrices and from x1/y1/z1 to x2/y2/z2 using block model coordinates, so from 0-16
     */
    public static void renderScaledFluidCuboid(FluidStack fluid, MatrixStack matrices, IVertexBuilder renderer, int combinedLight, float x1, float y1, float z1, float x2, float y2, float z2) {
        renderFluidCuboid(fluid, matrices, renderer, combinedLight, x1 / 16, y1 / 16, z1 / 16, x2 / 16, y2 / 16, z2 / 16);
    }

    /**
     * Renders a fluid block with offset from the matrices and from x1/y1/z1 to x2/y2/z2 inside the block local coordinates, so from 0-1
     */
    public static void renderFluidCuboid(FluidStack fluid, MatrixStack matrices, IVertexBuilder renderer, int combinedLight, float x1, float y1, float z1, float x2, float y2, float z2) {
        int color = fluid.getFluid().getAttributes().getColor(fluid);
        renderFluidCuboid(fluid, matrices, renderer, combinedLight, x1, y1, z1, x2, y2, z2, color);
    }

    /**
     * Renders a fluid block with offset from the matrices and from x1/y1/z1 to x2/y2/z2 inside the block local coordinates, so from 0-1
     */
    public static void renderFluidCuboid(FluidStack fluid, MatrixStack matrices, IVertexBuilder renderer, int combinedLight, float x1, float y1, float z1, float x2, float y2, float z2, int color)
    {
        TextureAtlasSprite still = Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(fluid.getFluid().getAttributes().getStillTexture(fluid));
        TextureAtlasSprite flowing = Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(fluid.getFluid().getAttributes().getFlowingTexture(fluid));

        renderFluidCuboid(still, flowing, color, matrices, renderer, combinedLight, x1, y1, z1, x2, y2, z2);
    }

    public static void renderFluidCuboid(TextureAtlasSprite still, TextureAtlasSprite flowing, int color, MatrixStack matrices, IVertexBuilder renderer, int combinedLight, float x1, float y1, float z1, float x2, float y2, float z2)
    {
        matrices.push();
        matrices.translate(x1, y1, z1);
        Matrix4f matrix = matrices.getLast().getMatrix();

        // x/y/z2 - x/y/z1 is because we need the width/height/depth
        putTexturedQuad(renderer, matrix, still, x2 - x1, y2 - y1, z2 - z1, Direction.DOWN, color, combinedLight, false);
        putTexturedQuad(renderer, matrix, flowing, x2 - x1, y2 - y1, z2 - z1, Direction.NORTH, color, combinedLight, true);
        putTexturedQuad(renderer, matrix, flowing, x2 - x1, y2 - y1, z2 - z1, Direction.EAST, color, combinedLight, true);
        putTexturedQuad(renderer, matrix, flowing, x2 - x1, y2 - y1, z2 - z1, Direction.SOUTH, color, combinedLight, true);
        putTexturedQuad(renderer, matrix, flowing, x2 - x1, y2 - y1, z2 - z1, Direction.WEST, color, combinedLight, true);
        putTexturedQuad(renderer, matrix, still, x2 - x1, y2 - y1, z2 - z1, Direction.UP, color, combinedLight, false);
        matrices.pop();
    }

    /**
     * Renders a full fluid cuboid for the given data
     * @param matrices  Matrix stack instance
     * @param buffer    Buffer type
     * @param still     Still sprite
     * @param flowing   Flowing sprite
     * @param cube      Fluid cuboid
     * @param from      Fluid start
     * @param to        Fluid end
     * @param color     Fluid color
     * @param light     Quad lighting
     * @param isGas     If true, fluid is a gas
     */
    public static void renderCuboid(MatrixStack matrices, IVertexBuilder buffer, FluidCuboid cube, TextureAtlasSprite still, TextureAtlasSprite flowing, Vector3f from, Vector3f to, int color, int light, boolean isGas) {
        Matrix4f matrix = matrices.getLast().getMatrix();
        int rotation = isGas ? 180 : 0;
        for (Direction dir : Direction.values()) {
            FluidCuboid.FluidFace face = cube.getFace(dir);
            if (face != null) {
                boolean isFlowing = face.isFlowing();
                int faceRot = (rotation + face.rotation()) % 360;
                putTexturedQuad(buffer, matrix, isFlowing ? flowing : still, from, to, dir, color, light, faceRot, isFlowing);
            }
        }
    }

    /**
     * Adds a quad to the renderer
     * @param renderer    Renderer instnace
     * @param matrix      Render matrix
     * @param sprite      Sprite to render
     * @param from        Quad start
     * @param to          Quad end
     * @param face        Face to render
     * @param color       Color to use in rendering
     * @param brightness  Face brightness
     * @param flowing     If true, half texture coordinates
     */
    public static void putTexturedQuad(IVertexBuilder renderer, Matrix4f matrix, TextureAtlasSprite sprite, Vector3f from, Vector3f to, Direction face, int color, int brightness, int rotation, boolean flowing) {
        // start with texture coordinates
        float x1 = from.getX(), y1 = from.getY(), z1 = from.getZ();
        float x2 = to.getX(), y2 = to.getY(), z2 = to.getZ();
        // choose UV based on opposite two axis
        float u1, u2, v1, v2;
        switch (face.getAxis()) {
            case Y:
            default:
                u1 = x1; u2 = x2;
                v1 = z2; v2 = z1;
                break;
            case Z:
                u1 = x2; u2 = x1;
                v1 = y1; v2 = y2;
                break;
            case X:
                u1 = z2; u2 = z1;
                v1 = y1; v2 = y2;
                break;
        }
        // flip V when relevant
        if (rotation == 0 || rotation == 270) {
            float temp = v1;
            v1 = 1f - v2;
            v2 = 1f - temp;
        }
        // flip U when relevant
        if (rotation >= 180) {
            float temp = u1;
            u1 = 1f - u2;
            u2 = 1f - temp;
        }
        // if rotating by 90 or 270, swap U and V
        float minU, maxU, minV, maxV;
        double size = flowing ? 8 : 16;
        if ((rotation % 180) == 90) {
            minU = sprite.getInterpolatedU(v1 * size);
            maxU = sprite.getInterpolatedU(v2 * size);
            minV = sprite.getInterpolatedV(u1 * size);
            maxV = sprite.getInterpolatedV(u2 * size);
        } else {
            minU = sprite.getInterpolatedU(u1 * size);
            maxU = sprite.getInterpolatedU(u2 * size);
            minV = sprite.getInterpolatedV(v1 * size);
            maxV = sprite.getInterpolatedV(v2 * size);
        }
        // based on rotation, put coords into place
        float u3, u4, v3, v4;
        switch(rotation) {
            case 0:
            default:
                u1 = minU; v1 = maxV;
                u2 = minU; v2 = minV;
                u3 = maxU; v3 = minV;
                u4 = maxU; v4 = maxV;
                break;
            case 90:
                u1 = minU; v1 = minV;
                u2 = maxU; v2 = minV;
                u3 = maxU; v3 = maxV;
                u4 = minU; v4 = maxV;
                break;
            case 180:
                u1 = maxU; v1 = minV;
                u2 = maxU; v2 = maxV;
                u3 = minU; v3 = maxV;
                u4 = minU; v4 = minV;
                break;
            case 270:
                u1 = maxU; v1 = maxV;
                u2 = minU; v2 = maxV;
                u3 = minU; v3 = minV;
                u4 = maxU; v4 = minV;
                break;
        }
        // add quads
        int light1 = brightness >> 0x10 & 0xFFFF;
        int light2 = brightness & 0xFFFF;
        int a = color >> 24 & 0xFF;
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        switch (face) {
            case DOWN:
                renderer.pos(matrix, x1, y1, z2).color(r, g, b, a).tex(u1, v1).lightmap(light1, light2).endVertex();
                renderer.pos(matrix, x1, y1, z1).color(r, g, b, a).tex(u2, v2).lightmap(light1, light2).endVertex();
                renderer.pos(matrix, x2, y1, z1).color(r, g, b, a).tex(u3, v3).lightmap(light1, light2).endVertex();
                renderer.pos(matrix, x2, y1, z2).color(r, g, b, a).tex(u4, v4).lightmap(light1, light2).endVertex();
                break;
            case UP:
                renderer.pos(matrix, x1, y2, z1).color(r, g, b, a).tex(u1, v1).lightmap(light1, light2).endVertex();
                renderer.pos(matrix, x1, y2, z2).color(r, g, b, a).tex(u2, v2).lightmap(light1, light2).endVertex();
                renderer.pos(matrix, x2, y2, z2).color(r, g, b, a).tex(u3, v3).lightmap(light1, light2).endVertex();
                renderer.pos(matrix, x2, y2, z1).color(r, g, b, a).tex(u4, v4).lightmap(light1, light2).endVertex();
                break;
            case NORTH:
                renderer.pos(matrix, x1, y1, z1).color(r, g, b, a).tex(u1, v1).lightmap(light1, light2).endVertex();
                renderer.pos(matrix, x1, y2, z1).color(r, g, b, a).tex(u2, v2).lightmap(light1, light2).endVertex();
                renderer.pos(matrix, x2, y2, z1).color(r, g, b, a).tex(u3, v3).lightmap(light1, light2).endVertex();
                renderer.pos(matrix, x2, y1, z1).color(r, g, b, a).tex(u4, v4).lightmap(light1, light2).endVertex();
                break;
            case SOUTH:
                renderer.pos(matrix, x2, y1, z2).color(r, g, b, a).tex(u1, v1).lightmap(light1, light2).endVertex();
                renderer.pos(matrix, x2, y2, z2).color(r, g, b, a).tex(u2, v2).lightmap(light1, light2).endVertex();
                renderer.pos(matrix, x1, y2, z2).color(r, g, b, a).tex(u3, v3).lightmap(light1, light2).endVertex();
                renderer.pos(matrix, x1, y1, z2).color(r, g, b, a).tex(u4, v4).lightmap(light1, light2).endVertex();
                break;
            case WEST:
                renderer.pos(matrix, x1, y1, z2).color(r, g, b, a).tex(u1, v1).lightmap(light1, light2).endVertex();
                renderer.pos(matrix, x1, y2, z2).color(r, g, b, a).tex(u2, v2).lightmap(light1, light2).endVertex();
                renderer.pos(matrix, x1, y2, z1).color(r, g, b, a).tex(u3, v3).lightmap(light1, light2).endVertex();
                renderer.pos(matrix, x1, y1, z1).color(r, g, b, a).tex(u4, v4).lightmap(light1, light2).endVertex();
                break;
            case EAST:
                renderer.pos(matrix, x2, y1, z1).color(r, g, b, a).tex(u1, v1).lightmap(light1, light2).endVertex();
                renderer.pos(matrix, x2, y2, z1).color(r, g, b, a).tex(u2, v2).lightmap(light1, light2).endVertex();
                renderer.pos(matrix, x2, y2, z2).color(r, g, b, a).tex(u3, v3).lightmap(light1, light2).endVertex();
                renderer.pos(matrix, x2, y1, z2).color(r, g, b, a).tex(u4, v4).lightmap(light1, light2).endVertex();
                break;
        }
    }

    public static void putTexturedQuad(IVertexBuilder renderer, Matrix4f matrix, TextureAtlasSprite sprite, float w, float h, float d, Direction face,
      int color, int brightness, boolean flowing) {
        putTexturedQuad(renderer, matrix, sprite, w, h, d, face, color, brightness, flowing, false, false);
    }

    public static void putTexturedQuad(IVertexBuilder renderer, Matrix4f matrix, TextureAtlasSprite sprite, float w, float h, float d, Direction face,
      int color, int brightness, boolean flowing, boolean flipHorizontally, boolean flipVertically) {
        int l1 = brightness >> 0x10 & 0xFFFF;
        int l2 = brightness & 0xFFFF;

        int a = color >> 24 & 0xFF;
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;

        putTexturedQuad(renderer, matrix, sprite, w, h, d, face, r, g, b, a, l1, l2, flowing, flipHorizontally, flipVertically);
    }

    /* Fluid cuboids */
// x and x+w has to be within [0,1], same for y/h and z/d
    public static void putTexturedQuad(IVertexBuilder renderer, Matrix4f matrix, TextureAtlasSprite sprite, float w, float h, float d, Direction face,
      int r, int g, int b, int a, int light1, int light2, boolean flowing, boolean flipHorizontally, boolean flipVertically) {
        // safety
        if (sprite == null) {
            return;
        }
        float minU;
        float maxU;
        float minV;
        float maxV;

        double size = 16f;
        if (flowing) {
            size = 8f;
        }

        double xt1 = 0;
        double xt2 = w;
        while (xt2 > 1f) xt2 -= 1f;
        double yt1 = 0;
        double yt2 = h;
        while (yt2 > 1f) yt2 -= 1f;
        double zt1 = 0;
        double zt2 = d;
        while (zt2 > 1f) zt2 -= 1f;

        // flowing stuff should start from the bottom, not from the start
        if (flowing) {
            double tmp = 1d - yt1;
            yt1 = 1d - yt2;
            yt2 = tmp;
        }

        switch (face) {
            case DOWN:
            case UP:
                minU = sprite.getInterpolatedU(xt1 * size);
                maxU = sprite.getInterpolatedU(xt2 * size);
                minV = sprite.getInterpolatedV(zt1 * size);
                maxV = sprite.getInterpolatedV(zt2 * size);
                break;
            case NORTH:
            case SOUTH:
                minU = sprite.getInterpolatedU(xt2 * size);
                maxU = sprite.getInterpolatedU(xt1 * size);
                minV = sprite.getInterpolatedV(yt1 * size);
                maxV = sprite.getInterpolatedV(yt2 * size);
                break;
            case WEST:
            case EAST:
                minU = sprite.getInterpolatedU(zt2 * size);
                maxU = sprite.getInterpolatedU(zt1 * size);
                minV = sprite.getInterpolatedV(yt1 * size);
                maxV = sprite.getInterpolatedV(yt2 * size);
                break;
            default:
                minU = sprite.getMinU();
                maxU = sprite.getMaxU();
                minV = sprite.getMinV();
                maxV = sprite.getMaxV();
        }

        if (flipHorizontally) {
            float tmp = minV;
            minV = maxV;
            maxV = tmp;
        }

        if (flipVertically) {
            float tmp = minU;
            minU = maxU;
            maxU = tmp;
        }

        switch (face) {
            case DOWN:
                renderer.pos(matrix, 0, 0, 0).color(r, g, b, a).tex(minU, minV).lightmap(light1, light2).normal(1, 0, 0).endVertex();
                renderer.pos(matrix, w, 0, 0).color(r, g, b, a).tex(maxU, minV).lightmap(light1, light2).normal(1, 0, 0).endVertex();
                renderer.pos(matrix, w, 0, d).color(r, g, b, a).tex(maxU, maxV).lightmap(light1, light2).normal(1, 0, 0).endVertex();
                renderer.pos(matrix, 0, 0, d).color(r, g, b, a).tex(minU, maxV).lightmap(light1, light2).normal(1, 0, 0).endVertex();
                break;
            case UP:
                renderer.pos(matrix, 0, h, 0).color(r, g, b, a).tex(minU, minV).lightmap(light1, light2).normal(1, 0, 0).endVertex();
                renderer.pos(matrix, 0, h, d).color(r, g, b, a).tex(minU, maxV).lightmap(light1, light2).normal(1, 0, 0).endVertex();
                renderer.pos(matrix, w, h, d).color(r, g, b, a).tex(maxU, maxV).lightmap(light1, light2).normal(1, 0, 0).endVertex();
                renderer.pos(matrix, w, h, 0).color(r, g, b, a).tex(maxU, minV).lightmap(light1, light2).normal(1, 0, 0).endVertex();
                break;
            case NORTH:
                renderer.pos(matrix, 0, 0, 0).color(r, g, b, a).tex(minU, maxV).lightmap(light1, light2).normal(1, 0, 0).endVertex();
                renderer.pos(matrix, 0, h, 0).color(r, g, b, a).tex(minU, minV).lightmap(light1, light2).normal(1, 0, 0).endVertex();
                renderer.pos(matrix, w, h, 0).color(r, g, b, a).tex(maxU, minV).lightmap(light1, light2).normal(1, 0, 0).endVertex();
                renderer.pos(matrix, w, 0, 0).color(r, g, b, a).tex(maxU, maxV).lightmap(light1, light2).normal(1, 0, 0).endVertex();
                break;
            case SOUTH:
                renderer.pos(matrix, 0, 0, d).color(r, g, b, a).tex(maxU, maxV).lightmap(light1, light2).normal(1, 0, 0).endVertex();
                renderer.pos(matrix, w, 0, d).color(r, g, b, a).tex(minU, maxV).lightmap(light1, light2).normal(1, 0, 0).endVertex();
                renderer.pos(matrix, w, h, d).color(r, g, b, a).tex(minU, minV).lightmap(light1, light2).normal(1, 0, 0).endVertex();
                renderer.pos(matrix, 0, h, d).color(r, g, b, a).tex(maxU, minV).lightmap(light1, light2).normal(1, 0, 0).endVertex();
                break;
            case WEST:
                renderer.pos(matrix, 0, 0, 0).color(r, g, b, a).tex(maxU, maxV).lightmap(light1, light2).normal(1, 0, 0).endVertex();
                renderer.pos(matrix, 0, 0, d).color(r, g, b, a).tex(minU, maxV).lightmap(light1, light2).normal(1, 0, 0).endVertex();
                renderer.pos(matrix, 0, h, d).color(r, g, b, a).tex(minU, minV).lightmap(light1, light2).normal(1, 0, 0).endVertex();
                renderer.pos(matrix, 0, h, 0).color(r, g, b, a).tex(maxU, minV).lightmap(light1, light2).normal(1, 0, 0).endVertex();
                break;
            case EAST:
                renderer.pos(matrix, w, 0, 0).color(r, g, b, a).tex(minU, maxV).lightmap(light1, light2).normal(1, 0, 0).endVertex();
                renderer.pos(matrix, w, h, 0).color(r, g, b, a).tex(minU, minV).lightmap(light1, light2).normal(1, 0, 0).endVertex();
                renderer.pos(matrix, w, h, d).color(r, g, b, a).tex(maxU, minV).lightmap(light1, light2).normal(1, 0, 0).endVertex();
                renderer.pos(matrix, w, 0, d).color(r, g, b, a).tex(maxU, maxV).lightmap(light1, light2).normal(1, 0, 0).endVertex();
                break;
        }
    }
}
