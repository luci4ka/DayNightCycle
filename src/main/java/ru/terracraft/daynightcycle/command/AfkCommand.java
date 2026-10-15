package ru.terracraft.daynightcycle.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import ru.terracraft.daynightcycle.DayNightCyclePlugin;

public class AfkCommand {

    private final DayNightCyclePlugin plugin;

    public AfkCommand(DayNightCyclePlugin plugin) {
        this.plugin = plugin;
    }

    public CommandAPICommand build() {
        return new CommandAPICommand("afk")
                .withPermission(CommandPermission.fromString("dnc.afk"))
                .executesPlayer((player, args) -> {
                    plugin.getAfkManager().toggleAfk(player);
                });
    }
}