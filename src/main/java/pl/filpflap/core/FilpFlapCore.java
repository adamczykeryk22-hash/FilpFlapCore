package pl.filpflap.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.*;

public final class FilpFlapCore extends JavaPlugin implements Listener {

    private static final TextColor PURPLE = TextColor.color(190, 70, 255);
    private static final TextColor LIGHT_PURPLE = TextColor.color(225, 160, 255);
    private static final TextColor GREEN = TextColor.color(80, 255, 130);
    private static final TextColor GOLD = TextColor.color(255, 205, 70);
    private static final TextColor RED = TextColor.color(255, 80, 90);
    private static final TextColor BLUE = TextColor.color(90, 170, 255);
    private static final TextColor GRAY = TextColor.color(150, 150, 165);

    private static final Component MAIN_TITLE =
            Component.text("✦ FILPFLAP • BOX PVP ✦", PURPLE);

    private static final Component SHOP_TITLE =
            Component.text("✦ FILPFLAP • SKLEP ✦", PURPLE);

    private final Map<UUID, Double> money = new HashMap<>();
    private final Map<UUID, Integer> coins = new HashMap<>();
    private final Map<UUID, Integer> kills = new HashMap<>();
    private final Map<UUID, Integer> deaths = new HashMap<>();
    private final Map<UUID, Integer> streak = new HashMap<>();
    private final Map<UUID, Boolean> autoSell = new HashMap<>();
    private final Map<UUID, Long> daily = new HashMap<>();

    private final Map<Material, Double> prices = new LinkedHashMap<>();

    @Override
    public void onEnable() {
        setupPrices();
        loadData();

        getServer().getPluginManager().registerEvents(this, this);

        startScoreboardTask();
        startTabTask();

        getLogger().info("FilpFlapCore 6.1 uruchomiony!");
    }

    @Override
    public void onDisable() {
        saveData();
    }

    private void setupPrices() {
        prices.put(Material.COAL, 10.0);
        prices.put(Material.IRON_INGOT, 25.0);
        prices.put(Material.GOLD_INGOT, 45.0);
        prices.put(Material.REDSTONE, 30.0);
        prices.put(Material.LAPIS_LAZULI, 35.0);
        prices.put(Material.DIAMOND, 100.0);
        prices.put(Material.EMERALD, 150.0);
        prices.put(Material.COPPER_INGOT, 12.0);
    }

    // =====================================================
    // KOMENDY
    // =====================================================

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Komenda tylko dla gracza.");
            return true;
        }

        String name = command.getName().toLowerCase();

        if (name.equals("filpflap") || name.equals("ff")) {
            openMainMenu(player);
            return true;
        }

        if (name.equals("shop")) {
            openShop(player);
            return true;
        }

        if (name.equals("money") || name.equals("bal")) {
            player.sendMessage(
                    Component.text(
                            "◆ Twoje saldo: $" + format(getMoney(player)),
                            GREEN
                    )
            );
            return true;
        }

        if (name.equals("coins") || name.equals("monety")) {
            player.sendMessage(
                    Component.text(
                            "◆ Twoje monety: " + getCoins(player),
                            GOLD
                    )
            );
            return true;
        }
