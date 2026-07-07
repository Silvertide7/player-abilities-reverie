package net.silvertide.pa_reverie.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.silvertide.pa_reverie.registry.ReverieDataComponents;

import java.util.List;

public class EphemeralFoodItem extends Item {

    private static final int TICKS_PER_SECOND = 20;
    private static final int SECONDS_PER_MINUTE = 60;

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
        stack.set(ReverieDataComponents.EXPIRES_AT_GAME_TIME.get(), level.getGameTime() + lifetimeTicks);
    }

    public static boolean isExpired(ItemStack stack, Level level) {
        Long expiresAt = stack.get(ReverieDataComponents.EXPIRES_AT_GAME_TIME.get());
        return expiresAt != null && level.getGameTime() >= expiresAt;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        Long expiresAt = stack.get(ReverieDataComponents.EXPIRES_AT_GAME_TIME.get());
        if (expiresAt == null) {
            return;
        }
        Level level = context.level();
        if (level == null) {
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
