package com.harbor.calendly.models;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DOWSlot {
    private DayOfWeek dow;
    private List<CalendarSlot> freeSlots;
    private List<CalendarSlot> bookedSlots;
}
