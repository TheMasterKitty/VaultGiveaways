package net.themasterkitty.vaultgiveaways;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.regex.Pattern;

import static net.themasterkitty.vaultgiveaways.VaultGiveaways.*;
import static org.bukkit.Bukkit.getPlayer;
import static org.bukkit.Bukkit.getServer;

public class Giveaway {
    public UUID ownerUUID;
    public List<Player> entries = new ArrayList<>();
    public int awardAmount = 0;
    public ItemStack rewardItem = null;
    public int remainingTime = configuration.getInt("giveawayCountdownTime");
    public boolean cancelOnClick = false;
    public boolean givesItem = false;
    public Giveaway(UUID uuid, int money) {
        ownerUUID = uuid;
        awardAmount = money;
    }
    public Giveaway(UUID uuid, ItemStack item) {
        ownerUUID = uuid;
        rewardItem = item;
        givesItem = true;
    }
    public void giveReward() {
        if (givesItem) {
            if (entries.size() == 0) {
                if (getServer().getOfflinePlayer(ownerUUID).isOnline()) {
                    Objects.requireNonNull(getPlayer(ownerUUID)).getInventory().addItem(rewardItem);
                    if (getServer().getOfflinePlayer(ownerUUID).isOnline()) Objects.requireNonNull(getServer().getPlayer(ownerUUID)).sendMessage(translateMessage("giveawayrefunded"));
                }
                else {
                    itemsForgotten.put(getServer().getOfflinePlayer(ownerUUID), rewardItem);
                }
            }
            else {
                int winner = new Random().nextInt(entries.size());
                entries.get(winner).getInventory().addItem(rewardItem);
                if (getServer().getOfflinePlayer(ownerUUID).isOnline()) Objects.requireNonNull(getServer().getPlayer(ownerUUID)).sendMessage(translateMessage("itemgiveawaywin").replaceAll(Pattern.quote("[winner]"), entries.get(winner).getName()).replaceAll(Pattern.quote("[player]"), Objects.requireNonNull(getServer().getOfflinePlayer(ownerUUID).getName())).replaceAll(Pattern.quote("[giveaway]"), String.valueOf(rewardItem.getType())));
                for (Player p : entries) {
                    p.sendMessage(translateMessage("itemgiveawaywin").replaceAll(Pattern.quote("[winner]"), entries.get(winner).getName()).replaceAll(Pattern.quote("[player]"), Objects.requireNonNull(getServer().getOfflinePlayer(ownerUUID).getName())).replaceAll(Pattern.quote("[giveaway]"), String.valueOf(rewardItem.getType())));
                }
            }
        }
        else {
            if (entries.size() == 0) {
                econ.depositPlayer(getServer().getOfflinePlayer(ownerUUID), awardAmount).transactionSuccess();
                if (getServer().getOfflinePlayer(ownerUUID).isOnline()) Objects.requireNonNull(getServer().getPlayer(ownerUUID)).sendMessage(translateMessage("giveawayrefunded"));
            }
            else {
                int winner = new Random().nextInt(entries.size());
                econ.depositPlayer(entries.get(winner), awardAmount).transactionSuccess();
                if (getServer().getOfflinePlayer(ownerUUID).isOnline())  Objects.requireNonNull(getServer().getPlayer(ownerUUID)).sendMessage(translateMessage("moneygiveawaywin").replaceAll(Pattern.quote("[winner]"), entries.get(winner).getName()).replaceAll(Pattern.quote("[player]"), Objects.requireNonNull(getServer().getOfflinePlayer(ownerUUID).getName())).replaceAll(Pattern.quote("[giveaway]"), String.valueOf(awardAmount)));
                for (Player p : entries) {
                    p.sendMessage(translateMessage("moneygiveawaywin").replaceAll(Pattern.quote("[winner]"), entries.get(winner).getName()).replaceAll(Pattern.quote("[player]"), Objects.requireNonNull(getServer().getOfflinePlayer(ownerUUID).getName())).replaceAll(Pattern.quote("[giveaway]"), String.valueOf(awardAmount)));
                }
            }
        }
        giveaways.remove(ownerUUID);
        cooldowns.put(getServer().getOfflinePlayer(ownerUUID), configuration.getInt("giveawayCooldown"));
    }
    public void cancelGiveaway() {
        if (givesItem) {
            for (Player p : entries) {
                p.sendMessage(translateMessage("itemgiveawaycancelled").replaceAll(Pattern.quote("[player]"), "§c" + getServer().getOfflinePlayer(ownerUUID).getName()).replaceAll(Pattern.quote("[giveaway]"), rewardItem.getType().toString()));
            }
            if (getServer().getOfflinePlayer(ownerUUID).isOnline()) {
                Objects.requireNonNull(getPlayer(ownerUUID)).getInventory().addItem(rewardItem);
                Objects.requireNonNull(getServer().getPlayer(ownerUUID)).sendMessage(translateMessage("itemgiveawaycancelled").replaceAll(Pattern.quote("[player]"), "§c" + getServer().getOfflinePlayer(ownerUUID).getName()).replaceAll(Pattern.quote("[giveaway]"), rewardItem.getType().toString()));
            }
            else {
                itemsForgotten.put(getServer().getOfflinePlayer(ownerUUID), rewardItem);
            }
        }
        else
        {
            econ.depositPlayer(getServer().getOfflinePlayer(ownerUUID), awardAmount).transactionSuccess();
            for (Player p : entries) {
                p.sendMessage(translateMessage("moneygiveawaycancelled").replaceAll(Pattern.quote("[player]"), "§c" + getServer().getOfflinePlayer(ownerUUID).getName()).replaceAll(Pattern.quote("[giveaway]"), String.valueOf(awardAmount)));
            }
            if (getServer().getOfflinePlayer(ownerUUID).isOnline())  Objects.requireNonNull(getServer().getPlayer(ownerUUID)).sendMessage(translateMessage("moneygiveawaycancelled").replaceAll(Pattern.quote("[player]"), "§c" + getServer().getOfflinePlayer(ownerUUID).getName()).replaceAll(Pattern.quote("[giveaway]"), String.valueOf(awardAmount)));
        }
        giveaways.remove(ownerUUID);
        cooldowns.put(getServer().getOfflinePlayer(ownerUUID), configuration.getInt("cancelledGiveawayCooldown"));
    }
}