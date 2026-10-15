package ru.terracraft.daynightcycle.lang;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import ru.terracraft.daynightcycle.DayNightCyclePlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class LanguageManager {

    private static final String DEFAULT_LANGUAGE = "ru_ru"; // fallback, если язык игрока не найден
    private static final String LANG_EXTENSION = ".lang";

    private final DayNightCyclePlugin plugin;
    private final Map<String, LangFile> languages = new HashMap<>();
    private final Map<UUID, String> localeCache = new HashMap<>();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private String currentLanguage;

    public LanguageManager(DayNightCyclePlugin plugin, String initialLanguage) {
        this.plugin = plugin;
        this.currentLanguage = initialLanguage != null ? initialLanguage : DEFAULT_LANGUAGE;
        loadLanguages();
    }

    // ═══════════════ LOADING ═══════════════

    public void loadLanguages() {
        languages.clear();
        localeCache.clear();

        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists() && !langFolder.mkdirs()) {
            plugin.getLogger().warning("Не удалось создать папку lang/");
        }

        plugin.saveResource("lang/ru_ru.lang", false);
        plugin.saveResource("lang/en_us.lang", false);

        File[] files = langFolder.listFiles((_, name) -> name.endsWith(LANG_EXTENSION));
        if (files == null) return;

        for (File file : files) {
            String code = file.getName()
                    .substring(0, file.getName().length() - LANG_EXTENSION.length())
                    .toLowerCase();
            try {
                languages.put(code, LangFile.load(file));
                plugin.getLogger().info("Загружен язык: " + code);
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка загрузки " + file.getName() + ": " + e.getMessage());
            }
        }

        if (!languages.containsKey(currentLanguage)) {
            currentLanguage = DEFAULT_LANGUAGE;
        }
    }

    // ═══════════════ PLAYER LOCALE ═══════════════

    /**
     * Возвращает код языка для игрока с учётом fallback.
     * Кэшируется, чтобы не дёргать player.locale() постоянно.
     */
    public String getPlayerLocale(Player player) {
        if (player == null) return currentLanguage;

        return localeCache.computeIfAbsent(player.getUniqueId(), _ -> {
            Locale locale = player.locale();

            // 1. Полный код: ru_ru, en_us
            String full = locale.toString().toLowerCase();
            if (languages.containsKey(full)) return full;

            // 2. Язык + страна: ru_ru
            String lang = locale.getLanguage().toLowerCase();
            String withCountry = lang + "_" + locale.getCountry().toLowerCase();
            if (languages.containsKey(withCountry)) return withCountry;

            // 3. Только язык: ru
            if (languages.containsKey(lang)) return lang;

            // 4. Поиск по префиксу: ru -> ru_ru, en -> en_us
            for (String code : languages.keySet()) {
                if (code.startsWith(lang + "_")) return code;
            }

            // 5. Fallback
            return languages.containsKey(currentLanguage) ? currentLanguage : DEFAULT_LANGUAGE;
        });
    }

    /** Сбросить кэш локали для игрока (например, при смене языка в клиенте). */
    public void invalidateLocale(Player player) {
        if (player != null) localeCache.remove(player.getUniqueId());
    }

    // ═══════════════ GET MESSAGE ═══════════════

    public String getMessage(MessageKey key) {
        return getMessage(null, key);
    }

    public String getMessage(Player player, MessageKey key) {
        LangFile lang = langFor(player);
        if (lang == null) return "<red>Missing: " + key.path() + "</red>";
        String value = lang.getString(key.path());
        return value != null ? value : "<red>Missing: " + key.path() + "</red>";
    }

    // ═══════════════ RENDER ═══════════════

    public Component render(String text, Player player) {
        if (text == null || text.isEmpty()) return Component.empty();

        String replaced = text;
        if (player != null) {
            replaced = replaced.replace("%player%", player.getName());
        }

        try {
            return miniMessage.deserialize(LangFile.unescape(replaced));
        } catch (Exception e) {
            return Component.text(replaced);
        }
    }

    /** Удобный метод: взять сообщение по ключу и сразу отрендерить. */
    public Component renderKey(MessageKey key, Player player) {
        return render(getMessage(player, key), player);
    }

    // ═══════════════ INTERNAL ═══════════════

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

    // ═══════════════ ACTION BAR ═══════════════

    /** Отправить сообщение в action bar игрока. */
    public void sendActionBar(Player player, MessageKey key) {
        if (player == null) return;
        player.sendActionBar(renderKey(key, player));
    }

    /** Отправить сообщение в action bar с заменой placeholder'ов. */
    public void sendActionBar(Player player, MessageKey key, String placeholder, String value) {
        if (player == null) return;
        String raw = getMessage(player, key).replace(placeholder, value);
        player.sendActionBar(render(raw, player));
    }

    // ═══════════════ GETTERS / SETTERS ═══════════════

    public Set<String> getAvailableLanguages() { return languages.keySet(); }

}