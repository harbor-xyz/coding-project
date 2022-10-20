package com.harbor.calendly.validators;

import org.springframework.stereotype.Service;

import com.harbor.calendly.exceptions.UserValidationException;
import com.harbor.calendly.models.User;

@Service
public class UserValidator {

    public boolean validateUser(User user) throws UserValidationException {
        if (null == user.getEmail() || user.getEmail().isEmpty()) {
            throw new UserValidationException("Email is mandatory field");
        }
        if (null == user.getMobileNumber() || user.getMobileNumber().length() > 12) {
            throw new UserValidationException("Invalid Mobile Number");
        }
        if (null == user.getName() || user.getName().isEmpty() || user.getName().length() > 100) {
            throw new UserValidationException("Invalid name provided");
        }
        return true;
    }
}
