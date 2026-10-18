package ru.terracraft.daynightcycle.sleep;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.terracraft.daynightcycle.DayNightCyclePlugin;

public class SleepListener implements Listener {

    private final DayNightCyclePlugin plugin;

    public SleepListener(DayNightCyclePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        if (event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) return;
        plugin.getServer().getScheduler().runTaskLater(plugin,
                () -> plugin.getSleepManager().checkSleep(event.getPlayer().getWorld()), 1L);
    }

    @EventHandler
    public void onBedLeave(PlayerBedLeaveEvent event) {
        plugin.getServer().getScheduler().runTaskLater(plugin,
                () -> plugin.getSleepManager().checkSleep(event.getPlayer().getWorld()), 1L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        World world = p.getWorld();
        plugin.getAfkManager().onQuit(p);
        plugin.getLanguageManager().invalidateLocale(p);
        plugin.getServer().getScheduler().runTaskLater(plugin,
                () -> plugin.getSleepManager().checkSleep(world), 1L);
    }


    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        World from = event.getFrom();
        World to = event.getPlayer().getWorld();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getSleepManager().checkSleep(from);
            plugin.getSleepManager().checkSleep(to);
        }, 1L);
    }
}