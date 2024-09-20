package com.kaps.service;

import com.kaps.datastore.MeetingDataStore;
import com.kaps.model.Meeting;
import com.kaps.model.Slot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MeetingService {
    MeetingDataStore meetingDataStore;
    @Autowired UserService userService;

    public MeetingService() {
        meetingDataStore = MeetingDataStore.getInstance();
        userService = new UserService();
    }

    /*
    * Scheduling Algo:
    * 1. the requestor should have exact slot present otherwise throw exception, capture this slot id
    * 2. then find overlapping slot for requestee
    * 3. schedule the meet
    * 4. remove these slots
    * */

    public Meeting scheduleMeeting(String requestor, String requestee, LocalDateTime startTime, LocalDateTime endTime) throws Exception {
        performMeetingSanity(startTime, endTime);
        if (!userService.isSlotAvailable(requestor, startTime, endTime)) {
            throw new Exception("Requestor of the meeting does not have the slot himself. Kindly add a slot and then schedule!");
        }
        Slot requesteeSlot = userService.getOverlappingSlots(requestee, startTime, endTime);
        Meeting meet = meetingDataStore.scheduleMeeting(new Meeting(null, requestor, requestee, startTime, endTime));
        userService.removeSlot(requestee, requesteeSlot.getId());
        userService.removeSlot(requestor, userService.getSlotId(requestor, startTime, endTime));

        return meet;
    }

    public void performMeetingSanity(LocalDateTime startTime, LocalDateTime endTime) throws Exception {
        if (startTime.isAfter(endTime)) {
            throw new Exception("Meeting start time cannot be after meeting end time!");
        }
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new Exception("Cannot schedule an expired meeting!");
        }
    }
}
