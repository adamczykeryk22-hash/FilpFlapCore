package pl.filpflap.skyblock.generator;

import org.bukkit.Material;
import org.bukkit.block.Block;
import pl.filpflap.skyblock.island.Island;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class GeneratorManager {

    private final Random random = new Random();

    public Material generateBlock(Island island) {
        GeneratorLevel level = getLevel(island.getGeneratorLevel());

        double roll = random.nextDouble() * 100.0;
        double current = 0.0;

        for (Map.Entry<Material, Double> entry :
                level.getBlockChances().entrySet()) {

            current += entry.getValue();

            if (roll <= current) {
                return entry.getKey();
            }
        }

        return Material.COBBLESTONE;
    }

    public GeneratorLevel getLevel(int level) {
        return switch (Math.max(1, Math.min(level, 6))) {

            case 1 -> level(
                    1,
                    0,
                    chance(
                            Material.COBBLESTONE, 70.0,
                            Material.STONE, 30.0
                    )
            );

            case 2 -> level(
                    2,
                    5000,
                    chance(
                            Material.COBBLESTONE, 50.0,
                            Material.STONE, 30.0,
                            Material.COAL_ORE, 15.0,
                            Material.IRON_ORE, 5.0
                    )
            );

            case 3 -> level(
                    3,
                    15000,
                    chance(
                            Material.STONE, 35.0,
                            Material.COAL_ORE, 25.0,
                            Material.IRON_ORE, 20.0,
                            Material.GOLD_ORE, 15.0,
                            Material.REDSTONE_ORE, 5.0
                    )
            );

            case 4 -> level(
                    4,
                    40000,
                    chance(
                            Material.IRON_ORE, 25.0,
                            Material.GOLD_ORE, 25.0,
                            Material.REDSTONE_ORE, 20.0,
                            Material.DIAMOND_ORE, 8.0,
                            Material.EMERALD_ORE, 2.0,
                            Material.STONE, 20.0
                    )
            );

            case 5 -> level(
                    5,
                    100000,
                    chance(
                            Material.GOLD_ORE, 25.0,
                            Material.DIAMOND_ORE, 15.0,
                            Material.EMERALD_ORE, 8.0,
                            Material.ANCIENT_DEBRIS, 2.0,
                            Material.IRON_ORE, 20.0,
                            Material.REDSTONE_ORE, 30.0
                    )
            );

            default -> level(
                    6,
                    250000,
                    chance(
                            Material.DIAMOND_ORE, 25.0,
                            Material.EMERALD_ORE, 15.0,
                            Material.ANCIENT_DEBRIS, 10.0,
                            Material.GOLD_ORE, 20.0,
                            Material.IRON_ORE, 15.0,
                            Material.REDSTONE_ORE, 15.0
                    )
            );
        };
    }

    private GeneratorLevel level(
            int level,
            double cost,
            Map<Material, Double> chances
    ) {
        return new GeneratorLevel(level, cost, chances);
    }

    private Map<Material, Double> chance(
            Object... values
    ) {
        Map<Material, Double> result = new LinkedHashMap<>();

        for (int i = 0; i < values.length; i += 2) {
            result.put(
                    (Material) values[i],
                    (Double) values[i + 1]
            );
        }

        return result;
    }
}
