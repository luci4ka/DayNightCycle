package ru.terracraft.daynightcycle.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SleepListConfig {

    @Comment("""
            Игроки, которые всегда считаются спящими.
            Имена не зависят от регистра.
            Пример:
              players:
                - Notch
            """)
    public List<String> players = new ArrayList<>();
}