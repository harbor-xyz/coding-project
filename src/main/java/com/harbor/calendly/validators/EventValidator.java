package com.harbor.calendly.validators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.harbor.calendly.exceptions.InvalidEventBookException;
import com.harbor.calendly.exceptions.InvalidEventException;
import com.harbor.calendly.models.CalendlyEvent;
import com.harbor.calendly.models.EventBookRequest;

@Service
public class EventValidator {
    private static final Logger logger = LoggerFactory.getLogger(EventValidator.class);

    public boolean validateEvent(CalendlyEvent event) throws InvalidEventException {
        logger.debug("Validating the event {}", event);
        if (event.getEventName() == null || event.getEventName().isEmpty()) {
            throw new InvalidEventException("Event name can't be empty");
        }
        if (event.getUserId() == null || event.getUserId().isEmpty()) {
            throw new InvalidEventException("UserId for event can't be empty");
        }
        return true;
    }

    public boolean validateBookEvent(CalendlyEvent event, EventBookRequest request) throws InvalidEventBookException {
        if (event.getSlotDurationInMins() != ((request.getSlotEndTime() - request.getSlotStartTime()) / 60)) {
            throw new InvalidEventBookException("Event duration selected is not as per event preferences.");
        }
        if (event.getEventStartDateTime() >= request.getSlotStartTime()
                || event.getEventEndDateTime() <= request.getSlotEndTime()) {
            throw new InvalidEventBookException("Event book range is outside of preferences");
        }
        return true;
    }
}
