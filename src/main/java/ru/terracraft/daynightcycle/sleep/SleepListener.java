package ru.terracraft.daynightcycle.sleep;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.terracraft.daynightcycle.DayNightCyclePlugin;

public class SleepListener implements Listener {

    private final DayNightCyclePlugin plugin;

    public SleepListener(DayNightCyclePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        // Проверим через тик, когда игрок реально уснёт
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
        plugin.getAfkManager().onQuit(event.getPlayer());
    }
}