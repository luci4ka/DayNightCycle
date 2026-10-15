package ru.terracraft.daynightcycle.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.terracraft.daynightcycle.DayNightCyclePlugin;
import ru.terracraft.daynightcycle.lang.MessageKey;

public class DncCommand {

    private final DayNightCyclePlugin plugin;

    public DncCommand(DayNightCyclePlugin plugin) {
        this.plugin = plugin;
    }

    public CommandAPICommand build() {
        CommandAPICommand root = new CommandAPICommand("daynightcycle")
                .withAliases("dnc")
                .executes((CommandExecutor) (sender, _) -> sendInfo(sender));

        // ─── /dnc info ───
        root.withSubcommand(new CommandAPICommand("info")
                .executes((CommandExecutor) (sender, _) -> sendInfo(sender)));

        // ─── /dnc reload ───
        root.withSubcommand(new CommandAPICommand("reload")
                .withPermission(CommandPermission.fromString("dnc.command.reload"))
                .executes((sender, _) -> {
                    try {
                        plugin.reloadAll();
                        send(sender);
                    } catch (Exception e) {
                        send(sender, MessageKey.COMMAND_RELOAD_ERROR, "%error%", e.getMessage());
                    }
                }));

        // ─── /dnc status ───
        root.withSubcommand(new CommandAPICommand("status")
                .executes((sender, _) -> {
                    Player p = asPlayer(sender);
                    var lang = plugin.getLanguageManager();
                    var cfg = plugin.getMainConfig();

                    sender.sendMessage(lang.render(lang.getMessage(p, MessageKey.COMMAND_STATUS_HEADER), p));
                    sender.sendMessage(lang.render(lang.getMessage(p, MessageKey.COMMAND_STATUS_DAY)
                            .replace("%day%", String.valueOf(cfg.cycle.dayMinutes)), p));
                    sender.sendMessage(lang.render(lang.getMessage(p, MessageKey.COMMAND_STATUS_NIGHT)
                            .replace("%night%", String.valueOf(cfg.cycle.nightMinutes)), p));
                    sender.sendMessage(lang.render(lang.getMessage(p, MessageKey.COMMAND_STATUS_AFK_TIMEOUT)
                            .replace("%timeout%", String.valueOf(cfg.afk.timeoutMinutes)), p));
                    sender.sendMessage(lang.render(lang.getMessage(p, MessageKey.COMMAND_STATUS_AFK_PLAYERS)
                            .replace("%count%", String.valueOf(plugin.getAfkManager().countAfk())), p));
                    sender.sendMessage(lang.render(lang.getMessage(p, MessageKey.COMMAND_STATUS_FOOTER), p));
                }));

        // ─── /dnc set day|night <min> ───
        root.withSubcommand(new CommandAPICommand("set")
                .withPermission(CommandPermission.fromString("dnc.command.set"))
                .withSubcommand(new CommandAPICommand("day")
                        .withArguments(new IntegerArgument("minutes", 1))
                        .executes((sender, args) -> {
                            Object raw = args.get("minutes");
                            plugin.getMainConfig().cycle.dayMinutes = raw instanceof Number n ? n.intValue() : 1;
                            plugin.saveConfigs();
                            plugin.getTimeController().reload();
                            sendSet(sender);
                        }))
                .withSubcommand(new CommandAPICommand("night")
                        .withArguments(new IntegerArgument("minutes", 1))
                        .executes((sender, args) -> {
                            Object raw = args.get("minutes");
                            plugin.getMainConfig().cycle.nightMinutes = raw instanceof Number n ? n.intValue() : 1; // ← FIX
                            plugin.saveConfigs();
                            plugin.getTimeController().reload();
                            sendSet(sender);
                        })));

        // ─── /dnc sleeplist ───
        root.withSubcommand(new CommandAPICommand("sleeplist")
                .executes((sender, _) -> {
                    Player p = asPlayer(sender);
                    var lang = plugin.getLanguageManager();
                    var list = plugin.getSleepList().players;
                    sender.sendMessage(lang.render(lang.getMessage(p, MessageKey.SLEEPLIST_HEADER)
                            .replace("%count%", String.valueOf(list.size())), p));
                    for (String name : list) {
                        sender.sendMessage(lang.render(lang.getMessage(p, MessageKey.SLEEPLIST_ENTRY)
                                .replace("%player%", name), p));
                    }
                })
                .withSubcommand(new CommandAPICommand("add")
                        .withPermission(CommandPermission.fromString("dnc.command.sleeplist"))
                        .withArguments(new StringArgument("player"))
                        .executes((sender, args) -> {
                            Object raw = args.get("player");
                            String name = raw instanceof String s ? s : "";
                            if (name.isEmpty()) {
                                send(sender, MessageKey.SLEEPLIST_NOT_FOUND, "%player%", "?");
                                return;
                            }
                            var list = plugin.getSleepList().players;
                            if (list.stream().noneMatch(n -> n.equalsIgnoreCase(name))) {
                                list.add(name);
                                plugin.saveConfigs();
                            }
                            send(sender, MessageKey.SLEEPLIST_ADDED, "%player%", name);
                        }))
                .withSubcommand(new CommandAPICommand("remove")
                        .withPermission(CommandPermission.fromString("dnc.command.sleeplist"))
                        .withArguments(new StringArgument("player"))
                        .executes((sender, args) -> {
                            Object raw = args.get("player");
                            String name = raw instanceof String s ? s : "";
                            if (name.isEmpty()) {
                                send(sender, MessageKey.SLEEPLIST_NOT_FOUND, "%player%", "?");
                                return;
                            }
                            var list = plugin.getSleepList().players;
                            boolean removed = list.removeIf(n -> n.equalsIgnoreCase(name));
                            if (removed) {
                                plugin.saveConfigs();
                                send(sender, MessageKey.SLEEPLIST_REMOVED, "%player%", name);
                            } else {
                                send(sender, MessageKey.SLEEPLIST_NOT_FOUND, "%player%", name);
                            }
                        })));

        return root;
    }

