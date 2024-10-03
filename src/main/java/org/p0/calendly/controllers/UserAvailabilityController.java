package org.p0.calendly.controllers;


import lombok.NonNull;
import org.p0.calendly.dtos.AvailabilityResponse;
import org.p0.calendly.dtos.Availabilityrequest;
import org.p0.calendly.exceptions.UserAlreadyExistsException;
import org.p0.calendly.exceptions.UserNotFoundException;
import org.p0.calendly.models.User;
import org.p0.calendly.services.UserAvailabilityService;
import org.p0.calendly.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/availability")
public class UserAvailabilityController {


    @Autowired
    private UserAvailabilityService userAvailabilityService;

    // Create user availability
    @PostMapping
    public ResponseEntity<Void> createUserAvailability(@RequestBody @NonNull Availabilityrequest availabilityrequest) {

        User response = null;
        try {
            userAvailabilityService.createUserAvailabilty(availabilityrequest);
        }
        catch (UserNotFoundException e)
        {
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.status(201).build();
     }

    // Get user availability
    @GetMapping("/{id}")
    public ResponseEntity<AvailabilityResponse> getUserAvailability(@PathVariable @NonNull String id) {
        AvailabilityResponse response = new AvailabilityResponse();
        try {
            response=  userAvailabilityService.getUserAvailability(id);
        }
        catch (UserNotFoundException e)
        {
            return ResponseEntity.status(404).body(response);
        }
        return ResponseEntity.status(200).body(response);
    }
}
