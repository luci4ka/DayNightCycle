package ru.terracraft.daynightcycle.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class MainConfig {

    @Comment("Версия конфига")
    public int configVersion = 1;

    @Comment("Язык по умолчанию (ru_ru, en_us)")
    public String language = "ru_ru";

    @Comment("Настройки цикла дня/ночи")
    public CycleSection cycle = new CycleSection();

    @Comment("Настройки пропуска сна")
    public SleepSection sleep = new SleepSection();

    @Comment("Настройки AFK")
    public AfkSection afk = new AfkSection();

    @Configuration
    public static class CycleSection {
        public boolean enabled = true;
        public int dayMinutes = 20;
        public int nightMinutes = 10;
        public List<String> worlds = new ArrayList<>();
        public boolean disableVanillaCycle = true;
    }

    @Configuration
    public static class SleepSection {
        public boolean enabled = true;
        public double percent = 0.5;
        public boolean afkCountsAsSleeping = true;
        public boolean broadcastSkip = true;
        public AnimationSection animation = new AnimationSection();

        @Configuration
        public static class AnimationSection {
            public boolean enabled = true;
            public int durationSeconds = 3;
        }
    }

    @Configuration
    public static class AfkSection {
        public boolean enabled = true;
        public int timeoutMinutes = 5;
        public boolean resetOnMove = true;
        public boolean resetOnChat = true;
        public boolean resetOnInteract = true;
        public boolean smartMovement = true;
        public DisplaySection display = new DisplaySection();

        @Configuration
        public static class DisplaySection {
            public String text = "AFK";
        }
    }

}