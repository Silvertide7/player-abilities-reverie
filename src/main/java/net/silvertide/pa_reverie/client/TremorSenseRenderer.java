package net.silvertide.pa_reverie.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.silvertide.pa_reverie.PAReverie;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = PAReverie.MOD_ID, value = Dist.CLIENT)
public final class TremorSenseRenderer {

    private static final float RIPPLE_SPEED_BLOCKS_PER_SECOND = 24f;
    private static final float PULSE_FADE_IN_SECONDS = 0.25f;
    private static final float PULSE_FADE_OUT_SECONDS = 0.75f;
    private static final float PULSE_DARK_SECONDS = 3.0f;
    private static final float PULSE_VISIBLE_END_SECONDS = PULSE_FADE_IN_SECONDS + PULSE_FADE_OUT_SECONDS;
    private static final float PULSE_PERIOD_SECONDS = PULSE_VISIBLE_END_SECONDS + PULSE_DARK_SECONDS;
    private static final float VISIBLE_ALPHA = 0.95f;
    private static final float ENDING_FADE_PORTION = 0.20f;
    private static final float OUTLINE_PADDING = 0.002f;

    private TremorSenseRenderer() {}

    @SubscribeEvent
    public static void onClientLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            TremorSenseRenderState.clear();
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        TremorSenseRenderState.ActiveHighlight active = TremorSenseRenderState.current();
        if (active == null || active.blocks().isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        long gameTick = minecraft.level.getGameTime();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float elapsedSeconds = ((gameTick - active.startGameTick()) + partialTick) / 20f;
        float totalSeconds = (active.endGameTick() - active.startGameTick()) / 20f;
        float endingFadeStartSeconds = totalSeconds * (1f - ENDING_FADE_PORTION);

        Vec3 cameraPos = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        Matrix4f matrix = poseStack.last().pose();

        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = buffers.getBuffer(ReverieHighlightRenderType.linesThroughWalls());

        float endingFade = 1f;
        if (elapsedSeconds > endingFadeStartSeconds) {
            endingFade = Math.max(0f, 1f - (elapsedSeconds - endingFadeStartSeconds) / (totalSeconds - endingFadeStartSeconds));
        }

        for (TremorSenseRenderState.HighlightedBlock block : active.blocks()) {
            float rippleDelay = block.distanceFromOrigin() / RIPPLE_SPEED_BLOCKS_PER_SECOND;
            float blockElapsedTime = elapsedSeconds - rippleDelay;
            if (blockElapsedTime < 0f) {
                continue;
            }
            float pulseTime = blockElapsedTime % PULSE_PERIOD_SECONDS;
            if (pulseTime >= PULSE_VISIBLE_END_SECONDS) {
                continue;
            }
            float pulseAlpha;
            if (pulseTime < PULSE_FADE_IN_SECONDS) {
                pulseAlpha = VISIBLE_ALPHA * (pulseTime / PULSE_FADE_IN_SECONDS);
            } else {
                float fadeOutElapsed = pulseTime - PULSE_FADE_IN_SECONDS;
                pulseAlpha = VISIBLE_ALPHA * (1f - fadeOutElapsed / PULSE_FADE_OUT_SECONDS);
            }
            float alpha = pulseAlpha * endingFade;
            if (alpha <= 0f) {
                continue;
            }
            drawCubeOutline(consumer, matrix, block, alpha);
        }

        buffers.endBatch(ReverieHighlightRenderType.linesThroughWalls());

        poseStack.popPose();
    }

    private static void drawCubeOutline(VertexConsumer consumer, Matrix4f matrix, TremorSenseRenderState.HighlightedBlock block, float alpha) {
        float x0 = block.pos().getX() - OUTLINE_PADDING;
        float y0 = block.pos().getY() - OUTLINE_PADDING;
        float z0 = block.pos().getZ() - OUTLINE_PADDING;
        float x1 = block.pos().getX() + 1f + OUTLINE_PADDING;
        float y1 = block.pos().getY() + 1f + OUTLINE_PADDING;
        float z1 = block.pos().getZ() + 1f + OUTLINE_PADDING;
        int color = block.color();
        int a = Math.round(alpha * 255f) & 0xFF;
        ReverieHighlightLines.drawBox(consumer, matrix, x0, y0, z0, x1, y1, z1,
                (color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, a);
    }
}
