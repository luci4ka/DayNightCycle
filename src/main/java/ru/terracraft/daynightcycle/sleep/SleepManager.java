package ru.terracraft.daynightcycle.sleep;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import ru.terracraft.daynightcycle.DayNightCyclePlugin;
import ru.terracraft.daynightcycle.lang.MessageKey;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SleepManager {

    private final DayNightCyclePlugin plugin;
    private final Map<UUID, BukkitTask> animationTasks = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> skipping = new ConcurrentHashMap<>();

    public SleepManager(DayNightCyclePlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        animationTasks.values().forEach(BukkitTask::cancel);
        animationTasks.clear();
        skipping.clear();
    }

    public void shutdown() {
        reload();
    }

    public boolean isSkipping(World world) {
        return Boolean.TRUE.equals(skipping.get(world.getUID()));
    }

    public void checkSleep(World world) {
        if (isSkipping(world)) return;
        if (!plugin.getMainConfig().sleep.enabled) return;
        if (world.getEnvironment() != World.Environment.NORMAL) return;

        long time = world.getTime();
        if (time < 12500 || time > 23500) return;

        int required = getRequiredSleepers(world);
        int sleeping = countSleeping(world);
        int realSleepers = countRealSleepers(world);

        if (realSleepers < 1) return; // хотя бы один реально спящий
        if (sleeping >= required && required > 0) {
            skipNight(world, sleeping, required);
        }
    }

    private int countRealSleepers(World world) {
        int count = 0;
        for (Player p : world.getPlayers()) {
            if (p.hasPermission("dnc.bypass")) continue;
            if (p.isSleepingIgnored()) continue;
            if (p.isSleeping()) count++;
        }
        return count;
    }

    private int getRequiredSleepers(World world) {
        int online = 0;
        for (Player p : world.getPlayers()) {
            if (p.hasPermission("dnc.bypass")) continue;
            if (p.isSleepingIgnored()) continue;
            online++;
        }
        double percent = plugin.getMainConfig().sleep.percent;
        return Math.max(1, (int) Math.ceil(online * percent - 1e-9));
    }

    private int countSleeping(World world) {
        Set<UUID> counted = new HashSet<>();
        int count = 0;

        for (Player p : world.getPlayers()) {
            if (p.hasPermission("dnc.bypass")) continue;
            if (p.isSleepingIgnored()) continue;

            if (p.isSleeping()) { counted.add(p.getUniqueId()); count++; continue; }
            if (plugin.getMainConfig().sleep.afkCountsAsSleeping
                    && plugin.getAfkManager().isAfk(p)) {
                counted.add(p.getUniqueId());
                count++;
            }
        }

        for (String name : plugin.getSleepList().players) {
            Player p = plugin.getServer().getPlayerExact(name);
            if (p == null) continue;
            if (!p.getWorld().equals(world)) continue;
            if (p.hasPermission("dnc.bypass")) continue;
            if (p.isSleepingIgnored()) continue;
            if (counted.contains(p.getUniqueId())) continue;
            counted.add(p.getUniqueId());
            count++;
        }
        return count;
    }

    private void skipNight(World world, int sleeping, int required) {
        skipping.put(world.getUID(), true);
        try {
            if (plugin.getMainConfig().sleep.animation.enabled) {
                animateNight(world, sleeping, required);
            } else {
                finishSkip(world, sleeping, required);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Sleep skip failed: " + e.getMessage());
            skipping.remove(world.getUID());
        }
    }

    private void animateNight(World world, int sleeping, int required) {
        int durationTicks = plugin.getMainConfig().sleep.animation.durationSeconds * 20;
        long startTime = world.getTime();
        long targetTime = 0L;
        long delta = targetTime - startTime;
        if (delta < 0) delta += 24000L;

        final long totalDelta = delta;
        final int totalTicks = durationTicks;
        final long start = startTime;

        BukkitTask old = animationTasks.remove(world.getUID());
        if (old != null) old.cancel();

        for (Player p : world.getPlayers()) {
            plugin.getLanguageManager().sendActionBar(p, MessageKey.SLEEP_ANIMATION_START);
        }

        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
            int elapsed = 0;

            @Override
            public void run() {
                try {
                    if (elapsed >= totalTicks) {
                        finishSkip(world, sleeping, required);
                        BukkitTask t = animationTasks.remove(world.getUID());
                        if (t != null) t.cancel();
                        return;
                    }
                    long progress = (long) ((double) elapsed / totalTicks * totalDelta);
                    world.setTime((start + progress) % 24000L);
                    elapsed++;
                } catch (Exception e) {
                    plugin.getLogger().warning("Sleep animation failed: " + e.getMessage());
                    BukkitTask t = animationTasks.remove(world.getUID());
                    if (t != null) t.cancel();
                    skipping.remove(world.getUID());
                }
            }
        }, 0L, 1L);

        animationTasks.put(world.getUID(), task);
    }

    private void finishSkip(World world, int sleeping, int required) {
        world.setTime(0);
        if (plugin.getMainConfig().sleep.clearWeather) {
            world.setStorm(false);
            world.setThundering(false);
        }

        if (plugin.getMainConfig().sleep.broadcastSkip) {
            for (Player p : world.getPlayers()) {
                String msg = plugin.getLanguageManager().getMessage(p, MessageKey.SLEEP_SKIP)
                        .replace("%sleeping%", String.valueOf(sleeping))
                        .replace("%required%", String.valueOf(required));
                p.sendMessage(plugin.getLanguageManager().render(msg, p));
            }
        }

        skipping.remove(world.getUID());
    }
}