pwd
        if (name.equals("daily")) {
            claimDaily(player);
            return true;
        }

        if (name.equals("autosell")) {

            if (!player.hasPermission("filpflap.autosell")) {
                player.sendMessage(
                        Component.text(
                                "✖ Nie masz uprawnien do AutoSell.",
                                RED
                        )
                );
                return true;
            }

            UUID uuid = player.getUniqueId();

            boolean enabled =
                    !autoSell.getOrDefault(uuid, false);

            autoSell.put(uuid, enabled);

            if (enabled) {
                player.sendMessage(
                        Component.text(
                                "◆ AutoSell: WŁĄCZONY",
                                GREEN
                        )
                );
            } else {
                player.sendMessage(
                        Component.text(
                                "◆ AutoSell: WYŁĄCZONY",
                                RED
                        )
                );
            }

            return true;}

        if (name.equals("pay")) {

            if (args.length != 2) {
                player.sendMessage(
                        Component.text(
                                "Uzycie: /pay <gracz> <kwota>",
                                LIGHT_PURPLE
                        )
                );
                return true;
            }

            Player target =
                    Bukkit.getPlayerExact(args[0]);

            if (target == null) {
                player.sendMessage(
                        Component.text(
                                "✖ Gracz nie jest online.",
                                RED
                        )
                );
                return true;
            }

            double amount;

            try {
                amount = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(
                        Component.text(
                                "✖ Nieprawidlowa kwota.",
                                RED
                        )
                );
                return true;
            }

            if (amount <= 0 || getMoney(player) < amount) {
                player.sendMessage(
                        Component.text(
                                "✖ Nie masz wystarczajaco pieniedzy.",
                                RED
                        )
                );
                return true;
            }

            removeMoney(player, amount);
            addMoney(target, amount);

            player.sendMessage(
                    Component.text(
                            "✓ Wyslano $" + format(amount)
                                    + " do " + target.getName(),
                            GREEN
                    )
            );

            target.sendMessage(
                    Component.text(
                            "✓ Otrzymales $" + format(amount)
                                    + " od " + player.getName(),
                            GREEN
                    )
            );

            return true;
        }

        return false;
    }

    // =====================================================
    // KOPANIE / AUTOSELL
    // =====================================================

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();

        if (!autoSell.getOrDefault(
                player.getUniqueId(), false)) {
            return;
        }

        if (!player.hasPermission("filpflap.autosell")) {
            return;
        }

        Material block = event.getBlock().getType();

        double price = getPriceForBlock(block);

        if (price <= 0) {
            return;
        }

        Collection<ItemStack> drops =
                event.getBlock().getDrops(player.getInventory().getItemInMainHand());

        int amount = 0;

        for (ItemStack drop : drops) {
            if (prices.containsKey(drop.getType())) {
                amount += drop.getAmount();
            }
        }

        if (amount <= 0) {
            return;
        }

        double earned = price * amount;

        event.setDropItems(false);

        addMoney(player, earned);

        player.sendActionBar(
                Component.text(
                        "◆ AUTOSELL  +$" +
                                format(earned) +
                                "  •  Saldo: $" +
                                format(getMoney(player)),
                        GREEN
                )
        );
    }

    private double getPriceForBlock(Material material) {

        return switch (material) {
            case COAL_ORE, DEEPSLATE_COAL_ORE ->
                    prices.get(Material.COAL);

            case IRON_ORE, DEEPSLATE_IRON_ORE ->
                    prices.get(Material.IRON_INGOT);

            case GOLD_ORE, DEEPSLATE_GOLD_ORE ->
                    prices.get(Material.GOLD_INGOT);

            case REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE ->
                    prices.get(Material.REDSTONE);

            case LAPIS_ORE, DEEPSLATE_LAPIS_ORE ->
                    prices.get(Material.LAPIS_LAZULI);

            case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE ->
                    prices.get(Material.DIAMOND);

            case EMERALD_ORE, DEEPSLATE_EMERALD_ORE ->
                    prices.get(Material.EMERALD);

            case COPPER_ORE, DEEPSLATE_COPPER_ORE ->
                    prices.get(Material.COPPER_INGOT);

            default -> 0.0;
        };
    }

    // =====================================================
    // PVP
    // =====================================================

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {

        Player victim = event.getEntity();

        UUID victimId = victim.getUniqueId();

        deaths.put(
                victimId,
                getDeaths(victim) + 1
        );

        streak.put(victimId, 0);

        Player killer = victim.getKiller();

        if (killer != null) {

            kills.put(
                    killer.getUniqueId(),
                    getKills(killer) + 1
            );

            streak.put(
                    killer.getUniqueId(),
                    getStreak(killer) + 1
            );

            addCoins(killer, 5);

            killer.sendMessage(
                    Component.text(
                            "⚔ +1 Kill  •  🔥 Streak: "
                                    + getStreak(killer)
                                    + "  •  +5 monet",
                            PURPLE
                    )
            );
        }
    }

    // =====================================================
    // DAILY
    // =====================================================

    private void claimDaily(Player player) {

        UUID uuid = player.getUniqueId();

        long now = System.currentTimeMillis();

        long last =
                daily.getOrDefault(uuid, 0L);

        long cooldown =
                24L * 60L * 60L * 1000L;

        if (now - last < cooldown) {

            long remaining =
                    cooldown - (now - last);

            long hours =
                    remaining / 3600000L;

            player.sendMessage(
                    Component.text(
                            "⏳ Daily bedzie dostepne za "
                                    + hours + "h.",
                            RED
                    )
            );

            return;
        }

        daily.put(uuid, now);

        addCoins(player, 50);

        player.sendMessage(
                Component.text(
                        "✦ DAILY  +50 MONET",
                        GOLD
                )
        );
    }

    // =====================================================
    // SKLEP
    // =====================================================

    private void openShop(Player player) {

        Inventory shop =
                Bukkit.createInventory(
                        null,
                        45,
                        SHOP_TITLE
                );

        ItemStack background =
                item(
                        Material.BLACK_STAINED_GLASS_PANE,
                        " "
                );

        for (int i = 0; i < 45; i++) {
            shop.setItem(i, background);
        }

        int slot = 10;

        for (Map.Entry<Material, Double> entry :
                prices.entrySet()) {

            shop.setItem(
                    slot,
                    shopItem(
                            entry.getKey(),
                            entry.getValue()
                    )
            );

            slot++;

            if (slot == 17) {
                slot = 19;
            }
        }

        shop.setItem(
                40,
                item(
                        Material.BARRIER,
                        "✖ WRÓĆ",
                        "",
                        "Wróć do głównego menu."
                )
        );

        player.openInventory(shop);
    }

    private ItemStack shopItem(
            Material material,
            double price
    ) {

        return item(
                material,
                "◆ " + prettyMaterial(material),
                "",
                "Cena: $" + format(price),
                "",
                "» Kliknij, aby sprzedać wszystko"
        );
    }

    private String prettyMaterial(Material material) {

        return switch (material) {
            case COAL -> "WĘGIEL";
            case IRON_INGOT -> "ŻELAZO";
            case GOLD_INGOT -> "ZŁOTO";
            case REDSTONE -> "REDSTONE";
            case LAPIS_LAZULI -> "LAPIS";
            case DIAMOND -> "DIAMENT";
            case EMERALD -> "SZMARAGD";
            case COPPER_INGOT -> "MIEDŹ";
            default -> material.name();
        };
    }

    private void sellAll(
            Player player,
            Material material,
            double price
    ) {

        int amount = 0;

        for (ItemStack stack :
                player.getInventory().getContents()) {

            if (stack != null &&
                    stack.getType() == material) {

                amount += stack.getAmount();
            }
        }

        if (amount <= 0) {

            player.sendMessage(
                    Component.text(
                            "✖ Nie masz tego przedmiotu.",
                            RED
                    )
            );

            return;
        }

        for (int i = 0;
             i < player.getInventory().getSize();
             i++) {

            ItemStack stack =
                    player.getInventory().getItem(i);

            if (stack != null &&
                    stack.getType() == material) {

                player.getInventory().setItem(i, null);
            }
        }

        double earned = amount * price;

        addMoney(player, earned);

        player.sendActionBar(
                Component.text(
                        "✦ SPRZEDANO  +" +
                                "$" + format(earned) +
                                "  •  Saldo: $" +
                                format(getMoney(player)),
                        GREEN
                )
        );
    }

    // =====================================================
    // GUI
    // =====================================================

    private void openMainMenu(Player player) {

        Inventory menu =
                Bukkit.createInventory(
                        null,
                        45,
                        MAIN_TITLE
                );

        ItemStack background =
                item(
                        Material.BLACK_STAINED_GLASS_PANE,
                        " "
                );

        for (int i = 0; i < 45; i++) {
            menu.setItem(i, background);
        }

        menu.setItem(
                4,
                item(
                        Material.NETHER_STAR,
                        "✦ FILPFLAP BOX PVP ✦",
                        "",
                        "Witaj, " + player.getName() + "!",
                        "",
                        "💰 Saldo: $" +
                                format(getMoney(player)),
                        "🪙 Monety: " +
                                getCoins(player),
                        "",
                        "✦ Wybierz opcję."
                )
        );

        menu.setItem(
                20,
                item(
                        Material.DIAMOND_PICKAXE,
                        "⛏ KOPALNIA",
                        "",
                        "§a● AKTYWNA",
                        "",
                        "Kop rudy i zdobądź drop.",
                        "Sprzedaj go w sklepie.",
                        "",
                        "» Kliknij"
                )
        );

        menu.setItem(
                21,
                item(
                        Material.NETHERITE_SWORD,
                        "⚔ PVP",
                        "",
                        "§c● WALCZ",
                        "",
                        "Zdobywaj kille.",
                        "Buduj KillStreak.",
                        "",
                        "» Wkrótce"
                )
        );

        menu.setItem(
                22,
                item(
                        Material.CHEST,
                        "🎁 SKRZYNKI",
                        "",
                        "Specjalne nagrody.",
                        "",
                        "» Wkrótce"
                )
        );

        menu.setItem(
                23,
                item(
                        Material.WOLF_SPAWN_EGG,
                        "🐾 PETY",
                        "",
                        "Towarzysze i bonusy.",
                        "",
                        "» Wkrótce"
                )
        );

        menu.setItem(
                24,
                item(
                        Material.EMERALD,
                        "◆ SKLEP",
                        "",
                        "Sprzedawaj zdobyte przedmioty.",
                        "",
                        "» Kliknij"
                )
        );

        menu.setItem(
                29,
                item(
                        Material.GOLD_INGOT,
                        "🏆 RANKING",
                        "",
                        "Najlepsi gracze.",
                        "",
                        "» Wkrótce"
                )
        );

        menu.setItem(
                30,
                item(
                        Material.ENCHANTED_BOOK,
                        "✦ QUESTY",
                        "",
                        "Wykonuj zadania.",
                        "Zdobywaj monety.",
                        "",
                        "» Wkrótce"
                )
        );

        menu.setItem(
                31,
                item(
                        Material.SUNFLOWER,
                        "☀ DAILY",
                        "",
                        "Codzienna nagroda.",
                        "",
                        "§6+50 monet",
                        "",
                        "» Kliknij"
                )
        );

        menu.setItem(
                32,
                item(
                        Material.PLAYER_HEAD,
                        "👤 PROFIL",
                        "",
                        "💰 $" + format(getMoney(player)),
                        "🪙 " + getCoins(player),
                        "⚔ Kille: " + getKills(player),
                        "☠ Zgony: " + getDeaths(player),
                        "🔥 Streak: " + getStreak(player)
                )
        );

        menu.setItem(
                40,
                item(
                        Material.BARRIER,
                        "✖ ZAMKNIJ",
                        "",
                        "Zamknij menu."
                )
        );

        player.openInventory(menu);
    }

    // =====================================================
    // KLIKANIE
    // =====================================================

    @EventHandler
    public void onInventoryClick(
            InventoryClickEvent event
    ) {

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Component title = event.getView().title();

        if (title.equals(MAIN_TITLE)) {

            event.setCancelled(true);

            int slot = event.getRawSlot();

            if (slot == 40) {
                player.closeInventory();
                return;
            }

            if (slot == 24) {
                openShop(player);
                return;
            }

            if (slot == 31) {
                claimDaily(player);
                return;
            }

            return;
        }

        if (title.equals(SHOP_TITLE)) {

            event.setCancelled(true);

            int slot = event.getRawSlot();

            if (slot == 40) {
                openMainMenu(player);
                return;
            }

            Material material = null;

            int index = slot - 10;

            if (slot >= 10 && slot <= 16) {
                material =
                        new ArrayList<>(prices.keySet())
                                .get(index);
            }

            if (slot >= 19 && slot <= 20) {
                int secondIndex =
                        slot - 19 + 7;

                if (secondIndex < prices.size()) {
                    material =
                            new ArrayList<>(prices.keySet())
                                    .get(secondIndex);
                }
            }

            if (material != null) {
                sellAll(
                        player,
                        material,
                        prices.get(material)
                );
            }
        }
    }

    // =====================================================
    // SCOREBOARD
    // =====================================================

    private void startScoreboardTask() {

        Bukkit.getScheduler().runTaskTimer(
                this,
                () -> {

                    for (Player player :
                            Bukkit.getOnlinePlayers()) {

                        updateScoreboard(player);
                    }

                },
                20L,
                20L
        );
    }

    private void updateScoreboard(Player player) {

        ScoreboardManager manager =
                Bukkit.getScoreboardManager();

        if (manager == null) {
            return;
        }

        Scoreboard board =
                manager.getNewScoreboard();

        Objective objective =
                board.registerNewObjective(
                        "filpflap",
                        "dummy",
                        Component.text(
                                "✦ FILPFLAP ✦",
                                PURPLE
                        )
                );

        objective.setDisplaySlot(
                DisplaySlot.SIDEBAR
        );

        score(objective, "§d✦ §fBOX PVP", 10);
        score(objective, "§8──────────", 9);
        score(objective, "§a💰 §f$" +
                format(getMoney(player)), 8);
        score(objective, "§6🪙 §f" +
                getCoins(player), 7);
        score(objective, "§c⚔ §fKille: " +
                getKills(player), 6);
        score(objective, "§4☠ §fZgony: " +
                getDeaths(player), 5);
        score(objective, "§e🔥 §fStreak: " +
                getStreak(player), 4);
        score(objective, "§8────────── ", 3);
        score(objective, "§dplay.filpflap.pl", 2);
        score(objective, "§8FilpFlap", 1);

        player.setScoreboard(board);
    }

    private void score(
            Objective objective,
            String text,
            int value
    ) {
        objective.getScore(text).setScore(value);
    }

    // =====================================================
    // TAB
    // =====================================================

    private void startTabTask() {

        Bukkit.getScheduler().runTaskTimer(
                this,
                () -> {

                    for (Player player :
                            Bukkit.getOnlinePlayers()) {

                        updateTab(player);
                    }

                },
                20L,
                20L
        );
    }

    private void updateTab(Player player) {

        String rank =
                player.hasPermission("filpflap.rank.owner")
                        ? "§4OWNER"
                        : player.hasPermission("filpflap.rank.admin")
                        ? "§cADMIN"
                        : player.hasPermission("filpflap.rank.mod")
                        ? "§2MOD"
                        : player.hasPermission("filpflap.rank.vip")
                        ? "§6VIP"
                        : "§7GRACZ";

        player.setPlayerListHeaderFooter(
                "§d§l✦ FILPFLAP NETWORK ✦\n"
                        + "§5BOX PVP",
                "\n"
                        + rank + " §8• §f"
                        + player.getName()
                        + "\n§7Online: §f"
                        + Bukkit.getOnlinePlayers().size()
        );

        Scoreboard board =
                player.getScoreboard();

        Team team =
                board.getTeam("filpflap");

        if (team == null) {
            team = board.registerNewTeam("filpflap");
        }

        team.setPrefix(rank + " §f");
        team.addEntry(player.getName());
    }

    // =====================================================
    // ITEMY
    // =====================================================

    private ItemStack item(
            Material material,
            String name,
            String... lore
    ) {

        ItemStack stack =
                new ItemStack(material);

        ItemMeta meta =
                stack.getItemMeta();

        TextColor color =
                LIGHT_PURPLE;

        if (name.contains("KOPALNIA")) {
            color = GREEN;
        } else if (name.contains("PVP")) {
            color = RED;
        } else if (name.contains("SKRZYNKI")) {
            color = GOLD;
        } else if (name.contains("SKLEP")) {
            color = GREEN;
        } else if (name.contains("RANKING")) {
            color = GOLD;
        } else if (name.contains("QUESTY")) {
            color = BLUE;
        } else if (name.contains("DAILY")) {
            color = GOLD;
        }

        meta.displayName(
                Component.text(
                        strip(name),
                        color
                )
        );

        List<Component> lines =
                new ArrayList<>();

        for (String line : lore) {

            if (line.isEmpty()) {
                lines.add(Component.text(""));
                continue;
            }

            TextColor lineColor =
                    GRAY;

            if (line.contains("§a")) {
                lineColor = GREEN;
            } else if (line.contains("§c")) {
                lineColor = RED;
            } else if (line.contains("§6")) {
                lineColor = GOLD;
            } else if (line.startsWith("»")) {
                lineColor = LIGHT_PURPLE;
            } else if (line.startsWith("💰")) {
                lineColor = GREEN;
            } else if (line.startsWith("🪙")) {
                lineColor = GOLD;
            }

            lines.add(
                    Component.text(
                            strip(line),
                            lineColor
                    )
            );
        }

        meta.lore(lines);

        stack.setItemMeta(meta);

        return stack;
    }

    private String strip(String text) {

        return ChatColor.stripColor(
                text.replace("§a", "")
                        .replace("§c", "")
                        .replace("§6", "")
        );
    }

    // =====================================================
    // DANE
    // =====================================================

    private double getMoney(Player player) {
        return money.getOrDefault(
                player.getUniqueId(),
                0.0
        );
    }

    private int getCoins(Player player) {
        return coins.getOrDefault(
                player.getUniqueId(),
                0
        );
    }

    private int getKills(Player player) {
        return kills.getOrDefault(
                player.getUniqueId(),
                0
        );
    }

    private int getDeaths(Player player) {
        return deaths.getOrDefault(
                player.getUniqueId(),
                0
        );
    }

    private int getStreak(Player player) {
        return streak.getOrDefault(
                player.getUniqueId(),
                0
        );
    }

    private void addMoney(
            Player player,
            double amount
    ) {
        UUID uuid = player.getUniqueId();

        money.put(
                uuid,
                getMoney(player) + amount
        );
    }

    private void removeMoney(
            Player player,
            double amount
    ) {
        UUID uuid = player.getUniqueId();

        money.put(
                uuid,
                Math.max(
                        0,
                        getMoney(player) - amount
                )
        );
    }

    private void addCoins(
            Player player,
            int amount
    ) {
        UUID uuid = player.getUniqueId();

        coins.put(
                uuid,
                getCoins(player) + amount
        );
    }

    private String format(double amount) {

        if (amount == (long) amount) {
            return String.valueOf((long) amount);
        }

        return String.format(
                Locale.US,
                "%.2f",
                amount
        );
    }

    private void loadData() {

        if (!getConfig().isConfigurationSection("players")) {
            return;
        }

        var section =
                getConfig()
                        .getConfigurationSection(
                                "players"
                        );

        if (section == null) {
            return;
        }

        for (String key :
                section.getKeys(false)) {

            try {

                UUID uuid =
                        UUID.fromString(key);

                String path =
                        "players." + key;

                money.put(
                        uuid,
                        getConfig().getDouble(
                                path + ".money"
                        )
                );

                coins.put(
                        uuid,
                        getConfig().getInt(
                                path + ".coins"
                        )
                );

                kills.put(
                        uuid,
                        getConfig().getInt(
                                path + ".kills"
                        )
                );

                deaths.put(
                        uuid,
                        getConfig().getInt(
                                path + ".deaths"
                        )
                );

                streak.put(
                        uuid,
                        getConfig().getInt(
                                path + ".streak"
                        )
                );

                autoSell.put(
                        uuid,
                        getConfig().getBoolean(
                                path + ".autosell"
                        )
                );

                daily.put(
                        uuid,
                        getConfig().getLong(
                                path + ".daily"
                        )
                );

            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private void saveData() {

        Set<UUID> players =
                new HashSet<>();

        players.addAll(money.keySet());
        players.addAll(coins.keySet());
        players.addAll(kills.keySet());
        players.addAll(deaths.keySet());

        for (UUID uuid : players) {

            String path =
                    "players." + uuid;

            getConfig().set(
                    path + ".money",
                    money.getOrDefault(uuid, 0.0)
            );

            getConfig().set(
                    path + ".coins",
                    coins.getOrDefault(uuid, 0)
            );

            getConfig().set(
                    path + ".kills",
                    kills.getOrDefault(uuid, 0)
            );

            getConfig().set(
                    path + ".deaths",
                    deaths.getOrDefault(uuid, 0)
            );

            getConfig().set(
                    path + ".streak",
                    streak.getOrDefault(uuid, 0)
            );

            getConfig().set(
                    path + ".autosell",
                    autoSell.getOrDefault(uuid, false)
            );

            getConfig().set(
                    path + ".daily",
                    daily.getOrDefault(uuid, 0L)
            );
        }

        saveConfig();
    }
}
