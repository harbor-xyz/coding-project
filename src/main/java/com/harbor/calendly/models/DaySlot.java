package com.harbor.calendly.models;

import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DaySlot {
    private Date date;
    private List<CalendarSlot> freeSlots;
    private List<CalendarSlot> bookedSlots;
}
