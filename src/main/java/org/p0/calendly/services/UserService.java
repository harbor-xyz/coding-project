package org.p0.calendly.services;

import org.p0.calendly.exceptions.UserAlreadyExistsException;
import org.p0.calendly.exceptions.UserNotFoundException;
import org.p0.calendly.models.User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {
    private Map<String, User> userMap = new HashMap<>();

    // Create a new user
    public User createUser(User user) {
        if(user.getId() == null || user.getId().isEmpty())
            user.setUniqueId();

        if(userMap.containsKey(user.getId()))
        {
            throw new UserAlreadyExistsException();
        }

        userMap.put(user.getId(),user);

        return user;
    }

    // Retrieve a single user by ID
    public User getUserById(String id) {
        if(userMap.containsKey(id))
            return userMap.get(id);

        throw new UserNotFoundException();

    }

    // Update user details
    public User updateUser(String id, User userDetails) {
        if(!userMap.containsKey(id))
        {
            throw new UserNotFoundException();
        }


        User user = userMap.get(id);
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setEmail(userDetails.getEmail());
        userMap.put(user.getId(),user);
        return user;
    }

    // Delete user by ID
    public void deleteUser(String id) {
        userMap.remove(id);
    }
}