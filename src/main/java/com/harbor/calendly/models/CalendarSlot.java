package com.harbor.calendly.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CalendarSlot {
    private String slotId;
    private Long startTime;
    private Long endTime;
    private SlotType slotType;
    private TimeType timeType;

    public CalendarSlot() {}

    public CalendarSlot(long startTime, long endTime, SlotType type) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotType = type;
        this.timeType = TimeType.ABS;
    }

    public CalendarSlot(long startTime, long endTime, SlotType type, TimeType timeType) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotType = type;
        this.timeType = timeType;
    }
}