    // ═══════════════ INFO ═══════════════

    private void sendInfo(CommandSender sender) {
        Player p = asPlayer(sender);
        var lang = plugin.getLanguageManager();

        String version = plugin.getPluginMeta().getVersion();
        String author = String.join(", ", plugin.getPluginMeta().getAuthors());
        String languages = String.join(", ", lang.getAvailableLanguages());

        String papiState = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null
                ? lang.getMessage(p, MessageKey.STATE_ENABLED)
                : lang.getMessage(p, MessageKey.STATE_DISABLED);

        String bstatsState = plugin.getMetrics() != null
                ? lang.getMessage(p, MessageKey.STATE_ENABLED)
                : lang.getMessage(p, MessageKey.STATE_DISABLED);

        sender.sendMessage(lang.render(lang.getMessage(p, MessageKey.COMMAND_INFO_HEADER), p));
        sender.sendMessage(lang.render(lang.getMessage(p, MessageKey.COMMAND_INFO_VERSION)
                .replace("%version%", version), p));
        sender.sendMessage(lang.render(lang.getMessage(p, MessageKey.COMMAND_INFO_AUTHOR)
                .replace("%author%", author), p));
        sender.sendMessage(lang.render(lang.getMessage(p, MessageKey.COMMAND_INFO_LANGUAGES)
                .replace("%languages%", languages), p));
        sender.sendMessage(lang.render(lang.getMessage(p, MessageKey.COMMAND_INFO_PAPI)
                .replace("%state%", papiState), p));
        sender.sendMessage(lang.render(lang.getMessage(p, MessageKey.COMMAND_INFO_BSTATS)
                .replace("%state%", bstatsState), p));
        sender.sendMessage(lang.render(lang.getMessage(p, MessageKey.COMMAND_INFO_FOOTER), p));
    }

    // ═══════════════ HELPERS ═══════════════

    private Player asPlayer(CommandSender sender) {
        return sender instanceof Player p ? p : null;
    }

    private void send(CommandSender sender) {
        Player p = asPlayer(sender);
        var lang = plugin.getLanguageManager();
        sender.sendMessage(lang.render(lang.getMessage(p, MessageKey.COMMAND_RELOAD_SUCCESS), p));
    }

    private void send(CommandSender sender, MessageKey key, String placeholder, String value) {
        Player p = asPlayer(sender);
        var lang = plugin.getLanguageManager();
        String msg = lang.getMessage(p, key).replace(placeholder, value);
        sender.sendMessage(lang.render(msg, p));
    }

    private void sendSet(CommandSender sender) {
        Player p = asPlayer(sender);
        var lang = plugin.getLanguageManager();
        var cfg = plugin.getMainConfig();
        sender.sendMessage(lang.render(lang.getMessage(p, MessageKey.CYCLE_SET_SUCCESS)
                .replace("%day%", String.valueOf(cfg.cycle.dayMinutes))
                .replace("%night%", String.valueOf(cfg.cycle.nightMinutes)), p));
    }
}