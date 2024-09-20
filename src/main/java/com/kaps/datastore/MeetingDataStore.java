package com.kaps.datastore;

import com.kaps.Utility;
import com.kaps.model.Meeting;

import java.util.HashMap;
import java.util.Map;

public class MeetingDataStore {
    Map<String, Meeting> meetingDataMap;

    public static MeetingDataStore getInstance() {
        return new MeetingDataStore();
    }

    private MeetingDataStore() {
        meetingDataMap = new HashMap<>();
    }

    public Meeting scheduleMeeting(Meeting meeting) {
        meeting.setId(Utility.generateUUID());
        meetingDataMap.put(meeting.getId(), meeting);

        return meetingDataMap.get(meeting.getId());
    }
}
