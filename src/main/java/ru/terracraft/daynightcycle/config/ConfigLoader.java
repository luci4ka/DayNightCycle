package ru.terracraft.daynightcycle.config;

import de.exlll.configlib.ConfigLib;
import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import ru.terracraft.daynightcycle.DayNightCyclePlugin;

import java.nio.file.Path;

public class ConfigLoader {

    private static final String HEADER = """
            ═══════════════════════════════════════
             DayNightCycle — Configuration
            ═══════════════════════════════════════
            """;

    private static final String SLEEPLIST_HEADER = """
            ═══════════════════════════════════════
             DayNightCycle — Sleep Override
            ═══════════════════════════════════════
            """;

    private final DayNightCyclePlugin plugin;
    private final Path configPath;
    private final Path sleepListPath;

    private volatile MainConfig config;
    private volatile SleepListConfig sleepList;

    public ConfigLoader(DayNightCyclePlugin plugin) {
        this.plugin = plugin;
        this.configPath = plugin.getDataFolder().toPath().resolve("config.yml");
        this.sleepListPath = plugin.getDataFolder().toPath().resolve("sleeplist.yml");
    }

    public void load() {
        var configProps = ConfigLib.BUKKIT_DEFAULT_PROPERTIES.toBuilder().header(HEADER).build();
        var sleepListProps = ConfigLib.BUKKIT_DEFAULT_PROPERTIES.toBuilder().header(SLEEPLIST_HEADER).build();

        // Атомарная загрузка: сначала в локальные переменные
        MainConfig newConfig = YamlConfigurations.update(configPath, MainConfig.class, configProps);
        SleepListConfig newSleepList = YamlConfigurations.update(sleepListPath, SleepListConfig.class, sleepListProps);

        validate(newConfig);

        this.config = newConfig;
        this.sleepList = newSleepList;
    }

    /**
     * Сохраняет config.yml. Вызывается ТОЛЬКО когда плагин меняет конфиг
     * (например, через /dnc set). При onDisable НЕ вызывается, чтобы не затереть
     * ручные правки админа.
     */
    public void saveConfig() {
        MainConfig cfg = this.config;
        if (cfg == null) return;
        var props = ConfigLib.BUKKIT_DEFAULT_PROPERTIES.toBuilder().header(HEADER).build();
        YamlConfigurations.save(configPath, MainConfig.class, cfg, props);
    }

    /**
     * Сохраняет sleeplist.yml. Вызывается при изменении списка
     * (/dnc sleeplist add/remove).
     */
    public void saveSleepList() {
        SleepListConfig list = this.sleepList;
        if (list == null) return;
        var props = ConfigLib.BUKKIT_DEFAULT_PROPERTIES.toBuilder().header(SLEEPLIST_HEADER).build();
        YamlConfigurations.save(sleepListPath, SleepListConfig.class, list, props);
    }

    private void validate(MainConfig cfg) {
        if (cfg.cycle.dayMinutes < 1) {
            plugin.getLogger().warning("cycle.dayMinutes < 1 — set to 1");
            cfg.cycle.dayMinutes = 1;
        }
        if (cfg.cycle.nightMinutes < 1) {
            plugin.getLogger().warning("cycle.nightMinutes < 1 — set to 1");
            cfg.cycle.nightMinutes = 1;
        }
        if (cfg.afk.timeoutMinutes < 1) {
            plugin.getLogger().warning("afk.timeoutMinutes < 1 — set to 1");
            cfg.afk.timeoutMinutes = 1;
        }
        if (Double.isNaN(cfg.sleep.percent)) {
            plugin.getLogger().warning("sleep.percent is NaN — set to 0.5");
            cfg.sleep.percent = 0.5;
        }
        if (cfg.sleep.percent < 0.0) {
            plugin.getLogger().warning("sleep.percent < 0.0 — set to 0.0");
            cfg.sleep.percent = 0.0;
        }
        if (cfg.sleep.percent > 1.0) {
            plugin.getLogger().warning("sleep.percent > 1.0 — set to 1.0 (was " + cfg.sleep.percent + ")");
            cfg.sleep.percent = 1.0;
        }
        if (cfg.language == null || cfg.language.isBlank()) {
            plugin.getLogger().warning("language is empty — set to ru_ru");
            cfg.language = "ru_ru";
        }
        if (cfg.sleep.animation.durationSeconds < 0) {
            plugin.getLogger().warning("sleep.animation.durationSeconds < 0 — set to 0");
            cfg.sleep.animation.durationSeconds = 0;
        }
    }

    public MainConfig getConfig() {
        MainConfig cfg = this.config;
        if (cfg == null) throw new IllegalStateException("Config not loaded");
        return cfg;
    }

    public SleepListConfig getSleepList() {
        SleepListConfig list = this.sleepList;
        if (list == null) throw new IllegalStateException("SleepList not loaded");
        return list;
    }
}