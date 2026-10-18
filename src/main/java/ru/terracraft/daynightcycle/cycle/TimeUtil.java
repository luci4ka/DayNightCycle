package ru.terracraft.daynightcycle.cycle;

public final class TimeUtil {

    private TimeUtil() {}

    /**
     * Конвертирует тики Minecraft в формат 24 часа (HH:mm).
     * 0 тиков = 06:00, 12000 тиков = 18:00.
     */
    public static String toTime24(long ticks) {
        long hours = ((ticks / 1000) + 6) % 24;
        long minutes = (ticks % 1000) * 60 / 1000;
        return String.format("%02d:%02d", hours, minutes);
    }
}