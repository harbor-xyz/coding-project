package ApiIntegrationTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.p0.calendly.CalendlyApplication;
import org.p0.calendly.dtos.AvailabilityResponse;
import org.p0.calendly.dtos.Availabilityrequest;
import org.p0.calendly.models.Schedule;
import org.p0.calendly.models.User;
import org.p0.calendly.models.enums.RecurrenceType;
import org.p0.calendly.utils.RestTemplateClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
        import org.springframework.web.client.HttpClientErrorException;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = CalendlyApplication.class)

public class UserAvailabilityControllerIntegrationTests {

    @LocalServerPort
    private int port;


    private String userBaseUrl;
    private String availabilityBaseUrl;

    @Autowired
    private RestTemplateClient restTemplate;

    @BeforeEach
    public void setUp() {
        availabilityBaseUrl = "http://localhost:" + port + "/api/v1/availability";
        userBaseUrl = "http://localhost:" + port + "/api/v1/user";
    }

    // Test Create User Availability with meaningful method name
    @Test
    public void createUserAvailability_ShouldReturn201_WhenAvailabilityIsCreatedSuccessfully() {
        // create user first
        User newUser = new User(null, "Elizabeth", "Swann", "elizabeth.swann@example.com");
        ResponseEntity<User> response = restTemplate.postForEntity(userBaseUrl, newUser, User.class);
        String id = response.getBody().getId().toString();

        Schedule schedule = new Schedule(1696176000000L, 900, 1200, RecurrenceType.WEEKLY, 1696262400000L);
        List<Schedule> scheduleList = Arrays.asList(schedule);
        Availabilityrequest availabilityRequest = new Availabilityrequest(id, scheduleList);

        ResponseEntity<Void> responseEntity = restTemplate.postForEntity(availabilityBaseUrl, availabilityRequest, Void.class);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }

    // Test Create User Availability with User Not Found
    @Test
    public void createUserAvailability_ShouldReturn404_WhenUserNotFound() {
        Schedule schedule = new Schedule(1696176000000L, 900, 1200, RecurrenceType.WEEKLY, 1696262400000L);
        List<Schedule> scheduleList = Arrays.asList(schedule);
        Availabilityrequest availabilityRequest = new Availabilityrequest("nonexistent_id", scheduleList);

        try {
            restTemplate.postForEntity(availabilityBaseUrl, availabilityRequest, Void.class);

        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }
    }
//
//    // Test Get User Availability with meaningful method name
    @Test
    public void getUserAvailability_ShouldReturn200_WhenUserExists() {
        //create user
        User newUser = new User(null, "Elizabeth", "Swann", "elizabeth.swann@example.com");
        ResponseEntity<User> response = restTemplate.postForEntity(userBaseUrl, newUser, User.class);
        String id = response.getBody().getId().toString();

        // First create availability for a user
        // start date 1704844800000L = 2024/01/10 00:00:00 UTC
        //end date 1704931200000L = 2024/01/11 00:00:00 UTC
        Schedule schedule = new Schedule(1704844800000L, 3600000, 7200000, RecurrenceType.NONE, 1704931200000L);
        List<Schedule> scheduleList = Arrays.asList(schedule);
        Availabilityrequest availabilityRequest = new Availabilityrequest(id, scheduleList);
        restTemplate.postForEntity(availabilityBaseUrl, availabilityRequest, Void.class);

        // Now retrieve the availability for the user
        ResponseEntity<AvailabilityResponse> responseEntity = restTemplate.getForEntity(availabilityBaseUrl + "/{id}", AvailabilityResponse.class, id);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(1, responseEntity.getBody().getScheduleList().size());
        assertEquals(3600000, responseEntity.getBody().getScheduleList().get(0).getStartTime());
        assertEquals(RecurrenceType.NONE, responseEntity.getBody().getScheduleList().get(0).getRecurrenceType());
    }
//
//    // Test Get User Availability with User Not Found
    @Test
    public void getUserAvailability_ShouldReturn404_WhenUserNotFound() {
        try {
            restTemplate.getForEntity(availabilityBaseUrl + "/{id}", AvailabilityResponse.class, "nonexistent_id");
        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }
    }
}
