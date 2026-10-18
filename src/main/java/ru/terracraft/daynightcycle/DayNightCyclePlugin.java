package ru.terracraft.daynightcycle;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.GameRules;
import org.bukkit.World;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import ru.terracraft.daynightcycle.afk.AfkListener;
import ru.terracraft.daynightcycle.afk.AfkManager;
import ru.terracraft.daynightcycle.command.CommandRegistrar;
import ru.terracraft.daynightcycle.config.ConfigLoader;
import ru.terracraft.daynightcycle.config.MainConfig;
import ru.terracraft.daynightcycle.config.SleepListConfig;
import ru.terracraft.daynightcycle.config.StateStorage;
import ru.terracraft.daynightcycle.cycle.TimeController;
import ru.terracraft.daynightcycle.lang.LanguageManager;
import ru.terracraft.daynightcycle.lang.LocaleListener;
import ru.terracraft.daynightcycle.listener.WorldListener;
import ru.terracraft.daynightcycle.placeholder.DncExpansion;
import ru.terracraft.daynightcycle.sleep.SleepListener;
import ru.terracraft.daynightcycle.sleep.SleepManager;
import ru.terracraft.daynightcycle.update.DncUpdateChecker;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class DayNightCyclePlugin extends JavaPlugin {

    private static final int BSTATS_PLUGIN_ID = 34699;

    private ConfigLoader configLoader;
    private LanguageManager languageManager;
    private TimeController timeController;
    private AfkManager afkManager;
    private SleepManager sleepManager;
    private DncUpdateChecker updateChecker;
    private DncExpansion placeholderExpansion;
    private Metrics metrics;
    private StateStorage stateStorage;

    private boolean loaded = false;

    @Override
    public void onEnable() {

        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().severe("Не удалось создать папку плагина!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.stateStorage = new StateStorage(this);
        stateStorage.load();

        registerPermissions();

        this.configLoader = new ConfigLoader(this);
        try {
            configLoader.load();
            loaded = true;
        } catch (Exception e) {
            getLogger().severe("Не удалось загрузить конфиг: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.languageManager = new LanguageManager(this, getMainConfig().language);
        this.timeController = new TimeController(this);
        this.afkManager = new AfkManager(this);
        this.sleepManager = new SleepManager(this);

        getServer().getPluginManager().registerEvents(new AfkListener(this), this);
        getServer().getPluginManager().registerEvents(new SleepListener(this), this);
        getServer().getPluginManager().registerEvents(new LocaleListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldListener(this), this);

        getServer().getWorlds().forEach(w -> syncWorld(w, false));
        cleanupStaleState();
        stateStorage.saveIfDirty();

        new CommandRegistrar(this).registerAll();

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            this.placeholderExpansion = new DncExpansion(this);
            if (placeholderExpansion.register()) {
                getLogger().info("PlaceholderAPI: зарегистрированы %dnc_afk%, %dnc_afk_prefix%, %dnc_time%, %dnc_ticks%, %dnc_world%");
            } else {
                getLogger().warning("PlaceholderAPI: не удалось зарегистрировать расширение");
            }
        }

        this.updateChecker = new DncUpdateChecker(this);
        if (getMainConfig().updateCheck) {
            updateChecker.check();
        }

        timeController.start();
        afkManager.start();
        initMetrics();

        MainConfig cfg = getMainConfig();
        getLogger().info("DayNightCycle включён. День: " + cfg.cycle.dayMinutes
                + " мин, ночь: " + cfg.cycle.nightMinutes + " мин.");
    }

    @Override
    public void onDisable() {
        if (timeController != null) timeController.stop();
        if (afkManager != null) afkManager.stop();
        if (sleepManager != null) sleepManager.shutdown();

        if (stateStorage != null) {
            for (UUID worldId : List.copyOf(stateStorage.all().keySet())) {
                World world = getServer().getWorld(worldId);
                if (world != null) syncWorld(world, true);
            }
            stateStorage.saveIfDirty();
        }

        if (placeholderExpansion != null) placeholderExpansion.unregister();
        if (metrics != null) { try { metrics.shutdown(); } catch (Throwable ignored) {} }
        getLogger().info("DayNightCycle выключен.");
    }

    // ═══════════════ STATE ═══════════════

    /** Удаляет записи о мирах, которых больше нет на диске. */
    private void cleanupStaleState() {
        var existing = getServer().getWorlds().stream()
                .map(World::getUID)
                .collect(Collectors.toSet());
        for (UUID id : stateStorage.all().keySet()) {
            if (!existing.contains(id)) {
                stateStorage.remove(id);
            }
        }
    }

    // ═══════════════ SYNC ═══════════════

    public void syncWorld(World w, boolean forceRestore) {
        boolean want = false;
        MainConfig cfg = null;

        if (!forceRestore && loaded) {
            cfg = getMainConfig();
            want = cfg.cycle.enabled
                    && cfg.cycle.disableVanillaCycle
                    && w.getEnvironment() == World.Environment.NORMAL
                    && (cfg.cycle.worlds.isEmpty() || cfg.cycle.worlds.contains(w.getName()));
        }

        StateStorage.Original orig = stateStorage.get(w.getUID());
        if (want) {
            if (orig == null) {
                Boolean adv = w.getGameRuleValue(GameRules.ADVANCE_TIME);
                Integer pct = w.getGameRuleValue(GameRules.PLAYERS_SLEEPING_PERCENTAGE);
                orig = new StateStorage.Original(adv != null ? adv : Boolean.TRUE,
                        pct != null ? pct : 100);
                stateStorage.put(w.getUID(), orig);
            }
            w.setGameRule(GameRules.ADVANCE_TIME, false);
            if (cfg.sleep.enabled) {
                w.setGameRule(GameRules.PLAYERS_SLEEPING_PERCENTAGE, 101);
            } else {
                w.setGameRule(GameRules.PLAYERS_SLEEPING_PERCENTAGE, orig.sleepPercent());
            }
        } else if (orig != null) {
            w.setGameRule(GameRules.ADVANCE_TIME, orig.advance());
            w.setGameRule(GameRules.PLAYERS_SLEEPING_PERCENTAGE, orig.sleepPercent());
            stateStorage.remove(w.getUID());
        }
    }

    public void forgetWorld(World w) {
        syncWorld(w, true);
        if (timeController != null) timeController.forgetWorld(w);
    }

    // ═══════════════ PERMISSIONS ═══════════════

    private void registerPermissions() {
        var pm = getServer().getPluginManager();
        record P(String name, PermissionDefault def) {}
        for (var p : List.of(
                new P("dnc.afk", PermissionDefault.TRUE),
                new P("dnc.bypass", PermissionDefault.FALSE),
                new P("dnc.command.status", PermissionDefault.TRUE),
                new P("dnc.command.info", PermissionDefault.TRUE),
                new P("dnc.command.time", PermissionDefault.TRUE),
                new P("dnc.command.sleeplist", PermissionDefault.OP),
                new P("dnc.command.sleeplist.view", PermissionDefault.OP),
                new P("dnc.command.set", PermissionDefault.OP),
                new P("dnc.command.reload", PermissionDefault.OP))) {
            if (pm.getPermission(p.name()) == null) {
                pm.addPermission(new Permission(p.name(), p.def()));
            }
        }
        var sl = pm.getPermission("dnc.command.sleeplist");
        if (sl != null) {
            sl.getChildren().put("dnc.command.sleeplist.view", true);
            sl.recalculatePermissibles();
        }
    }

    // ═══════════════ METRICS ═══════════════

    private void initMetrics() {
        try {
            this.metrics = new Metrics(this, BSTATS_PLUGIN_ID);
            metrics.addCustomChart(new SimplePie("day_minutes", () -> String.valueOf(getMainConfig().cycle.dayMinutes)));
            metrics.addCustomChart(new SimplePie("night_minutes", () -> String.valueOf(getMainConfig().cycle.nightMinutes)));
            metrics.addCustomChart(new SimplePie("afk_enabled", () -> String.valueOf(getMainConfig().afk.enabled)));
            metrics.addCustomChart(new SimplePie("sleep_enabled", () -> String.valueOf(getMainConfig().sleep.enabled)));
            metrics.addCustomChart(new SimplePie("sleep_percent", () -> String.valueOf(getMainConfig().sleep.percent)));
            metrics.addCustomChart(new SimplePie("language", () -> getMainConfig().language));
            getLogger().info("bStats: метрики включены (ID " + BSTATS_PLUGIN_ID + ")");
        } catch (Exception e) {
            getLogger().warning("bStats не инициализирован: " + e.getMessage());
        }
    }

    // ═══════════════ RELOAD ═══════════════

    /**
     * Перезагружает всё. Возвращает null при успехе, иначе текст ошибки.
     */
    public String reloadAll() {
        try {
            configLoader.load();
            getServer().getWorlds().forEach(w -> syncWorld(w, false));
            languageManager.reload();
            timeController.reload();
            afkManager.reload();
            sleepManager.reload();
            stateStorage.saveIfDirty();
            getServer().getScheduler().runTask(this, () ->
                    getServer().getWorlds().forEach(sleepManager::checkSleep));
            return null;
        } catch (Exception e) {
            getLogger().warning("Ошибка перезагрузки: " + e.getMessage());
            return e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        }
    }

    // ═══════════════ GETTERS ═══════════════

    public MainConfig getMainConfig() { return configLoader.getConfig(); }
    public SleepListConfig getSleepList() { return configLoader.getSleepList(); }
    public LanguageManager getLanguageManager() { return languageManager; }
    public TimeController getTimeController() { return timeController; }
    public AfkManager getAfkManager() { return afkManager; }
    public SleepManager getSleepManager() { return sleepManager; }
    public Metrics getMetrics() { return metrics; }
    public ConfigLoader getConfigLoader() { return configLoader; }
    public StateStorage getStateStorage() { return stateStorage; }
}