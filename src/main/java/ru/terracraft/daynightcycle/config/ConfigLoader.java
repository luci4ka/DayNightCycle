package ru.terracraft.daynightcycle.config;

import de.exlll.configlib.ConfigLib;
import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import ru.terracraft.daynightcycle.DayNightCyclePlugin;

import java.nio.file.Path;

/**
 * Загрузчик конфигов DayNightCycle через ConfigLib.
 * <p>
 * Использует {@link YamlConfigurations#update(Path, Class, YamlConfigurationProperties)},
 * который:
 * <ul>
 *     <li>создаёт файл, если его нет;</li>
 *     <li>дописывает новые поля, если структура POJO изменилась;</li>
 *     <li>сохраняет комментарии из {@code @Comment}.</li>
 * </ul>
 * Также выполняет валидацию значений после загрузки.
 */
public class ConfigLoader {

    private static final String HEADER = """
            ═══════════════════════════════════════
             DayNightCycle — Конфигурация
            ═══════════════════════════════════════
            """;

    private final DayNightCyclePlugin plugin;
    private final Path configPath;
    private final Path sleepListPath;

    private MainConfig config;
    private SleepListConfig sleepList;

    public ConfigLoader(DayNightCyclePlugin plugin) {
        this.plugin = plugin;
        this.configPath = plugin.getDataFolder().toPath().resolve("config.yml");
        this.sleepListPath = plugin.getDataFolder().toPath().resolve("sleeplist.yml");
    }

    /**
     * Загружает (или создаёт) config.yml и sleeplist.yml,
     * затем валидирует значения.
     */
    public void load() {
        YamlConfigurationProperties props = buildProperties();
        this.config = YamlConfigurations.update(configPath, MainConfig.class, props);
        this.sleepList = YamlConfigurations.update(sleepListPath, SleepListConfig.class, props);
        validate();
    }

    /**
     * Сохраняет текущие значения в файлы.
     */
    public void save() {
        if (config == null || sleepList == null) {
            plugin.getLogger().warning("Нельзя сохранить конфиги — они не загружены");
            return;
        }
        YamlConfigurationProperties props = buildProperties();
        YamlConfigurations.save(configPath, MainConfig.class, config, props);
        YamlConfigurations.save(sleepListPath, SleepListConfig.class, sleepList, props);
    }

    // ═══════════════ VALIDATION ═══════════════

    /**
     * Проверяет и корректирует значения конфига.
     * Вызывается автоматически после {@link #load()}.
     */
    private void validate() {
        if (config.cycle.dayMinutes < 1) {
            plugin.getLogger().warning("cycle.day-minutes < 1 — ставлю 1");
            config.cycle.dayMinutes = 1;
        }
        if (config.cycle.nightMinutes < 1) {
            plugin.getLogger().warning("cycle.night-minutes < 1 — ставлю 1");
            config.cycle.nightMinutes = 1;
        }
        if (config.afk.timeoutMinutes < 1) {
            plugin.getLogger().warning("afk.timeout-minutes < 1 — ставлю 1");
            config.afk.timeoutMinutes = 1;
        }
        if (config.sleep.percent < 0.0) {
            plugin.getLogger().warning("sleep.percent < 0.0 — ставлю 0.0");
            config.sleep.percent = 0.0;
        }
        if (config.sleep.percent > 1.0) {
            plugin.getLogger().warning("sleep.percent > 1.0 — ставлю 1.0");
            config.sleep.percent = 1.0;
        }
    }

    // ═══════════════ GETTERS ═══════════════

    public MainConfig getConfig() {
        if (config == null) {
            throw new IllegalStateException("Конфиг не загружен — вызовите load()");
        }
        return config;
    }

    public SleepListConfig getSleepList() {
        if (sleepList == null) {
            throw new IllegalStateException("sleeplist.yml не загружен — вызовите load()");
        }
        return sleepList;
    }

    // ═══════════════ INTERNAL ═══════════════

    private YamlConfigurationProperties buildProperties() {
        return ConfigLib.BUKKIT_DEFAULT_PROPERTIES.toBuilder()
                .header(HEADER)
                .build();
    }
}