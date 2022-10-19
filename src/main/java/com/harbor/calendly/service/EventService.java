package com.harbor.calendly.service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.harbor.calendly.exceptions.InvalidEventBookException;
import com.harbor.calendly.exceptions.InvalidEventException;
import com.harbor.calendly.models.CalendarSlot;
import com.harbor.calendly.models.CalendlyEvent;
import com.harbor.calendly.models.DOWSlot;
import com.harbor.calendly.models.DaySlot;
import com.harbor.calendly.models.EventBookRequest;
import com.harbor.calendly.models.SlotType;
import com.harbor.calendly.models.TimeType;
import com.harbor.calendly.models.User;
import com.harbor.calendly.respositories.EventBookRepo;
import com.harbor.calendly.respositories.EventRepo;
import com.harbor.calendly.respositories.UserMongoRepo;
import com.harbor.calendly.utils.CommonUtils;
import com.harbor.calendly.validators.EventValidator;

import io.micrometer.core.instrument.MeterRegistry;

@Service
public class EventService {
    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    @Autowired
    private EventRepo repo;

    @Autowired
    private UserMongoRepo userRepo;

    @Autowired
    private EventValidator validator;

    @Autowired
    private EventBookRepo eventBookRepo;

    @Autowired
    private MeterRegistry registry;

    public List<CalendlyEvent> getAllEventForUser(String userId) throws InvalidEventException {
        Optional<User> user = userRepo.findById(userId);
        if (!user.isPresent()) {
            throw new InvalidEventException("UserID: {} doesn't exists");
        }
        return repo.findAllByUserIdAndEventStartDateTimeLessThanAndEventEndDateTimeGreaterThan(
                userId, System.currentTimeMillis() / 1000, System.currentTimeMillis() / 1000);
    }

    public CalendlyEvent getCalendlyEventById(String eventId) {
        return repo.findOneById(eventId);
    }

    public CalendlyEvent createEvent(CalendlyEvent event) throws InvalidEventException {
        validator.validateEvent(event);
        logger.debug("Validating the event if already exists");
        CalendlyEvent check = repo.findOneByEventNameAndUserId(event.getEventName(), event.getUserId());
        if (null != check) {
            throw new InvalidEventException("This event already exists. Can't create a new one.");
        }
        if ((event.getFreeDays() == null || event.getFreeDays().isEmpty()) &&
                (event.getFreeSlots() == null || event.getFreeSlots().isEmpty())) {
            logger.info("Found empty slots. Setting to default");
            event.setFreeSlots(CommonUtils.getPlatformDOWSlots());
            logger.info("Default set to : {}", event.getFreeSlots());
        }
        event = repo.save(event);
        registry.counter("eventCreated", "userId", event.getUserId()).increment();
        return event;
    }

