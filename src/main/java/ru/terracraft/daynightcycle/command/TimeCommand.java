package ru.terracraft.daynightcycle.command;

import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.terracraft.daynightcycle.DayNightCyclePlugin;
import ru.terracraft.daynightcycle.lang.MessageKey;

public class TimeCommand {

    private final DayNightCyclePlugin plugin;

    public TimeCommand(DayNightCyclePlugin plugin) {
        this.plugin = plugin;
    }

    public CommandAPICommand build() {
        return new CommandAPICommand("time")
                // /time
                .executes((sender, _) -> {
                    Player p = asPlayer(sender);
                    if (p == null) {
                        for (World world : plugin.getServer().getWorlds()) {
                            sendTime(sender, world, Format.TIME);
                        }
                        return;
                    }
                    sendTime(sender, p.getWorld(), Format.TIME);
                })

                // /time ticks
                .withSubcommand(new CommandAPICommand("ticks")
                        .executes((sender, _) -> {
                            Player p = asPlayer(sender);
                            if (p == null) {
                                for (World world : plugin.getServer().getWorlds()) {
                                    sendTime(sender, world, Format.TICKS);
                                }
                                return;
                            }
                            sendTime(sender, p.getWorld(), Format.TICKS);
                        }))

                // /time phase
                .withSubcommand(new CommandAPICommand("phase")
                        .executes((sender, _) -> {
                            Player p = asPlayer(sender);
                            if (p == null) {
                                for (World world : plugin.getServer().getWorlds()) {
                                    sendTime(sender, world, Format.PHASE);
                                }
                                return;
                            }
                            sendTime(sender, p.getWorld(), Format.PHASE);
                        }));
    }

    // ═══════════════ TIME ═══════════════

    private void sendTime(CommandSender sender, World world, Format format) {
        Player p = asPlayer(sender);
        var lang = plugin.getLanguageManager();

        long ticks = world.getTime();
        String time24 = ticksToTime24(ticks);
        String phase = getPhase(p, ticks);
        String worldName = colorizeWorld(p, world);

        sender.sendMessage(lang.render(lang.getMessage(p, MessageKey.COMMAND_TIME_HEADER), p));
        sender.sendMessage(lang.render(lang.getMessage(p, MessageKey.COMMAND_TIME_WORLD)
                .replace("%world%", worldName), p));

        switch (format) {
            case TIME -> sender.sendMessage(lang.render(
                    lang.getMessage(p, MessageKey.COMMAND_TIME_DAY)
                            .replace("%time%", time24)
                            .replace("%phase%", phase), p));

            case TICKS -> sender.sendMessage(lang.render(
                    lang.getMessage(p, MessageKey.COMMAND_TIME_DAY)
                            .replace("%time%", String.valueOf(ticks))
                            .replace("%phase%", phase), p));

            case PHASE -> sender.sendMessage(lang.render(
                    lang.getMessage(p, MessageKey.COMMAND_TIME_PHASE)
                            .replace("%phase%", phase), p));
        }

        sender.sendMessage(lang.render(lang.getMessage(p, MessageKey.COMMAND_TIME_FOOTER), p));
    }

    // ═══════════════ WORLD COLOR ═══════════════

    /**
     * Возвращает локализованное имя мира с цветом.
     * overworld = зелёный, nether = красный, end = фиолетовый, остальные = серый.
     */
    private String colorizeWorld(Player player, World world) {
        var lang = plugin.getLanguageManager();
        String name = world.getName();

        MessageKey key = switch (name) {
            case "world", "overworld" -> MessageKey.COMMAND_TIME_WORLD_OVERWORLD;
            case "world_nether", "the_nether", "nether" -> MessageKey.COMMAND_TIME_WORLD_NETHER;
            case "world_the_end", "the_end", "end" -> MessageKey.COMMAND_TIME_WORLD_END;
            default -> MessageKey.COMMAND_TIME_WORLD_OTHER;
        };

        return lang.getMessage(player, key).replace("%world%", name);
    }

    // ═══════════════ TIME HELPERS ═══════════════

    private String ticksToTime24(long ticks) {
        long hours = ((ticks / 1000) + 6) % 24;
        long minutes = (ticks % 1000) * 60 / 1000;
        return String.format("%02d:%02d", hours, minutes);
    }

    private String getPhase(Player player, long ticks) {
        var lang = plugin.getLanguageManager();

        if (ticks < 12000) return lang.getMessage(player, MessageKey.COMMAND_TIME_PHASE_DAY);
        if (ticks < 13000) return lang.getMessage(player, MessageKey.COMMAND_TIME_PHASE_SUNSET);
        if (ticks < 23000) return lang.getMessage(player, MessageKey.COMMAND_TIME_PHASE_NIGHT);
        return lang.getMessage(player, MessageKey.COMMAND_TIME_PHASE_SUNRISE);
    }

    private Player asPlayer(CommandSender sender) {
        return sender instanceof Player p ? p : null;
    }

    private enum Format {
        TIME, TICKS, PHASE
    }
}