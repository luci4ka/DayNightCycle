package ru.terracraft.daynightcycle.lang;

public enum MessageKey {

    AFK_ENABLED("afk.enabled"),
    AFK_DISABLED("afk.disabled"),
    AFK_MANUAL_TOGGLE("afk.manual.toggle"),
    AFK_STATE_ON("afk.state.on"),
    AFK_STATE_OFF("afk.state.off"),

    SLEEP_SKIP("sleep.skip"),
    SLEEP_ANIMATION_START("sleep.animation.start"),

    CYCLE_SET_SUCCESS("cycle.set.success"),

    // ─── Info ───
    COMMAND_INFO_HEADER("command.info.header"),
    COMMAND_INFO_VERSION("command.info.version"),
    COMMAND_INFO_AUTHOR("command.info.author"),
    COMMAND_INFO_LANGUAGES("command.info.languages"),
    COMMAND_INFO_PAPI("command.info.papi"),
    COMMAND_INFO_BSTATS("command.info.bstats"),
    COMMAND_INFO_FOOTER("command.info.footer"),

    // ─── State ───
    STATE_ENABLED("state.enabled"),
    STATE_DISABLED("state.disabled"),

    COMMAND_RELOAD_SUCCESS("command.reload.success"),
    COMMAND_RELOAD_ERROR("command.reload.error"),
    COMMAND_STATUS_HEADER("command.status.header"),
    COMMAND_STATUS_DAY("command.status.day"),
    COMMAND_STATUS_NIGHT("command.status.night"),
    COMMAND_STATUS_AFK_TIMEOUT("command.status.afk-timeout"),
    COMMAND_STATUS_AFK_PLAYERS("command.status.afk-players"),
    COMMAND_STATUS_FOOTER("command.status.footer"),

    SLEEPLIST_HEADER("sleeplist.header"),
    SLEEPLIST_ENTRY("sleeplist.entry"),
    SLEEPLIST_ADDED("sleeplist.added"),
    SLEEPLIST_REMOVED("sleeplist.removed"),
    SLEEPLIST_NOT_FOUND("sleeplist.not-found"),

    COMMAND_TIME_HEADER("command.time.header"),
    COMMAND_TIME_WORLD("command.time.world"),
    COMMAND_TIME_DAY("command.time.day"),
    COMMAND_TIME_FOOTER("command.time.footer"),

    // ─── Time: миры ───
    COMMAND_TIME_WORLD_OVERWORLD("command.time.world.overworld"),
    COMMAND_TIME_WORLD_NETHER("command.time.world.nether"),
    COMMAND_TIME_WORLD_END("command.time.world.end"),
    COMMAND_TIME_WORLD_OTHER("command.time.world.other"),
    SLEEPLIST_INVALID_NAME("sleeplist.invalid-name"),
    AFK_DISABLED_SYSTEM("afk.disabled.system"),
    ERROR_NO_PERMISSION("error.no-permission");


    private final String path;

    MessageKey(String path) { this.path = path; }
    public String path() { return path; }
}