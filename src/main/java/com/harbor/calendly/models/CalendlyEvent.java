package com.harbor.calendly.models;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Document(collection = "event_data")
public class CalendlyEvent {
    @Id
    private String id;

    private String userId;
    private String eventName;
    private List<DOWSlot> freeSlots;
    private List<DOWSlot> bookedSlots;
    private List<DaySlot> freeDays;
    private List<DaySlot> bookedDays;
    private long eventStartDateTime;
    private long eventEndDateTime;
    private int slotDurationInMins = 30;
}
