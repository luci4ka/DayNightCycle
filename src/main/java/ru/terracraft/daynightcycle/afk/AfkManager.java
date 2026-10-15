package ru.terracraft.daynightcycle.afk;


import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import ru.terracraft.daynightcycle.DayNightCyclePlugin;
import ru.terracraft.daynightcycle.lang.MessageKey;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AfkManager {

    private final DayNightCyclePlugin plugin;
    private final NamespacedKey afkKey;
    private final Map<UUID, Long> lastActivity = new HashMap<>();
    private BukkitTask task;

    public AfkManager(DayNightCyclePlugin plugin) {
        this.plugin = plugin;
        this.afkKey = new NamespacedKey(plugin, "afk_status");
    }

    public void start() {
        stop();
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    public void stop() {
        if (task != null) { task.cancel(); task = null; }
        lastActivity.clear();
    }

    public void reload() {
        lastActivity.keySet().removeIf(uuid -> plugin.getServer().getPlayer(uuid) == null);
        start();
    }

    public void onQuit(Player player) {
        if (player == null) return;
        lastActivity.remove(player.getUniqueId());
        // PDC сохраняется в playerdata сам по себе — ничего чистить не нужно
    }

    private void tick() {
        if (!plugin.getMainConfig().afk.enabled) return;
        long timeoutMs = plugin.getMainConfig().afk.timeoutMinutes * 60_000L;
        long now = System.currentTimeMillis();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.hasPermission("dnc.bypass")) continue;
            UUID uuid = player.getUniqueId();
            Long last = lastActivity.get(uuid);
            if (last == null) { lastActivity.put(uuid, now); continue; }

            if (!isAfk(player) && now - last >= timeoutMs) {
                setAfk(player, true, true);
            }
        }
    }

    public void updateActivity(Player player) {
        if (player == null) return;
        lastActivity.put(player.getUniqueId(), System.currentTimeMillis());
        if (isAfk(player)) {
            setAfk(player, false, false);
        }
    }

    public boolean isAfk(Player player) {
        if (player == null) return false;
        Boolean value = player.getPersistentDataContainer().get(afkKey, PersistentDataType.BOOLEAN);
        return value != null && value;
    }

    public void setAfk(Player player, boolean afk, boolean silent) {
        if (player == null) return;

        if (afk) {
            player.getPersistentDataContainer().set(afkKey, PersistentDataType.BOOLEAN, true);
            if (!silent) {
                plugin.getLanguageManager().sendActionBar(player, MessageKey.AFK_ENABLED);
            }
        } else {
            player.getPersistentDataContainer().remove(afkKey);
            if (!silent) {
                plugin.getLanguageManager().sendActionBar(player, MessageKey.AFK_DISABLED);
            }
        }
    }

    public void toggleAfk(Player player) {
        boolean next = !isAfk(player);
        setAfk(player, next, true);

        // Ручное переключение — тоже в action bar
        plugin.getLanguageManager().sendActionBar(
                player,
                MessageKey.AFK_MANUAL_TOGGLE,
                "%state%",
                plugin.getLanguageManager().getMessage(player,
                        next ? MessageKey.AFK_STATE_ON : MessageKey.AFK_STATE_OFF));
    }

    public int countAfk() {
        int c = 0;
        for (Player p : plugin.getServer().getOnlinePlayers()) if (isAfk(p)) c++;
        return c;
    }
}