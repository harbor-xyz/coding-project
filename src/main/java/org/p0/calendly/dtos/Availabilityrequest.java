package org.p0.calendly.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.p0.calendly.models.Schedule;

import java.util.List;

@AllArgsConstructor
@Getter
public class Availabilityrequest {
    private String userId;
    private List<Schedule> scheduleList;
}
