package com.harbor.calendly.respositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import com.harbor.calendly.models.CalendlyEvent;

@Service
public interface EventRepo extends MongoRepository<CalendlyEvent, String> {
    public CalendlyEvent findOneByEventNameAndUserId(String eventName, String userId);

    public List<CalendlyEvent> findAllByUserId(String userId);

    public List<CalendlyEvent> findAllByUserIdAndEventStartDateTimeLessThanAndEventEndDateTimeGreaterThan(String userId,
            long currentTime, long curretTime1);

    public CalendlyEvent findOneById(String eventId);
}
