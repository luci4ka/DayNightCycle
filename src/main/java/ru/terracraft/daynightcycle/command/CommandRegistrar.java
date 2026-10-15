package ru.terracraft.daynightcycle.command;

import dev.jorel.commandapi.CommandAPICommand;
import ru.terracraft.daynightcycle.DayNightCyclePlugin;

public class CommandRegistrar {

    private final DayNightCyclePlugin plugin;

    public CommandRegistrar(DayNightCyclePlugin plugin) {
        this.plugin = plugin;
    }

    public void registerAll() {
        // /dnc
        new DncCommand(plugin).build().register();

        // /afk
        new AfkCommand(plugin).build().register();

        // /time — перекрывает ванильную
        new TimeCommand(plugin).build().register();

        plugin.getLogger().info("Зарегистрированы команды: /dnc, /afk, /time");
    }
}