package org.p0.calendly.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.p0.calendly.exceptions.UserAlreadyExistsException;
import org.p0.calendly.exceptions.UserNotFoundException;
import org.p0.calendly.models.User;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;
    private User testUser;

    @BeforeEach
    void setUp() {
        userService = new UserService();
        testUser = new User(null, "John", "Doe", "john.doe@example.com");
    }

    @Test
    void testCreateUser() {
        User createdUser = userService.createUser(testUser);
        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());
        assertEquals("John", createdUser.getFirstName());
        assertEquals("Doe", createdUser.getLastName());
        assertEquals("john.doe@example.com", createdUser.getEmail());
    }

    @Test
    void testCreateUser_ThrowsUserAlreadyExistsException() {
        User sameUser = userService.createUser(testUser);
        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.createUser(sameUser);
        });
    }

    @Test
    void testGetUserById() {
        User user = userService.createUser(testUser);
        User fetchedUser = userService.getUserById(user.getId());
        assertNotNull(fetchedUser);
        assertEquals("John", fetchedUser.getFirstName());
    }

    @Test
    void testGetUserById_ThrowsUserNotFoundException() {
        assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById("999");
        });
    }

    @Test
    void testUpdateUser() {
        User user = userService.createUser(testUser);
        testUser.setFirstName("Jane");
        testUser.setLastName("Smith");
        testUser.setEmail("jane.smith@example.com");

        User updatedUser = userService.updateUser(user.getId(), testUser);

        assertNotNull(updatedUser);
        assertEquals("Jane", updatedUser.getFirstName());
        assertEquals("Smith", updatedUser.getLastName());
        assertEquals("jane.smith@example.com", updatedUser.getEmail());
    }

    @Test
    void testUpdateUser_ThrowsUserNotFoundException() {
        testUser.setFirstName("Jane");

        assertThrows(UserNotFoundException.class, () -> {
            userService.updateUser("99", testUser);
        });
    }

    @Test
    void testDeleteUser() {
        User user = userService.createUser(testUser);
        String userId = user.getId();
        userService.deleteUser(userId);

        assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(userId);
        });
    }
}