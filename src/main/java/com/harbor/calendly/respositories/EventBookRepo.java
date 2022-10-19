package com.harbor.calendly.respositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import com.harbor.calendly.models.EventBookRequest;

@Service
public interface EventBookRepo extends MongoRepository<EventBookRequest, String> {

}
