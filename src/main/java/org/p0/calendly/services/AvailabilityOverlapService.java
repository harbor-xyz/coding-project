package org.p0.calendly.services;

import lombok.NonNull;
import org.p0.calendly.dtos.ScheduleOverlapRequest;
import org.p0.calendly.dtos.ScheduleOverlapResponse;
import org.p0.calendly.exceptions.NotEnoughUsersException;
import org.p0.calendly.strategies.OverlapStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.TreeMap;

@Service
public class AvailabilityOverlapService {
    @Autowired
    private UserAvailabilityService userAvailabilityService;

    @Autowired
    private OverlapStrategy overlapStrategy;

    public ScheduleOverlapResponse getUserOverlaps(@NonNull List<String> scheduleOverlapRequest) {

        if(scheduleOverlapRequest == null ||  scheduleOverlapRequest.size() < 2)
            throw new NotEnoughUsersException();

        return getOverlaps(scheduleOverlapRequest);
    }

    private ScheduleOverlapResponse getOverlaps(List<String> users)
    {
        // Todo: check user validation
        String userId = users.get(0);

        TreeMap<Long,Long> userAvailability = userAvailabilityService.getAvailability(userId);

        for (int i=1;i<users.size();i++)
        {
            TreeMap<Long,Long> neighbourAvailability = userAvailabilityService.getAvailability(users.get(i));
            userAvailability = overlapStrategy.findOverlaps(userAvailability, neighbourAvailability);
        }

        return new ScheduleOverlapResponse(userAvailability);
    }
}
