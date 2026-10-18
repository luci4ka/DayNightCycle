package ru.terracraft.daynightcycle.afk;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import ru.terracraft.daynightcycle.DayNightCyclePlugin;
import ru.terracraft.daynightcycle.lang.MessageKey;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AfkManager {

    private final DayNightCyclePlugin plugin;
    private final Set<UUID> afk = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Long> lastActivity = new ConcurrentHashMap<>();
    private BukkitTask task;

    public AfkManager(DayNightCyclePlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    public void stop() {
        if (task != null) { task.cancel(); task = null; }
    }

    public void reload() {
        afk.clear();
        lastActivity.clear();
        start();
    }

    public void onQuit(Player player) {
        if (player == null) return;
        UUID id = player.getUniqueId();
        afk.remove(id);
        lastActivity.remove(id);
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
        long now = System.currentTimeMillis();
        UUID id = player.getUniqueId();
        Long last = lastActivity.get(id);
        // не чаще раза в секунду, кроме выхода из AFK
        if (last != null && now - last < 1000 && !afk.contains(id)) return;
        lastActivity.put(id, now);
        if (afk.contains(id)) {
            setAfk(player, false, false);
        }
    }

    public boolean isAfk(Player player) {
        return player != null && afk.contains(player.getUniqueId());
    }

    public void setAfk(Player player, boolean value, boolean silent) {
        if (player == null) return;
        UUID id = player.getUniqueId();

        if (value) {
            if (!afk.add(id)) return;
        } else {
            lastActivity.put(id, System.currentTimeMillis());
            if (!afk.remove(id)) return;
        }

        if (!silent) {
            plugin.getLanguageManager().sendActionBar(player,
                    value ? MessageKey.AFK_ENABLED : MessageKey.AFK_DISABLED);
        }

        if (plugin.getSleepManager() != null) {
            plugin.getSleepManager().checkSleep(player.getWorld());
        }
    }

    public void toggleAfk(Player player) {
        if (player == null) return;
        if (!plugin.getMainConfig().afk.enabled) {
            plugin.getLanguageManager().sendActionBar(player, MessageKey.AFK_DISABLED_SYSTEM);
            return;
        }
        if (player.hasPermission("dnc.bypass")) return;

        boolean next = !isAfk(player);
        setAfk(player, next, true);

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