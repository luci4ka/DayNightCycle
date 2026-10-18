package ru.terracraft.daynightcycle.lang;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class LangFile {

    private final Map<String, String> strings = new LinkedHashMap<>();

    public static LangFile load(File file) throws IOException {
        try (InputStream in = Files.newInputStream(file.toPath())) {
            return loadFrom(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
    }

    public static LangFile loadFrom(Reader source) throws IOException {
        PushbackReader reader = new PushbackReader(source, 1);
        int first = reader.read();
        if (first != -1 && first != 0xFEFF) {
            reader.unread(first);
        }
        Properties props = new Properties();
        props.load(reader);
        LangFile lang = new LangFile();
        for (String key : props.stringPropertyNames()) {
            lang.strings.put(key, props.getProperty(key));
        }
        return lang;
    }

    public String getString(String path) {
        return strings.get(path);
    }
}