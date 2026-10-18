package ru.terracraft.daynightcycle.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.daynightcycle.DayNightCyclePlugin;
import ru.terracraft.daynightcycle.cycle.TimeUtil;
import ru.terracraft.daynightcycle.lang.MessageKey;

public class DncExpansion extends PlaceholderExpansion {

    private final DayNightCyclePlugin plugin;

    public DncExpansion(DayNightCyclePlugin plugin) {
        this.plugin = plugin;
    }

    @Override public @NotNull String getIdentifier() { return "dnc"; }
    @Override public @NotNull String getAuthor() { return "TerraCraft"; }
    @Override public @NotNull String getVersion() { return plugin.getPluginMeta().getVersion(); }
    @Override public boolean persist() { return true; }

    @Override
    public String onRequest(OfflinePlayer offline, @NotNull String params) {
        if (offline == null || !offline.isOnline()) return "";
        Player player = offline.getPlayer();
        if (player == null) return "";

        if (params.equalsIgnoreCase("afk")) {
            return plugin.getAfkManager().isAfk(player)
                    ? plugin.getMainConfig().afk.display.text : "";
        }
        if (params.equalsIgnoreCase("afk_prefix")) {
            return plugin.getAfkManager().isAfk(player)
                    ? plugin.getMainConfig().afk.display.text + " " : "";
        }

        World world = player.getWorld();
        long ticks = world.getTime();

        if (params.equalsIgnoreCase("time")) {
            return TimeUtil.toTime24(ticks);
        }
        if (params.equalsIgnoreCase("ticks")) {
            return String.valueOf(ticks);
        }

        if (params.equalsIgnoreCase("world")) {
            Component component = plugin.getLanguageManager().render(
                    plugin.getLanguageManager().worldLabel(player, world), player);
            return LegacyComponentSerializer.legacySection().serialize(component);
        }

        return null;
    }
}