package ru.terracraft.daynightcycle.afk;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import ru.terracraft.daynightcycle.DayNightCyclePlugin;

import java.util.regex.Pattern;

public class AfkListener implements Listener {

    private static final Pattern AFK_CMD = Pattern.compile("^/(\\w+:)?afk(\\s.*)?$", Pattern.CASE_INSENSITIVE);

    private final DayNightCyclePlugin plugin;

    public AfkListener(DayNightCyclePlugin plugin) {
        this.plugin = plugin;
    }

    private static double angleDiff(double a, double b) {
        return Math.abs(((a - b + 540) % 360) - 180);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!plugin.getMainConfig().afk.resetOnMove) return;
        Location from = event.getFrom();
        Location to = event.getTo();

        if (plugin.getMainConfig().afk.smartMovement) {
            boolean sameBlock = from.getBlockX() == to.getBlockX()
                    && from.getBlockZ() == to.getBlockZ()
                    && Math.abs(from.getY() - to.getY()) < 0.5;
            boolean sameLook = angleDiff(from.getYaw(), to.getYaw()) < 1.0
                    && angleDiff(from.getPitch(), to.getPitch()) < 1.0;
            if (sameBlock && sameLook) return;
        } else if (from.equals(to)) {
            return;
        }
        plugin.getAfkManager().updateActivity(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleMove(VehicleMoveEvent event) {
        if (!plugin.getMainConfig().afk.resetOnMove) return;
        if (event.getVehicle().getPassengers().isEmpty()) return;
        for (var passenger : event.getVehicle().getPassengers()) {
            if (passenger instanceof org.bukkit.entity.Player p) {
                plugin.getAfkManager().updateActivity(p);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        if (!plugin.getMainConfig().afk.resetOnChat) return;
        plugin.getServer().getScheduler().runTask(plugin,
                () -> plugin.getAfkManager().updateActivity(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!plugin.getMainConfig().afk.resetOnChat) return;
        if (AFK_CMD.matcher(event.getMessage()).matches()) return;
        plugin.getAfkManager().updateActivity(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (!plugin.getMainConfig().afk.resetOnInteract) return;
        if (event.getAction() == Action.PHYSICAL) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        plugin.getAfkManager().updateActivity(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (!plugin.getMainConfig().afk.resetOnInteract) return;
        plugin.getAfkManager().updateActivity(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!plugin.getMainConfig().afk.resetOnInteract) return;
        if (event.getWhoClicked() instanceof org.bukkit.entity.Player p) {
            plugin.getAfkManager().updateActivity(p);
        }
    }
}