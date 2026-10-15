package ru.terracraft.daynightcycle.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.daynightcycle.DayNightCyclePlugin;

public class AfkExpansion extends PlaceholderExpansion {

    private final DayNightCyclePlugin plugin;

    public AfkExpansion(DayNightCyclePlugin plugin) {
        this.plugin = plugin;
    }

    @Override public @NotNull String getIdentifier() { return "dnc"; }
    @Override public @NotNull String getAuthor() { return "TerraCraft"; }
    @Override public @NotNull String getVersion() { return plugin.getPluginMeta().getVersion(); }
    @Override public boolean persist() { return true; }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";
        if (params.equalsIgnoreCase("afk")) {
            return plugin.getAfkManager().isAfk(player)
                    ? plugin.getMainConfig().afk.display.text
                    : "";
        }
        if (params.equalsIgnoreCase("afk_prefix")) {
            return plugin.getAfkManager().isAfk(player)
                    ? plugin.getMainConfig().afk.display.text + " "
                    : "";
        }
        return null;
    }
}