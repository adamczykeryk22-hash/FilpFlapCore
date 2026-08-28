package pl.filpflap.skyblock.generator;

import org.bukkit.Material;

import java.util.LinkedHashMap;
import java.util.Map;

public final class GeneratorLevel {

    private final int level;
    private final double upgradeCost;
    private final Map<Material, Double> blockChances;

    public GeneratorLevel(
            int level,
            double upgradeCost,
            Map<Material, Double> blockChances
    ) {
        this.level = level;
        this.upgradeCost = upgradeCost;
        this.blockChances = new LinkedHashMap<>(blockChances);
    }

    public int getLevel() {
        return level;
    }

    public double getUpgradeCost() {
        return upgradeCost;
    }

    public Map<Material, Double> getBlockChances() {
        return blockChances;
    }
}
