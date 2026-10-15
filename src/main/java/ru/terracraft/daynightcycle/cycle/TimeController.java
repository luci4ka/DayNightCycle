package ru.terracraft.daynightcycle.cycle;

import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import ru.terracraft.daynightcycle.DayNightCyclePlugin;

public class TimeController {

    private final DayNightCyclePlugin plugin;
    private BukkitTask task;

    public TimeController(DayNightCyclePlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();
        if (!plugin.getMainConfig().cycle.enabled) return;
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    public void stop() {
        if (task != null) { task.cancel(); task = null; }
    }

    public void reload() {
        start();
    }

    private void tick() {
        if (!plugin.getMainConfig().cycle.enabled) return;

        long dayMinutes = plugin.getMainConfig().cycle.dayMinutes;
        long nightMinutes = plugin.getMainConfig().cycle.nightMinutes;

        // Скорость: сколько ванильных тиков проходит за 1 секунду
        // tick() вызывается раз в секунду (20L)
        double daySpeed = 12000.0 / (dayMinutes * 60.0);
        double nightSpeed = 12000.0 / (nightMinutes * 60.0);

        for (World world : plugin.getServer().getWorlds()) {
            if (!applies(world)) continue;
            if (world.getEnvironment() != World.Environment.NORMAL) continue;

            long time = world.getTime();
            double speed = (time < 12000) ? daySpeed : nightSpeed;

            long newTime = time + Math.round((float) speed);
            if (newTime >= 24000) newTime -= 24000;
            if (newTime < 0) newTime += 24000;

            world.setTime(newTime);
        }
    }

    private boolean applies(World world) {
        var list = plugin.getMainConfig().cycle.worlds;
        if (list == null || list.isEmpty()) return true;
        return list.contains(world.getName());
    }
}