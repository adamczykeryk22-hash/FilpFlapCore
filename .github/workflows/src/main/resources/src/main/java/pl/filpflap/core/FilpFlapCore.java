package pl.filpflap.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class FilpFlapCore extends JavaPlugin implements Listener, TabExecutor {

    private static final TextColor PURPLE = TextColor.color(170, 0, 255);
    private static final Component TITLE =
            Component.text("✦ FILPFLAP ✦", PURPLE);

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        PluginCommand command = getCommand("filpflap");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        }

        getLogger().info("FilpFlapCore zostal wlaczony!");
    }

    private void openMenu(Player player) {
        Inventory inventory =
                Bukkit.createInventory(null, 27, TITLE);

        ItemStack filler =
                createItem(Material.PURPLE_STAINED_GLASS_PANE, " ");

        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, filler);
        }

        inventory.setItem(10, createItem(
                Material.DIAMOND_PICKAXE,
                "⛏ Mining",
                "Kopalnia i drop"
        ));

        inventory.setItem(11, createItem(
                Material.DIAMOND_SWORD,
                "⚔ PvP",
                "Kille i streaki"
        ));

        inventory.setItem(12, createItem(
                Material.CHEST,
                "🎁 Skrzynki",
                "Case opening"
        ));

        inventory.setItem(14, createItem(
                Material.ALLAY_SPAWN_EGG,
                "🐉 Pety",
                "Bonusy dla gracza"
        ));

        inventory.setItem(15, createItem(
                Material.NETHER_STAR,
                "🏆 Ranking",
                "TOP graczy"
        ));

        inventory.setItem(16, createItem(
                Material.BOOK,
                "🎯 Questy",
                "Daily i weekly"
        ));

        player.openInventory(inventory);
    }

    private ItemStack createItem(
            Material material,
            String name,
            String... lore
    ) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(
                Component.text(name, NamedTextColor.WHITE)
        );

        List<Component> loreComponents = new ArrayList<>();

        for (String line : lore) {
            loreComponents.add(
                    Component.text(line, NamedTextColor.GRAY)
            );
        }

        meta.lore(loreComponents);
        item.setItemMeta(meta);

        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!event.getView().title().equals(TITLE)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        String message = switch (event.getRawSlot()) {

            case 10 ->
                    "Mining bedzie dostepny w kolejnej wersji.";

            case 11 ->
                    "PvP bedzie dostepne w kolejnej wersji.";

            case 12 ->
                    "Skrzynki beda dostepne w kolejnej wersji.";

            case 14 ->
                    "Pety beda dostepne w kolejnej wersji.";

            case 15 ->
                    "Ranking bedzie dostepny w kolejnej wersji.";

            case 16 ->
                    "Questy beda dostepne w kolejnej wersji.";

            default -> null;
        };

        if (message != null) {
            player.sendMessage(
                    Component.text(message, PURPLE)
            );
        }
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {

        if (args.length > 0 &&
                args[0].equalsIgnoreCase("reload")) {

            if (!sender.hasPermission("filpflap.admin")) {
                sender.sendMessage(
                        Component.text(
                                "Brak uprawnien.",
                                NamedTextColor.RED
                        )
                );
                return true;
            }

            reloadConfig();

            sender.sendMessage(
                    Component.text(
                            "FilpFlapCore przeladowany.",
                            PURPLE
                    )
            );

            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(
                    Component.text(
                            "Tej komendy moze uzyc tylko gracz.",
                            NamedTextColor.RED
                    )
            );
            return true;
        }

        openMenu(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args
    ) {

        if (args.length == 1 &&
                sender.hasPermission("filpflap.admin")) {

            return List.of("reload");
        }

        return List.of();
    }
}
