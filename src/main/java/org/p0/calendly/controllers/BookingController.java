package org.p0.calendly.controllers;

import lombok.NonNull;
import org.p0.calendly.dtos.*;
import org.p0.calendly.models.Booking;
import org.p0.calendly.services.BookingManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/book")
public class BookingController {


    @Autowired
    private BookingManagementService meetingManagementService;

    // create booking
    @PostMapping
    public ResponseEntity<String> createBooking(@RequestBody @NonNull MeetingBookingRequest meetingBookingRequest) {
        // todo: input validations
        String response = meetingManagementService.createBooking(meetingBookingRequest);

        return ResponseEntity.status(201).body(response);
    }

    // Get booking by bookingID
    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable @NonNull String id) {
        Booking response = null;
        // todo: input validations
            response=  meetingManagementService.getBookingById(id);;

        return ResponseEntity.status(200).body(response);
    }

    // Get booking by userId
    @GetMapping("/user/{id}")
    public ResponseEntity<List<Booking>> getBookingByuserId(@PathVariable @NonNull String id) {
        List<Booking> response;

        response=  meetingManagementService.getBookingByuserId(id);

        return ResponseEntity.status(200).body(response);
    }

    // Update booking by bookingId
    @PutMapping("/{id}")
    public ResponseEntity<Booking> updateBooking(@PathVariable @NonNull String id, @RequestBody @NonNull Booking bookingDetails) {
        Booking response = null;

        response=  meetingManagementService.updateBooking(id, bookingDetails);


        return ResponseEntity.ok(response);
    }

    // Delete booking by bookingId
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable String id) {
        meetingManagementService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }
}