    public EventBookRequest bookSlot(EventBookRequest request) throws InvalidEventBookException {
        CalendlyEvent event = repo.findOneById(request.getEventId());
        if (null == event) {
            throw new InvalidEventBookException("Given eventId doesn't exist");
        }
        validator.validateBookEvent(event, request);

        checkOverlappingWithBookedDays(event, request);

        boolean canBook = checkIfAvailableOnFreeDays(event, request);

        if (canBook) {
            request = bookEventAndUpdateSlots(event, request);
            return request;
        }

        OffsetDateTime startDateTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(request.getSlotStartTime()),
                ZoneOffset.UTC);
        OffsetDateTime endDateTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(request.getSlotEndTime()),
                ZoneOffset.UTC);

        checkOverlappingWithBookedSlots(startDateTime, endDateTime, event, request);

        canBook = checkIfAvailableOnFreeSlots(startDateTime, endDateTime, event, request);

        if (canBook) {
            request = bookEventAndUpdateSlots(event, request);
            return request;
        } else {
            throw new InvalidEventBookException("No free slot available. Outside of provided window");
        }
    }

    public EventBookRequest bookEventAndUpdateSlots(CalendlyEvent event, EventBookRequest request) {
        CalendarSlot bookedCS = new CalendarSlot();
        bookedCS.setSlotType(SlotType.BUSY);
        bookedCS.setTimeType(TimeType.EPOCH);
        bookedCS.setStartTime(request.getSlotStartTime());
        bookedCS.setEndTime(request.getSlotEndTime());
        if (null == event.getBookedDays()) {
            event.setBookedDays(new ArrayList<>());
        }
        DaySlot daySlot = new DaySlot();
        List<CalendarSlot> calendarSlots = new ArrayList<>();
        calendarSlots.add(bookedCS);
        daySlot.setBookedSlots(calendarSlots);
        event.getBookedDays().add(daySlot);
        repo.save(event);
        request = eventBookRepo.save(request);
        registry.counter("eventBooked", "eventId", event.getId()).increment();
        return request;
    }

    public boolean checkIfAvailableOnFreeSlots(OffsetDateTime startDateTime, OffsetDateTime endDateTime,
            CalendlyEvent event, EventBookRequest request) {
        List<DOWSlot> freeDOWs = event.getFreeSlots();
        if (null != freeDOWs && !freeDOWs.isEmpty()) {
            for (DOWSlot dowSlot : freeDOWs) {
                if (dowSlot.getDow().toString().equals(startDateTime.getDayOfWeek().toString())) {
                    List<CalendarSlot> freeCalendarSlots = dowSlot.getFreeSlots();
                    if (null != freeCalendarSlots && !freeCalendarSlots.isEmpty()) {
                        for (CalendarSlot cs : freeCalendarSlots) {
                            int startSeconds = startDateTime.getHour() * 60 * 60 + startDateTime.getMinute() * 60
                                    + startDateTime.getSecond();
                            int endSeconds = endDateTime.getHour() * 60 * 60 + endDateTime.getMinute() * 60
                                    + endDateTime.getSecond();
                            if ((startSeconds >= cs.getStartTime()
                                    && endSeconds <= cs.getEndTime())) {
                                logger.info("Able to booked slot on week. Event : {}", event);
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public void checkOverlappingWithBookedSlots(OffsetDateTime startDateTime, OffsetDateTime endDateTime,
            CalendlyEvent event, EventBookRequest request) throws InvalidEventBookException {
        List<DOWSlot> bookedDOWs = event.getBookedSlots();
        if (null != bookedDOWs && !bookedDOWs.isEmpty()) {
            for (DOWSlot dowSlot : bookedDOWs) {
                if (dowSlot.getDow().toString().equals(startDateTime.getDayOfWeek().toString())) {
                    List<CalendarSlot> bookedCalendarSlots = dowSlot.getBookedSlots();
                    if (null != bookedCalendarSlots && !bookedCalendarSlots.isEmpty()) {
                        for (CalendarSlot cs : bookedCalendarSlots) {
                            int startSeconds = startDateTime.getHour() * 60 * 60 + startDateTime.getMinute() * 60
                                    + startDateTime.getSecond();
                            int endSeconds = endDateTime.getHour() * 60 * 60 + endDateTime.getMinute() * 60
                                    + endDateTime.getSecond();
                            if ((startSeconds >= cs.getStartTime()
                                    && startSeconds < cs.getEndTime())
                                    || (endSeconds >= cs.getStartTime()
                                            && endSeconds < cs.getEndTime())) {
                                registry.counter("eventBookedFailed", "eventId", event.getId()).increment();
                                throw new InvalidEventBookException("Booking time conflict because of bookedDOWs");
                            }
                        }
                    }
                }
            }
        }
    }

    public void checkOverlappingWithBookedDays(CalendlyEvent event, EventBookRequest request)
            throws InvalidEventBookException {
        List<DaySlot> bookedDays = event.getBookedDays();
        if (null != bookedDays && !bookedDays.isEmpty()) {
            for (DaySlot daySlot : bookedDays) {
                List<CalendarSlot> bookedSlots = daySlot.getBookedSlots();
                if (null != bookedSlots && !bookedSlots.isEmpty()) {
                    for (CalendarSlot cs : bookedSlots) {
                        if ((request.getSlotStartTime() >= cs.getStartTime()
                                && request.getSlotStartTime() < cs.getEndTime())
                                || (request.getSlotEndTime() >= cs.getStartTime()
                                        && request.getSlotEndTime() < cs.getEndTime())) {
                            registry.counter("eventBookedFailed", "eventId", event.getId()).increment();
                            throw new InvalidEventBookException("Booking time conflict because of booked Days");
                        }
                    }
                }
            }
        }
    }

    public boolean checkIfAvailableOnFreeDays(CalendlyEvent event, EventBookRequest request) {
        List<DaySlot> freeDays = event.getFreeDays();
        if (null != freeDays && !freeDays.isEmpty()) {
            for (DaySlot daySlot : freeDays) {
                List<CalendarSlot> freeSlots = daySlot.getFreeSlots();
                if (null != freeSlots && !freeSlots.isEmpty()) {
                    for (CalendarSlot cs : freeSlots) {
                        if ((request.getSlotStartTime() >= cs.getStartTime()
                                && request.getSlotEndTime() <= cs.getEndTime())) {
                            logger.info("Able to booked slot on day");
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
