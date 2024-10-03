package org.p0.calendly.controllers;


import lombok.NonNull;
import org.p0.calendly.exceptions.UserAlreadyExistsException;
import org.p0.calendly.exceptions.UserNotFoundException;
import org.p0.calendly.models.User;
import org.p0.calendly.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
public class UserProfileController {


    @Autowired
    private UserService userService;

    // Create user
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody @NonNull User user) {

        User response = null;
        try {
            response=  userService.createUser(user);
        }
        catch (UserAlreadyExistsException e)
        {
            //status 409 : conflict
            return ResponseEntity.status(409).body(response);
        }
        return ResponseEntity.status(201).body(response);
     }

    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable @NonNull String id) {
        User response = null;
        try {
            response=  userService.getUserById(id);
        }
        catch (UserNotFoundException e)
        {
            //status 404 : not found
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.status(200).body(response);

    }

    // Update user
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable @NonNull String id, @RequestBody @NonNull User userDetails) {
        User response = null;

        try {
            response=  userService.updateUser(id, userDetails);
        }
        catch (UserNotFoundException e)
        {
            //status 404 : conflict
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.ok(response);
    }

    // Delete user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
