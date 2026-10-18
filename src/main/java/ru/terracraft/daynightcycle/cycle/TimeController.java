package ru.terracraft.daynightcycle.cycle;

import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import ru.terracraft.daynightcycle.DayNightCyclePlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TimeController {

    private static final int PERIOD = 2;

    private final DayNightCyclePlugin plugin;
    private final Map<UUID, Double> remainder = new HashMap<>();
    private BukkitTask task;

    public TimeController(DayNightCyclePlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();
        if (!plugin.getMainConfig().cycle.enabled) return;
        remainder.clear();
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, PERIOD, PERIOD);
    }

    public void stop() {
        if (task != null) { task.cancel(); task = null; }
        remainder.clear();
    }

    public void reload() {
        start();
    }

    /** Очистка остатка при выгрузке мира. */
    public void forgetWorld(World world) {
        remainder.remove(world.getUID());
    }

    private void tick() {
        var cycle = plugin.getMainConfig().cycle;
        if (!cycle.enabled) return;

        double perRun = PERIOD / 20.0;
        double daySpeed   = 12000.0 / (cycle.dayMinutes * 60.0) * perRun;
        double nightSpeed = 12000.0 / (cycle.nightMinutes * 60.0) * perRun;

        for (World w : plugin.getServer().getWorlds()) {
            if (!applies(w)) continue;
            if (w.getEnvironment() != World.Environment.NORMAL) continue;
            if (plugin.getSleepManager() != null && plugin.getSleepManager().isSkipping(w)) continue;

            long time = w.getTime();
            double acc = remainder.getOrDefault(w.getUID(), 0.0)
                    + (time < 12000 ? daySpeed : nightSpeed);
            long step = (long) acc;
            remainder.put(w.getUID(), acc - step);

            if (step > 0) {
                w.setTime((time + step) % 24000L);
            }
        }
    }

    private boolean applies(World world) {
        var list = plugin.getMainConfig().cycle.worlds;
        if (list == null || list.isEmpty()) return true;
        return list.contains(world.getName());
    }
}