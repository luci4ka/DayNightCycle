package ru.terracraft.daynightcycle.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.terracraft.daynightcycle.DayNightCyclePlugin;
import ru.terracraft.daynightcycle.lang.MessageKey;

public class DncCommand {

    private final DayNightCyclePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public DncCommand(DayNightCyclePlugin plugin) {
        this.plugin = plugin;
    }

    public CommandAPICommand build() {
        CommandAPICommand root = new CommandAPICommand("daynightcycle")
                .withAliases("dnc")
                .executes((sender, _) -> {
                    if (!sender.hasPermission("dnc.command.status")) {
                        send(sender, MessageKey.ERROR_NO_PERMISSION);
                        return;
                    }
                    sendInfo(sender);
                });

        root.withSubcommand(new CommandAPICommand("info")
                .withPermission(CommandPermission.fromString("dnc.command.info"))
                .executes((CommandExecutor) (sender, _) -> sendInfo(sender)));

        root.withSubcommand(new CommandAPICommand("reload")
                .withPermission(CommandPermission.fromString("dnc.command.reload"))
                .executes((sender, _) -> {
                    String error = plugin.reloadAll();
                    if (error == null) {
                        send(sender, MessageKey.COMMAND_RELOAD_SUCCESS);
                    } else {
                        send(sender, MessageKey.COMMAND_RELOAD_ERROR, "%error%", miniMessage.escapeTags(error));
                    }
                }));

        // ─── /dnc time ───
        root.withSubcommand(new CommandAPICommand("time")
                .withPermission(CommandPermission.fromString("dnc.command.time"))
                .executes((sender, _) -> {
                    var lang = plugin.getLanguageManager();
                    Player p = asPlayer(sender);
                    if (p == null) {
                        for (World world : plugin.getServer().getWorlds()) {
                            lang.sendTime(sender, world);
                        }
                        return;
                    }
                    lang.sendTime(sender, p.getWorld());
                }));

        root.withSubcommand(new CommandAPICommand("status")
                .withPermission(CommandPermission.fromString("dnc.command.status"))
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

        root.withSubcommand(new CommandAPICommand("set")
                .withPermission(CommandPermission.fromString("dnc.command.set"))
                .withSubcommand(new CommandAPICommand("day")
                        .withArguments(new IntegerArgument("minutes", 1, 1440))
                        .executes((sender, args) -> {
                            int minutes = (int) args.get("minutes");
                            plugin.getMainConfig().cycle.dayMinutes = minutes;
                            plugin.getConfigLoader().saveConfig();
                            plugin.getTimeController().reload();
                            sendSet(sender);
                        }))
                .withSubcommand(new CommandAPICommand("night")
                        .withArguments(new IntegerArgument("minutes", 1, 1440))
                        .executes((sender, args) -> {
                            int minutes = (int) args.get("minutes");
                            plugin.getMainConfig().cycle.nightMinutes = minutes;
                            plugin.getConfigLoader().saveConfig();
                            plugin.getTimeController().reload();
                            sendSet(sender);
                        })));

        root.withSubcommand(new CommandAPICommand("sleeplist")
                .withPermission(CommandPermission.fromString("dnc.command.sleeplist.view"))
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
                            String name = (String) args.get("player");
                            String safeName = miniMessage.escapeTags(name);
                            if (!isValidName(name)) {
                                send(sender, MessageKey.SLEEPLIST_INVALID_NAME, "%player%", safeName);
                                return;
                            }
                            var list = plugin.getSleepList().players;
                            if (list.stream().noneMatch(n -> n.equalsIgnoreCase(name))) {
                                list.add(name);
                                plugin.getConfigLoader().saveSleepList();
                            }
                            send(sender, MessageKey.SLEEPLIST_ADDED, "%player%", safeName);
                        }))
                .withSubcommand(new CommandAPICommand("remove")
                        .withPermission(CommandPermission.fromString("dnc.command.sleeplist"))
                        .withArguments(new StringArgument("player"))
                        .executes((sender, args) -> {
                            String name = (String) args.get("player");
                            String safeName = miniMessage.escapeTags(name);
                            if (!isValidName(name)) {
                                send(sender, MessageKey.SLEEPLIST_INVALID_NAME, "%player%", safeName);
                                return;
                            }
                            var list = plugin.getSleepList().players;
                            boolean removed = list.removeIf(n -> n.equalsIgnoreCase(name));
                            if (removed) {
                                plugin.getConfigLoader().saveSleepList();
                                send(sender, MessageKey.SLEEPLIST_REMOVED, "%player%", safeName);
                            } else {
                                send(sender, MessageKey.SLEEPLIST_NOT_FOUND, "%player%", safeName);
                            }
                        })));

        return root;
    }

    // ═══════════════ /dtime ═══════════════

    /**
     * Отдельная команда /dtime — алиас для /dnc time.
     */
    public CommandAPICommand buildDtime() {
        return new CommandAPICommand("dtime")
                .withPermission(CommandPermission.fromString("dnc.command.time"))
                .executes((CommandExecutor) (sender, _) -> {
                    var lang = plugin.getLanguageManager();
                    Player p = asPlayer(sender);
                    if (p == null) {
                        for (World world : plugin.getServer().getWorlds()) {
                            lang.sendTime(sender, world);
                        }
                        return;
                    }
                    lang.sendTime(sender, p.getWorld());
                });
    }

    private boolean isValidName(String name) {
        return name != null && name.matches("^[A-Za-z0-9_.]{3,17}$");
    }

    private void sendInfo(CommandSender sender) {
        Player p = asPlayer(sender);
        var lang = plugin.getLanguageManager();

        String version = plugin.getPluginMeta().getVersion();
        String author = String.join(", ", plugin.getPluginMeta().getAuthors());
        String languages = String.join(", ", lang.getAvailableLanguages());

        String papiState = plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")
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

    private Player asPlayer(CommandSender sender) {
        return sender instanceof Player p ? p : null;
    }

    private void send(CommandSender sender, MessageKey key) {
        Player p = asPlayer(sender);
        var lang = plugin.getLanguageManager();
        sender.sendMessage(lang.render(lang.getMessage(p, key), p));
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