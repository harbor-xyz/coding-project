package com.harbor.calendly.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.harbor.calendly.exceptions.UserCreationException;
import com.harbor.calendly.exceptions.UserValidationException;
import com.harbor.calendly.models.User;
import com.harbor.calendly.respositories.UserMongoRepo;
import com.harbor.calendly.validators.UserValidator;

import io.micrometer.core.instrument.MeterRegistry;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserMongoRepo repo;

    @Autowired
    private UserValidator validator;

    @Autowired
    private MeterRegistry registry;

    public User createUser(User userToBeCreated) throws UserValidationException, UserCreationException {
        logger.debug("Creating user in service {}", userToBeCreated);
        validator.validateUser(userToBeCreated);
        User check = repo.findOneByEmail(userToBeCreated.getEmail());
        if (null != check) {
            logger.warn("User already exists with email. {}", userToBeCreated.getEmail());
            registry.counter("userRegistration", "type", "old").increment();
            return check;
        }
        try {
            userToBeCreated = repo.save(userToBeCreated);
        } catch (Exception e) {
            logger.error("Error creating database entry. {}", e.getMessage(), e);
            throw new UserCreationException("Unable to create user. Please re-try after sometime.");
        }
        registry.counter("userRegistration", "type", "new").increment();
        return userToBeCreated;
    }
}
