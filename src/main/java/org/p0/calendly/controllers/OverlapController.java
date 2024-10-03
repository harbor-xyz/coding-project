package org.p0.calendly.controllers;

import lombok.NonNull;
import org.p0.calendly.dtos.ScheduleOverlapRequest;
import org.p0.calendly.dtos.ScheduleOverlapResponse;
import org.p0.calendly.exceptions.NotEnoughUsersException;
import org.p0.calendly.services.AvailabilityOverlapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/overlap")
public class OverlapController {


    @Autowired
    private AvailabilityOverlapService availabilityOverlapService;

    // get overlaps for comma separated userIds
    @GetMapping("/ids={id}")
    public ResponseEntity<ScheduleOverlapResponse> getOverlaps(@PathVariable @NonNull String id) {
        // todo: validate input
        List<String> scheduleOverlapRequest = Arrays.asList(id.split(",", -1));

        ScheduleOverlapResponse response = new ScheduleOverlapResponse();
        try {
            response = availabilityOverlapService.getUserOverlaps(scheduleOverlapRequest);
        }
        catch (NotEnoughUsersException e)
        {
            return ResponseEntity.status(400).build();
        }
        return ResponseEntity.status(200).body(response);
    }
}
