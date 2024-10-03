package org.p0.calendly.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.p0.calendly.models.enums.RecurrenceType;

import java.util.List;

@Getter
@AllArgsConstructor
public class Schedule {
    private long startDate;
    private long startTime;
    private long endTime;
    private RecurrenceType recurrenceType;
    private Long endDate;
    //metadata
}
