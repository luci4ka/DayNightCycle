package ru.terracraft.daynightcycle.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class MainConfig {

    @Comment("Версия конфига")
    public int configVersion = 101;

    @Comment("Язык по умолчанию (ru_ru, en_us)")
    public String language = "ru_ru";

    @Comment("Проверять обновления при старте (GitHub Releases)")
    public boolean updateCheck = true;

    @Comment("Настройки цикла дня/ночи")
    public CycleSection cycle = new CycleSection();

    @Comment("Настройки пропуска сна")
    public SleepSection sleep = new SleepSection();

    @Comment("Настройки AFK")
    public AfkSection afk = new AfkSection();

    @Configuration
    public static class CycleSection {
        @Comment("Включить управление циклом")
        public boolean enabled = true;

        @Comment("Длительность дня в реальных минутах (12000 тиков)")
        public int dayMinutes = 20;

        @Comment("Длительность ночи в реальных минутах (12000 тиков)")
        public int nightMinutes = 10;

        @Comment("Список миров (пусто = все обычные миры)")
        public List<String> worlds = new ArrayList<>();

        @Comment("""
                Отключить ванильный цикл (ADVANCE_TIME).
                Если false — ваш цикл и ванильный работают одновременно,
                время идёт примерно вдвое быстрее.
                """)
        public boolean disableVanillaCycle = true;
    }

    @Configuration
    public static class SleepSection {
        @Comment("Включить пропуск сна")
        public boolean enabled = true;

        @Comment("Процент спящих (0.5 = 50%)")
        public double percent = 0.5;

        @Comment("AFK-игроки считаются спящими")
        public boolean afkCountsAsSleeping = true;

        @Comment("Сообщение о пропуске ночи")
        public boolean broadcastSkip = true;

        @Comment("Сбрасывать погоду при пропуске ночи")
        public boolean clearWeather = true;

        @Comment("Анимация ускорения ночи")
        public AnimationSection animation = new AnimationSection();

        @Configuration
        public static class AnimationSection {
            @Comment("Включить анимацию")
            public boolean enabled = true;

            @Comment("Длительность анимации (сек)")
            public int durationSeconds = 3;
        }
    }

    @Configuration
    public static class AfkSection {
        @Comment("Включить AFK-систему")
        public boolean enabled = true;

        @Comment("Таймаут AFK в минутах (5 = через 5 минут простоя)")
        public int timeoutMinutes = 5;

        @Comment("Сбрасывать AFK при движении")
        public boolean resetOnMove = true;

        @Comment("Сбрасывать AFK при чате/командах")
        public boolean resetOnChat = true;

        @Comment("Сбрасывать AFK при взаимодействии (клик, дроп, инвентарь)")
        public boolean resetOnInteract = true;

        @Comment("Игнорировать микро-сдвиги (толкание мобов, фантомы)")
        public boolean smartMovement = true;

        @Comment("Отображение AFK")
        public DisplaySection display = new DisplaySection();

        @Configuration
        public static class DisplaySection {
            @Comment("Текст AFK в action bar и плейсхолдере")
            public String text = "AFK";
        }
    }
}