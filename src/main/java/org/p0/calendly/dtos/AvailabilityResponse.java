package org.p0.calendly.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.p0.calendly.models.Schedule;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AvailabilityResponse {
    private List<Schedule> scheduleList;
}
