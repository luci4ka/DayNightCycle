package ru.terracraft.daynightcycle.lang;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class LangFile {

    private final Map<String, String> strings;
    private final String sourceName;

    private LangFile(Map<String, String> strings, String sourceName) {
        this.strings = strings;
        this.sourceName = sourceName;
    }

    public static LangFile load(File file) throws IOException {
        try (InputStream in = Files.newInputStream(file.toPath())) {
            return loadFrom(in, file.getName());
        }
    }

    public static LangFile loadFrom(Reader source) throws IOException {
        return loadFrom(source, "<reader>");
    }

    public static LangFile loadFrom(InputStream stream) throws IOException {
        return loadFrom(stream, "<stream>");
    }

    public static LangFile loadFrom(InputStream stream, String sourceName) throws IOException {
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return loadFrom(reader, sourceName);
        }
    }

    public static LangFile loadFrom(Reader source, String sourceName) throws IOException {
        PushbackReader reader = new PushbackReader(source, 1);
        int first = reader.read();
        if (first != -1 && first != 0xFEFF) {
            reader.unread(first);
        }

        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(reader);
        } catch (InvalidConfigurationException e) {
            throw new IOException("Invalid YAML in " + sourceName
                    + " (" + e.getMessage() + ")", e);
        }
        return fromConfig(config, sourceName);
    }

    private static LangFile fromConfig(YamlConfiguration config, String sourceName) {
        Map<String, String> map = new LinkedHashMap<>();
        for (String key : config.getKeys(true)) {
            Object value = config.get(key);
            if (value instanceof String s) {
                map.put(key, s);
            }
        }
        return new LangFile(Collections.unmodifiableMap(map), sourceName);
    }

    public String getString(String path) {
        return strings.get(path);
    }

    public Map<String, String> getAll() {
        return strings;
    }

    public String sourceName() {
        return sourceName;
    }

    public int size() {
        return strings.size();
    }
}