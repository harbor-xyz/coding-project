package org.p0.calendly.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.TreeMap;

@NoArgsConstructor
@Getter
@AllArgsConstructor
public class ScheduleOverlapResponse {
    private TreeMap<Long,Long> overlaps;
}
