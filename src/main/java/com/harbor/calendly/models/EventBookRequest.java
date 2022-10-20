package com.harbor.calendly.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Document(collection = "booked_events")
public class EventBookRequest {
    @Id
    private String id;

    private String userId;
    private String eventId;
    private long slotStartTime;
    private long slotEndTime;
    private UserInformation userInformation;
}
