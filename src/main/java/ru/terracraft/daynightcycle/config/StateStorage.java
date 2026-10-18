package ru.terracraft.daynightcycle.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.terracraft.daynightcycle.DayNightCyclePlugin;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Хранилище исходных значений gamerules для замороженных миров.
 * Переживает краш сервера (kill -9), в отличие от памяти.
 * Запись атомарная: сначала в .tmp, потом переименование.
 */
public class StateStorage {

    public record Original(Boolean advance, Integer sleepPercent) {}

    private final DayNightCyclePlugin plugin;
    private final File file;
    private final Map<UUID, Original> frozen = new HashMap<>();
    private volatile boolean dirty = false;

    public StateStorage(DayNightCyclePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "state.yml");
    }

    public void load() {
        frozen.clear();
        dirty = false;
        if (!file.exists()) return;
        try {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection root = yaml.getConfigurationSection("worlds");
            if (root == null) return;
            for (String key : root.getKeys(false)) {
                try {
                    UUID id = UUID.fromString(key);
                    boolean adv = root.getBoolean(key + ".advance", true);
                    int pct = root.getInt(key + ".sleep-percent", 100);
                    frozen.put(id, new Original(adv, pct));
                } catch (IllegalArgumentException ignored) {}
            }
            plugin.getLogger().info("Загружено состояние для " + frozen.size() + " замороженных миров.");
        } catch (Exception e) {
            plugin.getLogger().warning("Не удалось загрузить state.yml: " + e.getMessage());
        }
    }

    /**
     * Пишет state.yml, если были изменения. Атомарно: .tmp → rename.
     */
    public void saveIfDirty() {
        if (!dirty) return;
        dirty = false;
        try {
            YamlConfiguration yaml = new YamlConfiguration();
            ConfigurationSection root = yaml.createSection("worlds");
            for (var entry : frozen.entrySet()) {
                String key = entry.getKey().toString();
                root.set(key + ".advance", entry.getValue().advance());
                root.set(key + ".sleep-percent", entry.getValue().sleepPercent());
            }

            Path target = file.toPath();
            Path tmp = target.resolveSibling("state.yml.tmp");
            Files.writeString(tmp, yaml.saveToString(), StandardCharsets.UTF_8);

            try {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Не удалось сохранить state.yml: " + e.getMessage());
        }
    }

    public Original get(UUID id) { return frozen.get(id); }

    public void put(UUID id, Original orig) {
        frozen.put(id, orig);
        dirty = true;
    }

    public void remove(UUID id) {
        if (frozen.remove(id) != null) dirty = true;
    }

    public Map<UUID, Original> all() { return Map.copyOf(frozen); }
    public boolean isEmpty() { return frozen.isEmpty(); }
    public void clear() { frozen.clear(); dirty = true; }
}