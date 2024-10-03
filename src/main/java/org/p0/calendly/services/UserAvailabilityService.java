package org.p0.calendly.services;

import org.p0.calendly.dtos.AvailabilityResponse;
import org.p0.calendly.dtos.Availabilityrequest;
import org.p0.calendly.exceptions.UserNotFoundException;
import org.p0.calendly.models.Schedule;
import org.p0.calendly.models.User;
import org.p0.calendly.models.enums.RecurrenceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserAvailabilityService {

    private Map<String, TreeMap<Long, Long>> userAvailabilityMap = new HashMap<>();

    @Autowired
    private UserService userService;

    // Create a new user
    public void createUserAvailabilty(final Availabilityrequest availabilityrequest) {

        User user = userService.getUserById(availabilityrequest.getUserId());

        userAvailabilityMap.put(availabilityrequest.getUserId(),addUserSchedule(availabilityrequest.getScheduleList()));

    }
    public AvailabilityResponse getUserAvailability(String id) {
        User user = userService.getUserById(id);

        return new AvailabilityResponse(convertTimestampToSchedule(userAvailabilityMap.get(id)));
    }

    public TreeMap<Long, Long> getAvailability(String id)
    {
        return userAvailabilityMap.get(id);
    }

    public boolean isSlotAvailable(String userId, Long start, Long end)
    {
        if(!userAvailabilityMap.containsKey(userId))
            throw new UserNotFoundException();

        TreeMap<Long, Long> availability = userAvailabilityMap.get(userId);

        if(availability.containsKey(start) && availability.get(start) >= end)
            return true;
        if(availability.lowerKey(start) != null && availability.get(availability.lowerKey(start)) >= end)
            return true;

        return false;
    }

    public void updateAvailability(String userId, Long start, Long end)
    {
        TreeMap<Long, Long> availability = userAvailabilityMap.get(userId);

        if(availability.containsKey(start) && availability.get(start) == end)
        {
            availability.remove(start);

        }
        else if(availability.containsKey(start) && availability.get(start) > end)
        {
            availability.put(end,availability.get(start));
            availability.remove(start);

        }
        else if(availability.lowerKey(start) != null && availability.get(availability.lowerKey(start)) == end)
        {
            availability.put(availability.lowerKey(start),start);

        }
        else if(availability.lowerKey(start) != null && availability.get(availability.lowerKey(start)) >= end)
        {
            Long value = availability.get(availability.lowerKey(start));
            availability.put(availability.lowerKey(start),start);
            availability.put(end,value);

        }
    }

    private List<Schedule> convertTimestampToSchedule(TreeMap<Long, Long> map)
    {
        Long weeklyEpoch =Long.valueOf(7* 86400000);
        List<Schedule> scheduleList = new ArrayList<>();
        map.forEach((k,v) -> {
            Long tempKey = k;
            Long tempValue = v;
            while(map.containsKey(tempKey + weeklyEpoch))
            {
            tempValue = map.get(tempKey + weeklyEpoch);
                map.remove(tempKey);
                tempKey = tempKey + weeklyEpoch;

            }

            RecurrenceType type = tempKey != k ? RecurrenceType.WEEKLY : RecurrenceType.NONE;
            scheduleList.add(new Schedule(tempKey - (tempKey% 86400000),
                    (tempKey % 86400000), (tempValue% 86400000),
                    type, tempValue - (tempValue % 86400000) + 86400000));
        });

        return scheduleList;
    }

    private TreeMap<Long,Long> addUserSchedule(List<Schedule> scheduleList)
    {
        TreeMap<Long,Long> scheduleMap = new TreeMap<>();

        for(Schedule schedule : scheduleList)
        {
            Long startDate = schedule.getStartDate();
            Long startTime = Long.valueOf(schedule.getStartTime());
            Long endTime = Long.valueOf(schedule.getEndTime());
            Long endDate = schedule.getEndDate();

            switch (schedule.getRecurrenceType())
            {
                case NONE -> scheduleMap.put(startDate+startTime, startDate+endTime);
                case WEEKLY -> {
                    Long weeklyEpoch =Long.valueOf(7* 86400000);
                    Long tempStartDate = startDate;
                    while(endDate <= startDate)
                    {
                        scheduleMap.put(startDate+startTime, startDate+endTime);
                        tempStartDate +=weeklyEpoch;
                    }
                }
                case MONTHLY -> {
                    //TOdo: add monthly
                }
                case YEARLY -> {
                    //TOdo: add yearly
                }
            }
        }

        return scheduleMap;
    }
}