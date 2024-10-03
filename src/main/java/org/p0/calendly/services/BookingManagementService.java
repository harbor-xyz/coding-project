package org.p0.calendly.services;

import lombok.NonNull;
import org.p0.calendly.dtos.*;
import org.p0.calendly.models.Booking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BookingManagementService {
    private Map<String, Booking> bookingMap = new HashMap<>();
    private Map<String, List<String>> userBookingMap = new HashMap<>();
    private Map<String, List<String>> recurringBookingMap = new HashMap<>();

    @Autowired
    private UserAvailabilityService userAvailabilityService;

    public String createBooking(@NonNull MeetingBookingRequest meetingBookingRequest) {
        return book(meetingBookingRequest);
    }

    public Booking getBookingById(@NonNull String id) {
        return bookingMap.get(id);
    }

    public List<Booking> getBookingByuserId(@NonNull String id) {
        List<String> bookingIds = userBookingMap.get(id);
        List<Booking> bookingList = new ArrayList<>();
        for(String bookingId : bookingIds)
        {
            bookingList.add(bookingMap.get(bookingId));
        }
        return  bookingList;
    }

    public Booking updateBooking(@NonNull String id, @NonNull Booking bookingDetails) {
        if(!bookingMap.containsKey(id))
        {
            // todo : BookingNOTFOUNDEXCEPTION
        }


        Booking booking = bookingMap.get(id);
        booking.setOrganizer(bookingDetails.getOrganizer());
        booking.setStartTime(bookingDetails.getStartTime());
        booking.setEndTime(bookingDetails.getEndTime());
        booking.setAudience(bookingDetails.getAudience());
        bookingMap.put(id,booking);
        // Todo : Update availability
        return booking;
    }

    public void deleteBooking(String id) {
        bookingMap.remove(id);
        // Todo : Update availability
    }




    private String book(MeetingBookingRequest request)
    {
            Long startDate = request.getStartDate();
            Long startTime = Long.valueOf(request.getStartTime());
            Long endTime = Long.valueOf(request.getEndTime());
            Long endDate = request.getEndDate();

            switch (request.getRecurrenceType())
            {
                case NONE ->{
                    if(userAvailabilityService.isSlotAvailable(request.getOrganizer(), startDate+startTime,
                            startDate + endTime))
                    {
                        userAvailabilityService.updateAvailability(request.getOrganizer(), startDate+startTime,
                                startDate + endTime);

                        Booking booking = new Booking(request.getOrganizer(),
                                startDate+startTime,
                                startDate+endTime,request.getAudience(), null);
                        bookingMap.put(booking.getBookingId(), booking);

                        //update recurringmap
                        if(recurringBookingMap.containsKey(booking.getLinkedBookingId()))
                        {
                            List<String > bookingIds = recurringBookingMap.get(booking.getLinkedBookingId());
                            bookingIds.add(booking.getBookingId());
                            recurringBookingMap.put(booking.getLinkedBookingId(), bookingIds);
                        }
                        else
                        {
                            List<String > bookingIds  = new ArrayList<>();
                            bookingIds.add(booking.getBookingId());
                            recurringBookingMap.put(booking.getLinkedBookingId(), bookingIds);
                        }

                        //update  usermap
                        if(userBookingMap.containsKey(booking.getOrganizer()))
                        {
                            List<String > bookingIds = userBookingMap.get(booking.getOrganizer());
                            bookingIds.add(booking.getBookingId());
                            userBookingMap.put(booking.getLinkedBookingId(), bookingIds);
                        }
                        else
                        {
                            List<String > bookingIds  = new ArrayList<>();
                            bookingIds.add(booking.getBookingId());
                            userBookingMap.put(booking.getOrganizer(), bookingIds);
                        }

                        return booking.getBookingId();
                    }

                }

                case WEEKLY -> {
                    Long weeklyEpoch =Long.valueOf(7* 86400000);
                    Long tempStartDate = startDate;
                    String linkedBookingId = null;
                    while(endDate <= startDate)
                    {

                        if(userAvailabilityService.isSlotAvailable(request.getOrganizer(), startDate+startTime,
                                startDate + endTime))
                        {
                            userAvailabilityService.updateAvailability(request.getOrganizer(), startDate+startTime,
                                    startDate + endTime);
                            Booking booking = new Booking(request.getOrganizer(),
                                    startDate+startTime,startDate+endTime,request.getAudience(),linkedBookingId);
                            bookingMap.put(booking.getBookingId(), booking);

                            //update recurringmap
                            if(recurringBookingMap.containsKey(booking.getLinkedBookingId()))
                            {
                                List<String > bookingIds = recurringBookingMap.get(booking.getLinkedBookingId());
                                bookingIds.add(booking.getBookingId());
                                recurringBookingMap.put(booking.getLinkedBookingId(), bookingIds);
                            }
                            else
                            {
                                List<String > bookingIds  = new ArrayList<>();
                                bookingIds.add(booking.getBookingId());
                                recurringBookingMap.put(booking.getLinkedBookingId(), bookingIds);
                            }

                            //update  usermap
                            if(userBookingMap.containsKey(booking.getOrganizer()))
                            {
                                List<String > bookingIds = userBookingMap.get(booking.getOrganizer());
                                bookingIds.add(booking.getBookingId());
                                userBookingMap.put(booking.getLinkedBookingId(), bookingIds);
                            }
                            else
                            {
                                List<String > bookingIds  = new ArrayList<>();
                                bookingIds.add(booking.getBookingId());
                                userBookingMap.put(booking.getOrganizer(), bookingIds);
                            }
                            linkedBookingId = booking.getLinkedBookingId();
                        }
                        tempStartDate +=weeklyEpoch;
                    }
                    return linkedBookingId;
                }
                case MONTHLY -> {
                    //TOdo: add monthly
                }
                case YEARLY -> {
                    //TOdo: add yearly
                }
            }

//Handle using NO_SLOT_AVAILABLE_EXCEPTION
        return null;
    }
}

