package ru.terracraft.daynightcycle.lang;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.World;
import org.bukkit.entity.Player;
import ru.terracraft.daynightcycle.DayNightCyclePlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LanguageManager {

    private static final String DEFAULT_LANGUAGE = "ru_ru";
    private static final String LANG_EXTENSION = ".lang";

    private final DayNightCyclePlugin plugin;
    private volatile Map<String, LangFile> languages = Map.of();
    private final Map<UUID, String> localeCache = new ConcurrentHashMap<>();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private volatile String currentLanguage;
    private LangFile bundledDefault;

    public LanguageManager(DayNightCyclePlugin plugin, String initialLanguage) {
        this.plugin = plugin;
        this.currentLanguage = initialLanguage != null ? initialLanguage : DEFAULT_LANGUAGE;
        loadBundledDefault();
        loadLanguages();
    }

    private void loadBundledDefault() {
        try (InputStream in = plugin.getResource("lang/" + DEFAULT_LANGUAGE + LANG_EXTENSION)) {
            if (in != null) {
                this.bundledDefault = LangFile.loadFrom(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load bundled default lang: " + e.getMessage());
        }
    }

    public void loadLanguages() {
        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists() && !langFolder.mkdirs()) {
            plugin.getLogger().warning("Failed to create lang/ folder");
        }

        copyDefaultLanguages(langFolder);

        Map<String, LangFile> loaded = new HashMap<>();
        File[] files = langFolder.listFiles((_, name) -> name.endsWith(LANG_EXTENSION));
        if (files != null) {
            for (File file : files) {
                String code = file.getName()
                        .substring(0, file.getName().length() - LANG_EXTENSION.length())
                        .toLowerCase(Locale.ROOT);
                try {
                    loaded.put(code, LangFile.load(file));
                    plugin.getLogger().info("Loaded language: " + code);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load " + file.getName() + ": " + e.getMessage());
                }
            }
        }

        String configured = plugin.getMainConfig().language;
        if (configured != null) {
            this.currentLanguage = configured.toLowerCase(Locale.ROOT);
        }
        if (!loaded.containsKey(currentLanguage)) {
            this.currentLanguage = DEFAULT_LANGUAGE;
        }

        this.languages = Map.copyOf(loaded);
        localeCache.clear();
    }

    private void copyDefaultLanguages(File langFolder) {
        String[] builtin = {"ru_ru", "en_us", "de_de", "zh_cn", "es_es", "kk_kz", "be_by"};
        for (String code : builtin) {
            File target = new File(langFolder, code + LANG_EXTENSION);
            if (target.exists()) continue;
            try {
                plugin.saveResource("lang/" + code + LANG_EXTENSION, false);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public void reload() {
        loadLanguages();
    }

    public String getPlayerLocale(Player player) {
        if (player == null) return currentLanguage;
        return localeCache.computeIfAbsent(player.getUniqueId(), _ -> {
            Locale locale = player.locale();
            String full = locale.toString().toLowerCase(Locale.ROOT);
            if (languages.containsKey(full)) return full;
            String lang = locale.getLanguage().toLowerCase(Locale.ROOT);
            String withCountry = lang + "_" + locale.getCountry().toLowerCase(Locale.ROOT);
            if (languages.containsKey(withCountry)) return withCountry;
            if (languages.containsKey(lang)) return lang;
            for (String code : languages.keySet()) {
                if (code.startsWith(lang + "_")) return code;
            }
            return languages.containsKey(currentLanguage) ? currentLanguage : DEFAULT_LANGUAGE;
        });
    }

    public void invalidateLocale(Player player) {
        if (player != null) localeCache.remove(player.getUniqueId());
    }

    public String getMessage(MessageKey key) {
        return getMessage(null, key);
    }

    public String getMessage(Player player, MessageKey key) {
        LangFile lang = langFor(player);
        String value = lang == null ? null : lang.getString(key.path());
        if (value == null && bundledDefault != null) {
            value = bundledDefault.getString(key.path());
        }
        return value != null ? value : "<red>Missing: " + key.path() + "</red>";
    }

    public Component render(String text, Player player) {
        if (text == null || text.isEmpty()) return Component.empty();
        String replaced = text;
        if (player != null) replaced = replaced.replace("%player%", player.getName());
        try {
            return miniMessage.deserialize(replaced);
        } catch (Exception e) {
            return Component.text(replaced);
        }
    }

    public Component renderKey(MessageKey key, Player player) {
        return render(getMessage(player, key), player);
    }

    private LangFile currentLang() {
        LangFile lang = languages.get(currentLanguage);
        if (lang == null) lang = languages.get(DEFAULT_LANGUAGE);
        return lang;
    }

    private LangFile langFor(Player player) {
        if (player == null) return currentLang();
        String code = getPlayerLocale(player);
        LangFile lang = languages.get(code);
        return lang != null ? lang : currentLang();
    }

    public void sendActionBar(Player player, MessageKey key) {
        if (player == null) return;
        player.sendActionBar(renderKey(key, player));
    }

    public void sendActionBar(Player player, MessageKey key, String placeholder, String value) {
        if (player == null) return;
        String raw = getMessage(player, key).replace(placeholder, value);
        player.sendActionBar(render(raw, player));
    }

    public Set<String> getAvailableLanguages() { return languages.keySet(); }

    public String worldLabel(Player player, World world) {
        String name = world.getName();
        MessageKey key = switch (name) {
            case "world", "overworld" -> MessageKey.COMMAND_TIME_WORLD_OVERWORLD;
            case "world_nether", "the_nether", "nether" -> MessageKey.COMMAND_TIME_WORLD_NETHER;
            case "world_the_end", "the_end", "end" -> MessageKey.COMMAND_TIME_WORLD_END;
            default -> MessageKey.COMMAND_TIME_WORLD_OTHER;
        };
        return getMessage(player, key).replace("%world%", name);
    }

    public void sendTime(org.bukkit.command.CommandSender sender, World world) {
        Player p = sender instanceof Player pl ? pl : null;
        long ticks = world.getTime();
        String time24 = ru.terracraft.daynightcycle.cycle.TimeUtil.toTime24(ticks);
        String worldName = worldLabel(p, world);

        sender.sendMessage(render(getMessage(p, MessageKey.COMMAND_TIME_HEADER), p));
        sender.sendMessage(render(getMessage(p, MessageKey.COMMAND_TIME_WORLD)
                .replace("%world%", worldName), p));
        sender.sendMessage(render(getMessage(p, MessageKey.COMMAND_TIME_DAY)
                .replace("%time%", time24)
                .replace("%ticks%", String.valueOf(ticks)), p));
        sender.sendMessage(render(getMessage(p, MessageKey.COMMAND_TIME_FOOTER), p));
    }
}