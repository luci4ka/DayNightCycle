package ru.terracraft.daynightcycle.command;

import dev.jorel.commandapi.CommandAPICommand;
import ru.terracraft.daynightcycle.DayNightCyclePlugin;

public class CommandRegistrar {

    private final DayNightCyclePlugin plugin;

    public CommandRegistrar(DayNightCyclePlugin plugin) {
        this.plugin = plugin;
    }

    public void registerAll() {
        DncCommand dnc = new DncCommand(plugin);
        dnc.build().register();
        dnc.buildDtime().register();   // ← /dtime
        new AfkCommand(plugin).build().register();
        plugin.getLogger().info("Зарегистрированы команды: /dnc, /afk, /dtime");
    }
}