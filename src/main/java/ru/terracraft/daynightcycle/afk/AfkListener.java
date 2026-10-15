package ru.terracraft.daynightcycle.afk;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import ru.terracraft.daynightcycle.DayNightCyclePlugin;

public class AfkListener implements Listener {

    private final DayNightCyclePlugin plugin;

    public AfkListener(DayNightCyclePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!plugin.getMainConfig().afk.resetOnMove) return;
        Location from = event.getFrom();
        Location to = event.getTo();

        if (plugin.getMainConfig().afk.smartMovement) {
            if (from.getBlockX() == to.getBlockX()
                    && from.getBlockZ() == to.getBlockZ()
                    && Math.abs(from.getY() - to.getY()) < 0.5) {
                return;
            }
        } else if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) {
            return;
        }

        plugin.getAfkManager().updateActivity(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        if (!plugin.getMainConfig().afk.resetOnChat) return;
        // AsyncChatEvent асинхронный — переходим в главный поток для безопасности
        plugin.getServer().getScheduler().runTask(plugin,
                () -> plugin.getAfkManager().updateActivity(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (!plugin.getMainConfig().afk.resetOnInteract) return;
        plugin.getAfkManager().updateActivity(event.getPlayer());
    }
}