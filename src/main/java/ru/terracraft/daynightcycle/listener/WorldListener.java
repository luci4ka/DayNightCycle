package ru.terracraft.daynightcycle.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import ru.terracraft.daynightcycle.DayNightCyclePlugin;

public class WorldListener implements Listener {

    private final DayNightCyclePlugin plugin;

    public WorldListener(DayNightCyclePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        plugin.syncWorld(event.getWorld(), false);
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        plugin.forgetWorld(event.getWorld());
    }
}