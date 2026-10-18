package ru.terracraft.daynightcycle.update;

import com.github.ezplugins.updater.UpdateResult;
import ru.terracraft.daynightcycle.DayNightCyclePlugin;

/**
 * Проверка обновлений через GitHub Releases.
 * Асинхронно, не блокирует запуск сервера.
 */
public class DncUpdateChecker {

    private static final String GITHUB_OWNER = "luci4ka";
    private static final String GITHUB_REPO = "DayNightCycle";

    private final DayNightCyclePlugin plugin;

    public DncUpdateChecker(DayNightCyclePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Запускает асинхронную проверку обновлений.
     */
    public void check() {
        try {
            com.github.ezplugins.updater.UpdateChecker checker =
                    com.github.ezplugins.updater.UpdateChecker.builder(
                            GITHUB_OWNER,
                            GITHUB_REPO,
                            plugin.getPluginMeta().getVersion()
                    ).build();

            checker.checkNowAsync()
                    .thenAccept(this::handleResult)
                    .exceptionally(ex -> {
                        plugin.getLogger().warning("Update check failed: " + ex.getMessage());
                        return null;
                    });

        } catch (Exception e) {
            plugin.getLogger().warning("Update checker не инициализирован: " + e.getMessage());
        }
    }

    private void handleResult(UpdateResult result) {
        if (result.hasError()) {
            plugin.getLogger().warning("Update check failed: "
                    + result.getError().orElseThrow().getMessage());
            return;
        }

        if (result.isUpdateAvailable()) {
            plugin.getLogger().info("═══════════════════════════════════════");
            plugin.getLogger().info("  Доступна новая версия DayNightCycle!");
            plugin.getLogger().info("  Текущая:  " + plugin.getPluginMeta().getVersion());
            plugin.getLogger().info("  Новая:    " + result.getLatestVersion().orElse("unknown"));
            plugin.getLogger().info("  Ссылка:   " + result.getReleaseUrl().orElse("n/a"));
            plugin.getLogger().info("═══════════════════════════════════════");
        } else {
            plugin.getLogger().info("DayNightCycle: обновлений нет (v"
                    + plugin.getPluginMeta().getVersion() + ")");
        }
    }
}