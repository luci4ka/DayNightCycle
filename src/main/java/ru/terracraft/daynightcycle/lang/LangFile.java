package ru.terracraft.daynightcycle.lang;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Загрузчик .lang файлов (properties-формат с поддержкой списков через .1, .2, ...)
 */
public class LangFile {

    private final Map<String, String> strings = new LinkedHashMap<>();
    private final Map<String, List<String>> lists = new LinkedHashMap<>();

    public static LangFile load(File file) throws IOException {
        try (InputStream in = Files.newInputStream(file.toPath())) {
            return loadFrom(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
    }

    public static LangFile loadFrom(InputStreamReader reader) throws IOException {
        Properties props = new Properties();
        props.load(reader);

        LangFile lang = new LangFile();
        for (String key : props.stringPropertyNames()) {
            lang.strings.put(key, props.getProperty(key));
        }
        lang.buildLists();
        return lang;
    }

    private void buildLists() {
        for (String key : new ArrayList<>(strings.keySet())) {
            // motd.lines.1, motd.lines.2 → motd.lines
            int lastDot = key.lastIndexOf('.');
            if (lastDot <= 0) continue;
            String suffix = key.substring(lastDot + 1);
            if (!suffix.matches("\\d+")) continue;

            String base = key.substring(0, lastDot);
            lists.computeIfAbsent(base, _ -> new ArrayList<>())
                    .add(strings.get(key));
        }
    }

    public String getString(String path) {
        return strings.get(path);
    }

    public static String unescape(String s) {
        return s.replace("\\n", "\n");
    }
}