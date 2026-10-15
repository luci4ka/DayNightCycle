package ru.terracraft.daynightcycle;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.GameRules;
import org.bukkit.plugin.java.JavaPlugin;
import ru.terracraft.daynightcycle.afk.AfkListener;
import ru.terracraft.daynightcycle.afk.AfkManager;
import ru.terracraft.daynightcycle.command.CommandRegistrar;
import ru.terracraft.daynightcycle.config.ConfigLoader;
import ru.terracraft.daynightcycle.config.MainConfig;
import ru.terracraft.daynightcycle.config.SleepListConfig;
import ru.terracraft.daynightcycle.cycle.TimeController;
import ru.terracraft.daynightcycle.lang.LanguageManager;
import ru.terracraft.daynightcycle.lang.LocaleListener;
import ru.terracraft.daynightcycle.placeholder.AfkExpansion;
import ru.terracraft.daynightcycle.sleep.SleepListener;
import ru.terracraft.daynightcycle.sleep.SleepManager;

public final class DayNightCyclePlugin extends JavaPlugin {

    /**
     * ID плагина на bStats.
     * Замени на свой с <a href="https://bstats.org/getting-started">...</a>
     */
    private static final int BSTATS_PLUGIN_ID = 34699;

    private ConfigLoader configLoader;
    private LanguageManager languageManager;
    private TimeController timeController;
    private AfkManager afkManager;
    private SleepManager sleepManager;
    private Metrics metrics;

    @Override
    public void onEnable() {
        // ─── Папка данных ───
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().severe("Не удалось создать папку плагина!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // ─── Языки (saveResource — потому что это не ConfigLib) ───
        try {
            saveResource("lang/ru_ru.lang", false);
            saveResource("lang/en_us.lang", false);
        } catch (IllegalArgumentException e) {
            getLogger().severe("Файлы языков не найдены в jar! Проверь src/main/resources/lang/");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // ─── Конфиги (ConfigLib, update() создаёт файлы) ───
        this.configLoader = new ConfigLoader(this);
        try {
            configLoader.load();
        } catch (Exception e) {
            getLogger().severe("Не удалось загрузить конфиг: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getScheduler().runTaskLater(this, this::applyVanillaCycleSetting, 40L);

        // ─── bStats ───
        initMetrics();

        // ─── Языки ───
        this.languageManager = new LanguageManager(this, getMainConfig().language);

        // ─── Сервисы ───
        this.timeController = new TimeController(this);
        this.afkManager = new AfkManager(this);
        this.sleepManager = new SleepManager(this);

        // ─── Листенеры ───
        getServer().getPluginManager().registerEvents(new AfkListener(this), this);
        getServer().getPluginManager().registerEvents(new SleepListener(this), this);
        getServer().getPluginManager().registerEvents(new LocaleListener(this), this);

        // ─── Команды ───
        new CommandRegistrar(this).registerAll();

        // ─── PlaceholderAPI ───
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new AfkExpansion(this).register();
            getLogger().info("PlaceholderAPI: зарегистрирован %dnc_afk%");
        }

        // ─── Запуск ───
        timeController.start();
        afkManager.start();

        MainConfig cfg = getMainConfig();
        getLogger().info("DayNightCycle включён. День: "
                + cfg.cycle.dayMinutes + " мин, ночь: "
                + cfg.cycle.nightMinutes + " мин.");
    }

    @Override
    public void onDisable() {
        if (timeController != null) timeController.stop();
        if (afkManager != null) afkManager.stop();
        if (sleepManager != null) sleepManager.shutdown();
        if (configLoader != null) configLoader.save();
        getLogger().info("DayNightCycle выключен.");
    }

    // ═══════════════ BSTATS ═══════════════

    private void initMetrics() {
        try {
            this.metrics = new Metrics(this, BSTATS_PLUGIN_ID);
            MainConfig cfg = getMainConfig();

            metrics.addCustomChart(new SimplePie("day_minutes",
                    () -> String.valueOf(cfg.cycle.dayMinutes)));
            metrics.addCustomChart(new SimplePie("night_minutes",
                    () -> String.valueOf(cfg.cycle.nightMinutes)));
            metrics.addCustomChart(new SimplePie("afk_enabled",
                    () -> String.valueOf(cfg.afk.enabled)));
            metrics.addCustomChart(new SimplePie("sleep_enabled",
                    () -> String.valueOf(cfg.sleep.enabled)));
            metrics.addCustomChart(new SimplePie("sleep_percent",
                    () -> String.valueOf(cfg.sleep.percent)));
            metrics.addCustomChart(new SimplePie("language",
                    () -> cfg.language));

            getLogger().info("bStats: метрики включены (ID " + BSTATS_PLUGIN_ID + ")");
        } catch (Exception e) {
            getLogger().warning("bStats не инициализирован: " + e.getMessage());
        }
    }

    // ═══════════════ VANILLA CYCLE ═══════════════

    /**
     * Применяет настройку {@code cycle.disable-vanilla-cycle} ко всем мирам.
     * Вызывается при старте и при {@link #reloadAll()}.
     */
    private void applyVanillaCycleSetting() {
        if (!getMainConfig().cycle.disableVanillaCycle) return;
        getServer().getWorlds().forEach(w ->
                w.setGameRule(GameRules.ADVANCE_TIME, false));
    }

    // ═══════════════ RELOAD ═══════════════

    public void reloadAll() {
        configLoader.load();
        applyVanillaCycleSetting();
        languageManager.loadLanguages();
        timeController.reload();
        afkManager.reload();
        sleepManager.reload();
    }

    public void saveConfigs() {
        if (configLoader != null) configLoader.save();
    }

    // ═══════════════ GETTERS ═══════════════

    public MainConfig getMainConfig() { return configLoader.getConfig(); }
    public SleepListConfig getSleepList() { return configLoader.getSleepList(); }
    public LanguageManager getLanguageManager() { return languageManager; }
    public TimeController getTimeController() { return timeController; }
    public AfkManager getAfkManager() { return afkManager; }
    public SleepManager getSleepManager() { return sleepManager; }
    public Metrics getMetrics() { return metrics; }
}