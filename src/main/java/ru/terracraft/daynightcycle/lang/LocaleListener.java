package ru.terracraft.daynightcycle.lang;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLocaleChangeEvent;
import ru.terracraft.daynightcycle.DayNightCyclePlugin;

public class LocaleListener implements Listener {

    private final DayNightCyclePlugin plugin;

    public LocaleListener(DayNightCyclePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLocaleChange(PlayerLocaleChangeEvent event) {
        plugin.getLanguageManager().invalidateLocale(event.getPlayer());
    }
}