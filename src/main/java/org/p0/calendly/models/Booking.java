package org.p0.calendly.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class Booking {
    private String bookingId;

    private String linkedBookingId;
    @Setter
    private String organizer;
    @Setter
    private long startTime;
    @Setter
    private long endTime;
    @Setter
    private List<String> audience;
    //metadata

    public Booking(@NonNull String organizer, long startTime, long endTime, @NonNull List<String> audience,String linkedBookingId)
    {
        this.bookingId = this.setUniqueBookingId();
        this.linkedBookingId = linkedBookingId ==null ? setUniquelinkedBookingId() : linkedBookingId;
        this.audience = audience;
        this.startTime = startTime;
        this.endTime = endTime;
        this.organizer = organizer;
    }


    private String setUniqueBookingId()
    {
        return UUID.randomUUID().toString();
    }

    private String setUniquelinkedBookingId()
    {
        return UUID.randomUUID().toString();
    }
}
