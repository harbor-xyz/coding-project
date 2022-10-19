package com.harbor.calendly.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.harbor.calendly.exceptions.UserCreationException;
import com.harbor.calendly.exceptions.UserValidationException;
import com.harbor.calendly.models.Response;
import com.harbor.calendly.models.User;
import com.harbor.calendly.service.UserService;

@RestController
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;
    
    @PostMapping("/user")
    public ResponseEntity<Response> createUser(@RequestBody User user) {
        logger.info("Creating user: {}", user);
        try {
            user = userService.createUser(user);
        } catch (UserCreationException | UserValidationException exception) {
            logger.error("Unable to create user because {}", exception.getMessage(), exception);
            return new ResponseEntity<>(
                    new Response(false, "Unable to create user. " + exception.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(new Response(true, user), HttpStatus.CREATED);
    }
}