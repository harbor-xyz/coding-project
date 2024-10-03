package org.p0.calendly.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.p0.calendly.models.enums.RecurrenceType;

import java.util.List;

@Getter
@AllArgsConstructor
public class MeetingBookingRequest {
    private String organizer;
    private long startDate;
    private int startTime;
    private int endTime;
    private RecurrenceType recurrenceType;
    private Long endDate;
    private List<String> audience;
    //metadata
}