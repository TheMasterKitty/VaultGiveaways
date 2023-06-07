package net.themasterkitty.vaultgiveaways;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static org.bukkit.Bukkit.getPluginManager;

public final class VaultGiveaways extends JavaPlugin implements CommandExecutor, Listener {
    private static final Logger log = Logger.getLogger("Minecraft");
    public static Economy econ = null;
    public static Map<UUID, Giveaway> giveaways = new HashMap<>();
    public static Map<OfflinePlayer, Integer> cooldowns = new HashMap<>();
    public static YamlConfiguration configuration = new YamlConfiguration();
    public static Map<OfflinePlayer, ItemStack> itemsForgotten = new HashMap<>();
    YamlConfiguration forgotten = new YamlConfiguration();
    File configFile = new File(getDataFolder() + "/config.yml");
    @Override
    public void onEnable() {
        giveaways = new HashMap<>();
        cooldowns = new HashMap<>();
        itemsForgotten = new HashMap<>();

        try {
            getDataFolder().mkdirs();
            new File(getDataFolder() + "/forgotten.yml").createNewFile();
            configFile.createNewFile();
            forgotten.load(new File(getDataFolder() + "/forgotten.yml"));
            configuration.load(configFile);
            if (!configuration.contains("giveawayCountdownTime")) {
                configuration.set("giveawayCountdownTime", 60);
                configuration.setComments("giveawayCountdownTime", Collections.singletonList("The amount of time it takes for the giveaway to end"));
            }
            if (!configuration.contains("giveawayCooldown")) {
                configuration.set("giveawayCooldown", 300);
                configuration.setComments("giveawayCooldown", Collections.singletonList("The cooldown for a player to make another giveaway once their giveaway ends"));
            }
            if (!configuration.contains("cancelledGiveawayCooldown")) {
                configuration.set("cancelledGiveawayCooldown", 300);
                configuration.setComments("cancelledGiveawayCooldown", Collections.singletonList("The cooldown for a player to make another giveaway if they cancel their giveaway"));
            }
            if (!configuration.contains("enableMoneyGiveaways")) {
                configuration.set("enableMoneyGiveaways", true);
                configuration.setComments("enableMoneyGiveaways", Collections.singletonList("Allow users to give away money"));
            }
            if (!configuration.contains("enableItemGiveaways")) {
                configuration.set("enableItemGiveaways", true);
                configuration.setComments("enableItemGiveaways", Collections.singletonList("Allow users to give away items"));
            }
            if (!configuration.contains("giveawayCountdownTime")) {
                configuration.set("giveawayCountdownTime", 60);
                configuration.setComments("giveawayCountdownTime", Collections.singletonList("The amount of time it takes for the giveaway to end"));
            }
            if (!configuration.contains("minimumGiveawayMoney")) {
                configuration.set("minimumGiveawayMoney", 2500);
                configuration.setComments("minimumGiveawayMoney", Collections.singletonList("The minimum amount of money on a giveaway"));
            }
            if (!configuration.contains("itemWhitelistOn")) {
                configuration.set("itemWhitelistOn", false);
                configuration.setComments("itemWhitelistOn", Collections.singletonList("Requires an item to be on the whitelist to give away"));
            }
            if (!configuration.contains("itemWhitelist")) {
                configuration.set("itemWhitelist", new ArrayList<>());
                configuration.setComments("itemWhitelist", Arrays.asList("Lists: Uses list of the material (like 'iron_ingot') or in the format 'iron:tools' using any ore type (or leather/chainmail for armor) and 'tools', 'weapons' (axe & sword), 'armor', or 'all' 3.", "If the whitelist is turned on, these items will be the only ones that can be sold"));
            }
            if (!configuration.contains("itemBlacklist")) {
                configuration.set("itemBlacklist", new ArrayList<>());
                configuration.setComments("itemBlacklist", Collections.singletonList("An item cannot be on this list and be given away even if the whitelist is on"));
            }
            if (!configuration.contains("moneygiveawaycreation")) {
                configuration.set("moneygiveawaycreation", "&b[player] created a giveaway of &2$&a[giveaway]");
                configuration.setComments("moneygiveawaycreation", Collections.singletonList("All of below are configurable messages sent to players. You can use & for color codes, '[player]' for the giveaway starter (if applicable), '[winner]' for the giveaway winner (if applicable), and '[giveaway]' for the giveaway item / amount if applicable. Use [newline] for a new line."));
            }
            if (!configuration.contains("itemgiveawaycreation")) {
                configuration.set("itemgiveawaycreation", "&b[player] created a giveaway of [giveaway]");
            }
            if (!configuration.contains("entergiveawayclickable")) {
                configuration.set("entergiveawayclickable", "&6&n[Enter Here]");
            }
            if (!configuration.contains("giveawayitemdenied")) {
                configuration.set("giveawayitemdenied", "&cYou aren't allowed to give away this item.");
            }
            if (!configuration.contains("giveawaytoomuchmoney")) {
                configuration.set("giveawaytoomuchmoney", "&cYou don't have that much money to give away.");
            }
            if (!configuration.contains("giveawaybelowmin")) {
                configuration.set("giveawaybelowmin", "&cYou need to give away $[minimum] or more.");
            }
            if (!configuration.contains("giveawaynothing")) {
                configuration.set("giveawaynothing", "&cYou can't give away nothing.");
            }
            if (!configuration.contains("invalidinteger")) {
                configuration.set("invalidinteger", "&cInvalid Integer Provided");
            }
            if (!configuration.contains("giveawaymoneyusage")) {
                configuration.set("giveawaymoneyusage", "&cUsage: /giveawaymoney [amount]");
            }
            if (!configuration.contains("activegiveaway")) {
                configuration.set("activegiveaway", "&cYou still have an active giveaway. Do /cancelgiveaway to cancel it.");
            }
            if (!configuration.contains("giveawaycooldown")) {
                configuration.set("giveawaycooldown", "&cYou still have [mins]:[secs] remaining before making another giveaway.");
            }
            if (!configuration.contains("moneygiveawaysdisabled")) {
                configuration.set("moneygiveawaysdisabled", "&cMoney giveaways are disabled on this server.");
            }
            if (!configuration.contains("itemgiveawaysdisabled")) {
                configuration.set("itemgiveawaysdisabled", "&cItem giveaways are disabled on this server.");
            }
            if (!configuration.contains("enteredgiveaway")) {
                configuration.set("enteredgiveaway", "&bYou have entered the giveaway.");
            }
            if (!configuration.contains("enterowngiveaway")) {
                configuration.set("enterowngiveaway", "&cYou can't enter your own giveaway! Click again to cancel it.");
            }
            if (!configuration.contains("ingiveaway")) {
                configuration.set("ingiveaway", "&cYou are already in this giveaway. Disconnect and rejoin to exit it.");
            }
            if (!configuration.contains("giveawaynonexistant")) {
                configuration.set("giveawaynonexistant", "&cThat giveaway does not exist.");
            }
            if (!configuration.contains("invalidplayer")) {
                configuration.set("invalidplayer", "&cInvalid UUID/Username Provided.");
            }
            if (!configuration.contains("entergiveawayusage")) {
                configuration.set("entergiveawayusage", "&cUsage: /entergiveaway [uuid/playername]");
            }
            if (!configuration.contains("giveawayinfo")) {
                configuration.set("giveawayinfo", "&6&lVaultGiveaways - by TheMasterKitty[newline]&aGiving away money: &l/giveaway [amount][newline]&aGiving away item in hand: &l/giveawayitem[newline]&aCancelling a giveaway: &l/cancelgiveaway[newline]&aEntering a giveaway: &l/entergiveaway [playername]");
            }
            if (!configuration.contains("giveawayrefunded")) {
                configuration.set("giveawayrefunded", "&bYour giveaway was refunded as there were no entries.");
            }
            if (!configuration.contains("moneygiveawaywin")) {
                configuration.set("moneygiveawaywin", "&b[winner] won the giveaway of &2$&a[giveaway]&b by [player]");
            }
            if (!configuration.contains("itemgiveawaywin")) {
                configuration.set("itemgiveawaywin", "&b[winner] won the giveaway of [giveaway] by [player]");
            }
            if (!configuration.contains("moneygiveawaycancelled")) {
                configuration.set("moneygiveawaycancelled", "&c[player] cancelled their giveaway of &2$&a[giveaway]");
            }
            if (!configuration.contains("itemgiveawaycancelled")) {
                configuration.set("itemgiveawaycancelled", "&c[player] cancelled their giveaway of [giveaway]");
            }

            configuration.save(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
        if (!setupEconomy()) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Objects.requireNonNull(Bukkit.getPluginCommand("giveawaymoney")).setExecutor(this);
        Objects.requireNonNull(Bukkit.getPluginCommand("giveawayitem")).setExecutor(this);
        Objects.requireNonNull(Bukkit.getPluginCommand("entergiveaway")).setExecutor(this);
        Objects.requireNonNull(Bukkit.getPluginCommand("cancelgiveaway")).setExecutor(this);
        Objects.requireNonNull(Bukkit.getPluginCommand("vaultgiveaways")).setExecutor(this);
        Objects.requireNonNull(Bukkit.getPluginCommand("reloadvaultgiveaways")).setExecutor(this);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            try {
                giveaways.forEach((uuid, giveaway) -> {
                    if (giveaway.remainingTime != 0) giveaway.remainingTime--;
                    else giveaway.giveReward();
                });
            } catch (Exception ignored) { }
            try {
                cooldowns.forEach((player, integer) -> {
                    if (integer != 0) cooldowns.put(player, integer - 1);
                });
            } catch (Exception ignored) { }
        }, 0, 20);
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            try {
                int moneyPlayersForgotten = forgotten.getInt("moneyPlayersForgotten");
                for (int i = 0; i < moneyPlayersForgotten; i++)
                    econ.depositPlayer(forgotten.getOfflinePlayer("moneyPlayerForgotten" + i), forgotten.getInt("moneyPlayerAmountForgotten" + i)).transactionSuccess();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                int itemPlayersForgotten = forgotten.getInt("itemPlayersForgotten");
                for (int i = 0; i < itemPlayersForgotten; i++) {
                    itemsForgotten.put(forgotten.getOfflinePlayer("itemPlayerForgotten" + i), forgotten.getItemStack("itemPlayerStackForgotten" + i));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                forgotten.loadFromString("");
                new File(getDataFolder() + "/forgotten.yml").delete();
            } catch (InvalidConfigurationException e) {
                throw new RuntimeException(e);
            }
        }, 40);
        getServer().getPluginManager().registerEvents(this, this);
    }
    @Override
    public void onDisable() {
        try {
            forgotten.set("moneyPlayersForgotten", 0);
            forgotten.set("itemPlayersForgotten", 0);
            giveaways.forEach((uuid, giveaway) -> {
                try {
                    if (giveaway.givesItem) {
                        forgotten.set("itemPlayerForgotten" + forgotten.getInt("itemPlayersForgotten"), getServer().getOfflinePlayer(uuid));
                        forgotten.set("itemPlayerStackForgotten" + forgotten.getInt("itemPlayersForgotten"), giveaway.rewardItem);
                        forgotten.set("itemPlayersForgotten", forgotten.getInt("itemPlayersForgotten") + 1);
                    }
                    else {
                        forgotten.set("moneyPlayerForgotten" + forgotten.getInt("moneyPlayersForgotten"), getServer().getOfflinePlayer(uuid));
                        forgotten.set("moneyPlayerAmountForgotten" + forgotten.getInt("moneyPlayersForgotten"), giveaway.awardAmount);
                        forgotten.set("moneyPlayersForgotten", forgotten.getInt("moneyPlayersForgotten") + 1);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            itemsForgotten.forEach((player, item) -> {
                try {
                    forgotten.set("itemPlayerForgotten" + forgotten.getInt("itemPlayersForgotten"), player);
                    forgotten.set("itemPlayerStackForgotten" + forgotten.getInt("itemPlayersForgotten"), item);
                    forgotten.set("itemPlayersForgotten", forgotten.getInt("itemPlayersForgotten") + 1);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            forgotten.save(new File(getDataFolder() + "/forgotten.yml"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return true;
    }
    public static String translateMessage(String translationKey) {
        return Objects.requireNonNull(configuration.getString(translationKey)).replaceAll("&", "§").replaceAll(Pattern.quote("[newline]"), "\n");
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (itemsForgotten.containsKey(e.getPlayer())) {
            e.getPlayer().getLocation().getWorld().dropItem(e.getPlayer().getLocation(), itemsForgotten.get(e.getPlayer()));
            itemsForgotten.remove(e.getPlayer());
        }
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        giveaways.forEach((uuid, giveaway) -> giveaway.entries.remove(e.getPlayer()));
    }
    public List<Material> convertList(List<String> list) {
        List<Material> updatedList = new ArrayList<>();
        list.forEach(name -> {
            try {
                updatedList.add(Material.getMaterial(name));
            }
            catch (Exception ignored) {
                if (name.contains("/")) {
                    String material = name.split(":")[0];
                    String type = name.split(":")[1];
                    boolean b1 = Objects.equals(material, "leather") || Objects.equals(material, "chainmail") || Objects.equals(material, "iron") || Objects.equals(material, "gold") || Objects.equals(material, "diamond") || Objects.equals(material, "netherite");
                    if (Objects.equals(type, "armor")) {
                        if (b1) {
                            material = "minecraft:" + material;
                            updatedList.add(Material.getMaterial(material + "_helmet"));
                            updatedList.add(Material.getMaterial(material + "_chestplate"));
                            updatedList.add(Material.getMaterial(material + "_leggings"));
                            updatedList.add(Material.getMaterial(material + "_boots"));
                        }
                    }
                    else {
                        boolean b = Objects.equals(material, "wooden") || Objects.equals(material, "stone") || Objects.equals(material, "iron") || Objects.equals(material, "gold") || Objects.equals(material, "diamond") || Objects.equals(material, "netherite");
                        if (Objects.equals(type, "weapons")) {
                            if (b) {
                                material = "minecraft:" + material;
                                updatedList.add(Material.getMaterial(material + "_axe"));
                                updatedList.add(Material.getMaterial(material + "_sword"));
                            }
                        }
                        else if (Objects.equals(type, "tools")) {
                            if (b) {
                                material = "minecraft:" + material;
                                updatedList.add(Material.getMaterial(material + "_pickaxe"));
                                updatedList.add(Material.getMaterial(material + "_hoe"));
                                updatedList.add(Material.getMaterial(material + "_shovel"));
                            }
                        }
                        else if (Objects.equals(type, "all")) {
                            if (b1) {
                                material = "minecraft:" + material;
                                updatedList.add(Material.getMaterial(material + "_helmet"));
                                updatedList.add(Material.getMaterial(material + "_chestplate"));
                                updatedList.add(Material.getMaterial(material + "_leggings"));
                                updatedList.add(Material.getMaterial(material + "_boots"));
                            }
                            if (Objects.equals(material, "wooden") || Objects.equals(material, "stone") || Objects.equals(material, "iron") || Objects.equals(material, "gold") || Objects.equals(material, "netherite")) {
                                material = "minecraft:" + material;
                                updatedList.add(Material.getMaterial(material + "_pickaxe"));
                                updatedList.add(Material.getMaterial(material + "_hoe"));
                                updatedList.add(Material.getMaterial(material + "_shovel"));
                                updatedList.add(Material.getMaterial(material + "_axe"));
                                updatedList.add(Material.getMaterial(material + "_sword"));
                            }
                        }
                    }
                }
            }
        });
        return updatedList;
    }
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String commandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            log.info("Non-players cannot run this command.");
            return false;
        }

        Player player = (Player) sender;
        if (command.getLabel().equals("giveawaymoney")) {
            if (configuration.getBoolean("enableMoneyGiveaways")) {
                if ((!cooldowns.containsKey(player) || cooldowns.get(player) == 0) && !giveaways.containsKey(player.getUniqueId())) {
                    if (args.length == 1) {
                        try {
                            int i = Integer.parseInt(args[0]);
                            if (i >= configuration.getInt("minimumGiveawayMoney")) {
                                if (econ.getBalance(player) >= i) {
                                    EconomyResponse r = econ.withdrawPlayer(player, i);
                                    if (r.transactionSuccess()) {
                                        UUID uuid = player.getUniqueId();
                                        giveaways.put(uuid, new Giveaway(uuid, i));
                                        for (Player p : Bukkit.getOnlinePlayers()) {
                                            TextComponent component = new TextComponent(translateMessage("moneygiveawaycreation").replaceAll(Pattern.quote("[player]"), player.getName()).replaceAll(Pattern.quote("[giveaway]"), String.valueOf(i)) + " ");
                                            TextComponent addition = new TextComponent(translateMessage("entergiveawayclickable"));
                                            addition.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click here to enter the giveaway!")));
                                            addition.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/entergiveaway " + player.getUniqueId()));
                                            component.addExtra(addition);
                                            p.sendMessage(component);
                                        }

                                        return true;
                                    }
                                } else {
                                    sender.sendMessage(translateMessage("giveawaytoomuchmoney"));
                                    return false;
                                }
                            } else {
                                sender.sendMessage(translateMessage("giveawaybelowmin").replaceAll(Pattern.quote("[minimum]"), String.valueOf(configuration.getInt("minimumGiveawayMoney"))));
                                return false;
                            }
                        } catch (Exception ignored) {
                            player.sendMessage(translateMessage("invalidinteger"));
                            return false;
                        }
                    } else {
                        player.sendMessage(translateMessage("giveawaymoneyusage"));
                        return false;
                    }
                } else if (giveaways.containsKey(player.getUniqueId())) {
                    player.sendMessage(translateMessage("activegiveaway"));
                    return false;
                } else {
                    int mins = (int) Math.floor((double) cooldowns.get(player) / 60);
                    int secs = cooldowns.get(player) % 60;
                    String seconds = String.valueOf(secs);
                    if (secs < 10) seconds = "0" + seconds;
                    player.sendMessage(translateMessage("giveawaycooldown").replaceAll(Pattern.quote("[mins]"), String.valueOf(mins)).replaceAll(Pattern.quote("[secs]"), seconds));
                    return false;
                }
            }
            else {
                player.sendMessage(translateMessage("moneygiveawaysdisabled"));
                return false;
            }
        }
        else if (command.getLabel().equals("giveawayitem")) {
            if (configuration.getBoolean("enableItemGiveaways")) {
                if ((!cooldowns.containsKey(player) || cooldowns.get(player) == 0) && !giveaways.containsKey(player.getUniqueId())) {
                    ItemStack item = player.getInventory().getItemInMainHand();
                    if (item.getType() != Material.AIR) {
                        if (configuration.getBoolean("itemWhitelistOn")) {
                            if (convertList(configuration.getStringList("itemWhitelist")).contains(item.getType()) && !convertList(configuration.getStringList("itemBlacklist")).contains(item.getType())) {
                                UUID uuid = player.getUniqueId();
                                if (player.getInventory().contains(item)) {
                                    player.getInventory().remove(item);
                                    giveaways.put(uuid, new Giveaway(uuid, item));
                                    for (Player p : Bukkit.getOnlinePlayers()) {
                                        TextComponent component = new TextComponent(translateMessage("itemgiveawaycreation").replaceAll(Pattern.quote("[player]"), player.getName()).replaceAll(Pattern.quote("[giveaway]"), item.getType().toString()) + " ");
                                        TextComponent addition = new TextComponent(translateMessage("entergiveawayclickable"));
                                        addition.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click here to enter the giveaway!")));
                                        addition.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/entergiveaway " + player.getUniqueId()));
                                        component.addExtra(addition);
                                        p.sendMessage(component);
                                    }
                                }
                            } else {
                                player.sendMessage(translateMessage("giveawayitemdenied"));
                                return false;
                            }
                        } else {
                            if (!convertList(configuration.getStringList("itemBlacklist")).contains(item.getType())) {
                                UUID uuid = player.getUniqueId();
                                if (player.getInventory().contains(item)) {
                                    player.getInventory().remove(item);
                                    giveaways.put(uuid, new Giveaway(uuid, item));
                                    for (Player p : Bukkit.getOnlinePlayers()) {
                                        TextComponent component = new TextComponent(translateMessage("itemgiveawaycreation").replaceAll(Pattern.quote("[player]"), player.getName()).replaceAll(Pattern.quote("[giveaway]"), item.getType().toString()) + " ");
                                        TextComponent addition = new TextComponent(translateMessage("entergiveawayclickable"));
                                        addition.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click here to enter the giveaway!")));
                                        addition.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/entergiveaway " + player.getUniqueId()));
                                        component.addExtra(addition);
                                        p.sendMessage(component);
                                    }
                                }
                            } else {
                                player.sendMessage(translateMessage("giveawayitemdenied"));
                                return false;
                            }
                        }
                    } else {
                        player.sendMessage(translateMessage("giveawaynothing"));
                        return false;
                    }
                } else if (giveaways.containsKey(player.getUniqueId())) {
                    player.sendMessage(translateMessage("activegiveaway"));
                    return false;
                } else {
                    int mins = (int) Math.floor((double) cooldowns.get(player) / 60);
                    int secs = cooldowns.get(player) % 60;
                    String seconds = String.valueOf(secs);
                    if (secs < 10) seconds = "0" + seconds;
                    player.sendMessage(translateMessage("giveawaycooldown").replaceAll(Pattern.quote("[mins]"), String.valueOf(mins)).replaceAll(Pattern.quote("[secs]"), seconds));
                    return false;
                }
            }
            else {
                player.sendMessage(translateMessage("itemgiveawaysdisabled"));
                return false;
            }
        }
        else if (command.getLabel().equals("entergiveaway")) {
            if (args.length == 1) {
                try {
                    UUID uuid = UUID.fromString(args[0]);
                    if (giveaways.containsKey(uuid)) {
                        if (!giveaways.get(uuid).entries.contains(player)) {
                            if (getServer().getOfflinePlayer(uuid) != player) {
                                giveaways.get(uuid).entries.add(player);
                                player.sendMessage(translateMessage("enteredgiveaway"));
                                return true;
                            }
                            else if (!giveaways.get(uuid).cancelOnClick) {
                                giveaways.get(uuid).cancelOnClick = true;
                                player.sendMessage(translateMessage("enterowngiveaway"));
                                return false;
                            }
                            else giveaways.get(uuid).cancelGiveaway();
                        }
                        else {
                            player.sendMessage(translateMessage("ingiveaway"));
                            return false;
                        }
                    }
                    else {
                        player.sendMessage(translateMessage("giveawaynonexistant"));
                        return false;
                    }
                }
                catch (Exception ignored) {
                    try {
                        UUID uuid = Objects.requireNonNull(getServer().getOfflinePlayer(args[0])).getUniqueId();
                        if (giveaways.containsKey(uuid)) {
                            if (!giveaways.get(uuid).entries.contains(player)) {
                                if (getServer().getOfflinePlayer(uuid) != player) {
                                    giveaways.get(uuid).entries.add(player);
                                    player.sendMessage(translateMessage("enteredgiveaway"));
                                    return true;
                                }
                                else if (!giveaways.get(uuid).cancelOnClick) {
                                    giveaways.get(uuid).cancelOnClick = true;
                                    player.sendMessage(translateMessage("enterowngiveaway"));
                                    return false;
                                }
                                else giveaways.get(uuid).cancelGiveaway();
                            }
                            else {
                                player.sendMessage(translateMessage("ingiveaway"));
                                return false;
                            }
                        }
                        else {
                            player.sendMessage(translateMessage("giveawaynonexistant"));
                            return false;
                        }
                    } catch (Exception ignored2) {
                        player.sendMessage(translateMessage("invalidplayer"));
                        return false;
                    }
                }
            }
            else {
                player.sendMessage(translateMessage("entergiveawayusage"));
                return false;
            }
        }
        else if (command.getLabel().equals("cancelgiveaway")) {
            if (giveaways.containsKey(player.getUniqueId())) {
                giveaways.get(player.getUniqueId()).cancelGiveaway();
                return true;
            }
            return false;
        }
        else if (command.getLabel().equals("vaultgiveaways")) {
            player.sendMessage(translateMessage("giveawayinfo"));
            return true;
        }
        else if (command.getLabel().equals("reloadvaultgiveaways")) {
            getPluginManager().disablePlugin(this);
            getPluginManager().enablePlugin(this);
        }
        return false;
    }
}
