package com.harbor.calendly.models;

import java.util.ArrayList;
import java.util.List;

public enum DayOfWeek {
    SUNDAY(true), MONDAY(false), TUESDAY(false), WEDNESDAY(false), THURSDAY(false), FRIDAY(false), SATURDAY(true);

    private boolean isWeekend;

    private DayOfWeek(boolean isWeekend) {
        this.isWeekend = isWeekend;
    }

    public boolean isWeekend() {
        return this.isWeekend;
    }

    public static List<DayOfWeek> getWeekdays() {
        List<DayOfWeek> result = new ArrayList<>();
        for (DayOfWeek dow : DayOfWeek.values()) {
            if (!dow.isWeekend) {
                result.add(dow);
            }
        }
        return result;
    }

    public static List<DayOfWeek> getWeekends() {
        List<DayOfWeek> result = new ArrayList<>();
        for (DayOfWeek dow : DayOfWeek.values()) {
            if (dow.isWeekend) {
                result.add(dow);
            }
        }
        return result;
    }
}
