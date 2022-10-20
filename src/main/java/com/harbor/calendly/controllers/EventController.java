package com.harbor.calendly.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.harbor.calendly.exceptions.InvalidEventBookException;
import com.harbor.calendly.exceptions.InvalidEventException;
import com.harbor.calendly.models.CalendlyEvent;
import com.harbor.calendly.models.EventBookRequest;
import com.harbor.calendly.models.Response;
import com.harbor.calendly.service.EventService;

@RestController
public class EventController {
    private static final Logger logger = LoggerFactory.getLogger(EventController.class);

    @Autowired
    private EventService eventService;

    @PostMapping("/event")
    public ResponseEntity<Response> createEvent(@RequestBody CalendlyEvent event,
            @RequestHeader("userId") String userId) {
        logger.info("Booking a calendly event {} for user {}", event, userId);
        event.setUserId(userId);
        try {
            event = eventService.createEvent(event);
        } catch (InvalidEventException ex) {
            logger.error("Invalid Event {}", event, ex);
            return new ResponseEntity<>(new Response(false, "Couldn't create event because " + ex.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(new Response(true, event), HttpStatus.CREATED);
    }

    @GetMapping("/event")
    public ResponseEntity<Response> getEventsForUser(@RequestHeader("userId") String userId) {
        logger.info("Get all calendly event for user {}", userId);
        List<CalendlyEvent> events;
        try {
            events = eventService.getAllEventForUser(userId);
        } catch (InvalidEventException ex) {
            logger.error("Invalid UserId {}", userId, ex);
            return new ResponseEntity<>(new Response(false, "Couldn't get event because " + ex.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(new Response(true, events), HttpStatus.OK);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<Response> getEventById(@PathVariable("eventId") String eventId) {
        logger.info("Get calendly event by eventId {}", eventId);
        CalendlyEvent event;
        event = eventService.getCalendlyEventById(eventId);
        return new ResponseEntity<>(new Response(true, event), HttpStatus.OK);
    }

    @PostMapping("/event/{eventId}/book")
    public ResponseEntity<Response> bookEvent(@RequestBody EventBookRequest request,
            @RequestParam(name = "sendEmail", defaultValue = "false") Boolean sendEmail) {
        try {
            eventService.bookSlot(request, sendEmail);
        } catch (InvalidEventBookException ex) {
            return new ResponseEntity<>(new Response(false, "Unable to book slot " + ex.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(new Response(true, request), HttpStatus.OK);
    }

}
