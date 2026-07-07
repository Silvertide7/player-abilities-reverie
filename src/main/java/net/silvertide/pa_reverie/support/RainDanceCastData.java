package net.silvertide.pa_reverie.support;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

public class RainDanceCastData {

    private int elapsedTicks;
    private final List<BlockPos> dryFarmland = new ArrayList<>();
    private final List<BlockPos> currentRoundCrops = new ArrayList<>();
    private final List<BlockPos> nextRoundCrops = new ArrayList<>();
    private final List<BlockPos> rainTargets = new ArrayList<>();

    public int incrementElapsedTicks() {
        return ++elapsedTicks;
    }

    public void addDryFarmland(BlockPos pos) {
        dryFarmland.add(pos);
    }

    public void addGrowableCrop(BlockPos pos) {
        currentRoundCrops.add(pos);
    }

    public void keepForNextRound(BlockPos pos) {
        nextRoundCrops.add(pos);
    }

    public void addRainTarget(BlockPos pos) {
        rainTargets.add(pos);
    }

    public List<BlockPos> getRainTargets() {
        return rainTargets;
    }

    public boolean foundAnything() {
        return hasWork();
    }

    public boolean hasWork() {
        return !dryFarmland.isEmpty() || !currentRoundCrops.isEmpty() || !nextRoundCrops.isEmpty();
    }

    public BlockPos pollDryFarmland() {
        return dryFarmland.isEmpty() ? null : dryFarmland.remove(dryFarmland.size() - 1);
    }

    public BlockPos takeNextCropTurn(RandomSource random) {
        if (currentRoundCrops.isEmpty()) {
            if (nextRoundCrops.isEmpty()) {
                return null;
            }
            currentRoundCrops.addAll(nextRoundCrops);
            nextRoundCrops.clear();
        }
        int index = random.nextInt(currentRoundCrops.size());
        int lastIndex = currentRoundCrops.size() - 1;
        BlockPos chosen = currentRoundCrops.get(index);
        currentRoundCrops.set(index, currentRoundCrops.get(lastIndex));
        currentRoundCrops.remove(lastIndex);
        return chosen;
    }

}
