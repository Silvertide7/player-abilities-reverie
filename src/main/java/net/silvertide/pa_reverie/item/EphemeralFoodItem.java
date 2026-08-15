package net.silvertide.pa_reverie.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EphemeralFoodItem extends Item {

    private static final int TICKS_PER_SECOND = 20;
    private static final int SECONDS_PER_MINUTE = 60;
    private static final String EXPIRES_AT_GAME_TIME_TAG = "ExpiresAtGameTime";

    private final UseAnim useAnimation;
    private final int defaultLifetimeTicks;

    public EphemeralFoodItem(Properties properties, UseAnim useAnimation, int defaultLifetimeTicks) {
        super(properties);
        this.useAnimation = useAnimation;
        this.defaultLifetimeTicks = defaultLifetimeTicks;
    }

    public int defaultLifetimeTicks() {
        return defaultLifetimeTicks;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return useAnimation;
    }

    public static void setExpiration(ItemStack stack, Level level, int lifetimeTicks) {
        stack.getOrCreateTag().putLong(EXPIRES_AT_GAME_TIME_TAG, level.getGameTime() + lifetimeTicks);
    }

    @Nullable
    private static Long expiresAtGameTime(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(EXPIRES_AT_GAME_TIME_TAG, Tag.TAG_LONG)
                ? tag.getLong(EXPIRES_AT_GAME_TIME_TAG)
                : null;
    }

    public static boolean isExpired(ItemStack stack, Level level) {
        Long expiresAt = expiresAtGameTime(stack);
        return expiresAt != null && level.getGameTime() >= expiresAt;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        Long expiresAt = expiresAtGameTime(stack);
        if (expiresAt == null || level == null) {
            return;
        }
        long remainingTicks = expiresAt - level.getGameTime();
        if (remainingTicks <= 0L) {
            tooltip.add(Component.translatable("tooltip.pa_reverie.food_fading")
                    .withStyle(ChatFormatting.RED));
            return;
        }
        long remainingSeconds = remainingTicks / TICKS_PER_SECOND;
        if (remainingSeconds >= SECONDS_PER_MINUTE) {
            tooltip.add(Component.translatable("tooltip.pa_reverie.fades_in_minutes",
                            Math.max(1L, remainingSeconds / SECONDS_PER_MINUTE))
                    .withStyle(ChatFormatting.GOLD));
        } else {
            tooltip.add(Component.translatable("tooltip.pa_reverie.fades_in_seconds",
                            Math.max(1L, remainingSeconds))
                    .withStyle(ChatFormatting.GOLD));
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (isExpired(stack, level)) {
            if (!level.isClientSide) {
                stack.shrink(1);
                if (entity instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(
                            Component.translatable("message.pa_reverie.food_faded")
                                    .withStyle(ChatFormatting.RED)));
                }
            }
            return stack;
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
