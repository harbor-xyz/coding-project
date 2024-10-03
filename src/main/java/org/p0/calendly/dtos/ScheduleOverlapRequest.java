package org.p0.calendly.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class ScheduleOverlapRequest {
    private List<String> users;
}
