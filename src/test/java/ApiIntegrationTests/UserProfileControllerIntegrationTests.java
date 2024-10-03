package ApiIntegrationTests;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.p0.calendly.CalendlyApplication;
import org.p0.calendly.models.User;
import org.p0.calendly.utils.RestTemplateClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = CalendlyApplication.class)

public class UserProfileControllerIntegrationTests {

    @LocalServerPort
    private int port;


    private String baseUrl;

    @Autowired
    private RestTemplateClient restTemplate;

    @BeforeEach
    public void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/user";
    }

    // Test Create User
    @Test
    public void createUser_ShouldReturn201_WhenUserIsCreatedSuccessfully() {
        User newUser = new User(null, "John", "Doe", "john.doe@example.com");

        ResponseEntity<User> responseEntity = restTemplate.postForEntity(baseUrl, newUser, User.class);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertNotNull(responseEntity.getBody().getId());
        assertEquals("John", responseEntity.getBody().getFirstName());

    }

    @Test
    public void createUser_ShouldReturn409_WhenUserAlreadyExists() {
        // First create a user
        User newUser = new User(null, "Elizabeth", "Swann", "elizabeth.swann@example.com");
        ResponseEntity<User> response = restTemplate.postForEntity(baseUrl, newUser, User.class);
        String id = response.getBody().getId().toString();

        // Try to create the same user again
        User sameUser = new User(id, "Elizabeth", "Swann", "elizabeth.swann@example.com");
        try {
            restTemplate.postForEntity(baseUrl, sameUser, User.class);
        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        }
    }

    @Test
    public void getUserById_ShouldReturn200_WhenUserExists() {
        // First create a user
        User newUser = new User(null, "Jane", "Doe", "jane.doe@example.com");
       ResponseEntity<User> response = restTemplate.postForEntity(baseUrl, newUser, User.class);

       String id = response.getBody().getId().toString();
        // Now get the user by ID
        ResponseEntity<User> responseEntity = restTemplate.getForEntity(baseUrl + "/{id}", User.class, id);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("Jane", responseEntity.getBody().getFirstName());
    }

    @Test
    public void updateUser_ShouldReturn200_WhenUserIsUpdatedSuccessfully() {
        // First create a user
        User newUser = new User(null, "Jack", "Sparrow", "jack.sparrow@example.com");
        ResponseEntity<User> response = restTemplate.postForEntity(baseUrl, newUser, User.class);
        String id = response.getBody().getId().toString();

        // Update user details
        User updatedUser = new User(id, "Captain", "Sparrow", "captain.sparrow@example.com");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> entity = new HttpEntity<>(updatedUser, headers);

        ResponseEntity<User> responseEntity = restTemplate.exchange(baseUrl + "/{id}", HttpMethod.PUT, entity, User.class, id);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("Captain", responseEntity.getBody().getFirstName());
    }

    @Test
    public void deleteUser_ShouldReturn204_WhenUserIsDeletedSuccessfully() {
        // First create a user
        User newUser = new User(null, "Will", "Turner", "will.turner@example.com");
        ResponseEntity<User> response = restTemplate.postForEntity(baseUrl, newUser, User.class);
        String id = response.getBody().getId().toString();

        // Delete the user
        restTemplate.delete(baseUrl + "/{id}", id);

        try {
            restTemplate.getForEntity(baseUrl + "/{id}", User.class, "3");
        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }

    }
}