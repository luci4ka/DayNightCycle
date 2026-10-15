package ru.terracraft.daynightcycle.sleep;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import ru.terracraft.daynightcycle.DayNightCyclePlugin;
import ru.terracraft.daynightcycle.lang.MessageKey;

import java.util.HashSet;
import java.util.Set;

public class SleepManager {

    private final DayNightCyclePlugin plugin;
    private BukkitTask animationTask;
    private boolean skipping = false;

    public SleepManager(DayNightCyclePlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        if (animationTask != null) { animationTask.cancel(); animationTask = null; }
        skipping = false;
    }

    public void shutdown() {
        if (animationTask != null) {
            animationTask.cancel();
            animationTask = null;
        }
        skipping = false;
    }

    public void checkSleep(World world) {
        if (skipping) return;
        if (!plugin.getMainConfig().sleep.enabled) return;
        if (world.getEnvironment() != World.Environment.NORMAL) return;

        long time = world.getTime();
        if (time < 12500 || time > 23500) return;

        int required = getRequiredSleepers(world);
        int sleeping = countSleeping(world);

        if (sleeping >= required && required > 0) {
            skipNight(world, sleeping, required);
        }
    }

    private int getRequiredSleepers(World world) {
        int online = 0;
        for (Player p : world.getPlayers()) {
            if (p.hasPermission("dnc.bypass")) continue;
            online++;
        }
        double percent = plugin.getMainConfig().sleep.percent;
        return Math.max(1, (int) Math.ceil(online * percent));
    }

    private int countSleeping(World world) {
        Set<String> counted = new HashSet<>();
        int count = 0;

        for (Player p : world.getPlayers()) {
            if (p.isSleeping()) { counted.add(p.getName().toLowerCase()); count++; continue; }

            if (plugin.getMainConfig().sleep.afkCountsAsSleeping
                    && plugin.getAfkManager().isAfk(p)) {
                counted.add(p.getName().toLowerCase());
                count++;
            }
        }

        for (String name : plugin.getSleepList().players) {
            if (counted.contains(name.toLowerCase())) continue;
            Player p = plugin.getServer().getPlayerExact(name);
            if (p != null && p.getWorld().equals(world)) {
                counted.add(name.toLowerCase());
                count++;
            }
        }
        return count;
    }

    private void skipNight(World world, int sleeping, int required) {
        skipping = true;
        if (plugin.getMainConfig().sleep.animation.enabled) {
            animateNight(world, sleeping, required);
        } else {
            finishSkip(world, sleeping, required);
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

        if (animationTask != null) animationTask.cancel();

        // ─── Action bar: "ночь ускоряется" ───
        for (Player p : world.getPlayers()) {
            plugin.getLanguageManager().sendActionBar(p, MessageKey.SLEEP_ANIMATION_START);
        }

        animationTask = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
            int elapsed = 0;

            @Override
            public void run() {
                if (elapsed >= totalTicks) {
                    world.setTime(0);
                    world.setStorm(false);
                    world.setThundering(false);
                    finishSkip(world, sleeping, required);
                    if (animationTask != null) {
                        animationTask.cancel();
                        animationTask = null;
                    }
                    return;
                }
                long progress = (long) ((double) elapsed / totalTicks * totalDelta);
                world.setTime((start + progress) % 24000L);
                elapsed++;
            }
        }, 0L, 1L);
    }

    private void finishSkip(World world, int sleeping, int required) {
        world.setTime(0);
        world.setStorm(false);
        world.setThundering(false);

        if (plugin.getMainConfig().sleep.broadcastSkip) {
            String msg = plugin.getLanguageManager().getMessage(MessageKey.SLEEP_SKIP)
                    .replace("%sleeping%", String.valueOf(sleeping))
                    .replace("%required%", String.valueOf(required));
            for (Player p : world.getPlayers()) {
                p.sendMessage(plugin.getLanguageManager().render(msg, p));
            }
        }

        skipping = false;
    }
}