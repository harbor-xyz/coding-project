package ApiIntegrationTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.p0.calendly.CalendlyApplication;
import org.p0.calendly.dtos.Availabilityrequest;
import org.p0.calendly.dtos.ScheduleOverlapResponse;
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

public class OverlapControllerIntegrationTests {

    @LocalServerPort
    private int port;


    private String userBaseUrl, availabilityBaseUrl, overlapBaseUrl;
    private String user1Id, user2Id, user3Id;

    @Autowired
    private RestTemplateClient restTemplate;

    @BeforeEach
    public void setUp() {
        availabilityBaseUrl = "http://localhost:" + port + "/api/v1/availability";
        userBaseUrl = "http://localhost:" + port + "/api/v1/user";
        overlapBaseUrl = "http://localhost:" + port + "/api/v1/overlap";

        // refractor setup method to use TESTUTILS for creating user and schedule

        //create user1
        User user1 = new User(null, "Elizabeth", "Swann", "elizabeth.swann@example.com");
        ResponseEntity<User> response1 = restTemplate.postForEntity(userBaseUrl, user1, User.class);
        user1Id = response1.getBody().getId().toString();

        // First create availability for a user
        // start date 1704844800000L = 2024/01/10 00:00:00 UTC
        //end date 1704931200000L = 2024/01/11 00:00:00 UTC
        Schedule schedule1 = new Schedule(1704844800000L, 4800000, 8400000, RecurrenceType.NONE, 1704931200000L);
        List<Schedule> scheduleList1 = Arrays.asList(schedule1);
        //Availabilityrequest availabilityRequest = new Availabilityrequest(user1Id, scheduleList);
        restTemplate.postForEntity(availabilityBaseUrl, new Availabilityrequest(user1Id, scheduleList1), Void.class);



        //create user2
        User user2 = new User(null, "Amit", "G", "a.g@example.com");
        ResponseEntity<User> response2 = restTemplate.postForEntity(userBaseUrl, user2, User.class);
        user2Id = response2.getBody().getId().toString();

        // First create availability for a user
        // start date 1704844800000L = 2024/01/10 00:00:00 UTC
        //end date 1704931200000L = 2024/01/11 00:00:00 UTC
        Schedule schedule2 = new Schedule(1704844800000L, 3600000, 7200000, RecurrenceType.NONE, 1704931200000L);
        List<Schedule> scheduleList2 = Arrays.asList(schedule2);
        //Availabilityrequest availabilityRequest = new Availabilityrequest(id, scheduleList);
        restTemplate.postForEntity(availabilityBaseUrl, new Availabilityrequest(user2Id, scheduleList2), Void.class);



        //create user3
        User user3 = new User(null, "Ekan", "S", "e.s@example.com");
        ResponseEntity<User> response3 = restTemplate.postForEntity(userBaseUrl, user3, User.class);
        user3Id = response3.getBody().getId().toString();

        // First create availability for a user
        // start date 1705708800000L = 2024/01/20 00:00:00 UTC
        //end date 1705795200000L = 2024/01/21 00:00:00 UTC
        Schedule schedule3 = new Schedule(1705708800000L, 3600000, 7200000, RecurrenceType.NONE, 1705795200000L);
        List<Schedule> scheduleList3 = Arrays.asList(schedule3);
        //Availabilityrequest availabilityRequest = new Availabilityrequest(id, scheduleList);
        restTemplate.postForEntity(availabilityBaseUrl, new Availabilityrequest(user3Id, scheduleList3), Void.class);

    }

    //    // Test Get User Availability with meaningful method name
    @Test
    public void getUserAvailability_ShouldReturn200_WhenUserExists() {
        // Setup the ScheduleOverlapRequest with user IDs
        String id = user1Id + "," + user2Id;

        // Now retrieve the availability for the user
        ResponseEntity<ScheduleOverlapResponse> responseEntity = restTemplate.getForEntity(overlapBaseUrl + "/ids={id}", ScheduleOverlapResponse.class, id);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(1, responseEntity.getBody().getOverlaps().size());
        assertEquals(1704849600000L, responseEntity.getBody().getOverlaps().firstEntry().getKey());
        assertEquals(1704852000000L, responseEntity.getBody().getOverlaps().firstEntry().getValue());
    }

    // Test for NotEnoughUsersException
    @Test
    public void getOverlaps_ShouldReturn400_WhenNotEnoughUsersProvided() {
        // Setup the ScheduleOverlapRequest with a single user (which should trigger NotEnoughUsersException)
        String id = user1Id ;

        try {
            // Now retrieve the availability for the user
            ResponseEntity<ScheduleOverlapResponse> responseEntity = restTemplate.getForEntity(overlapBaseUrl + "/ids={id}", ScheduleOverlapResponse.class, id);
        } catch (HttpClientErrorException ex) {
            // Validate that a 400 Bad Request is returned
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        }
    }

    @Test
    public void getUserAvailability_ShouldReturnEmpty_WhenNoOverlaps() {
        // Setup the ScheduleOverlapRequest with user IDs
        String id = user1Id + "," + user3Id;

        // Now retrieve the availability for the user
        ResponseEntity<ScheduleOverlapResponse> responseEntity = restTemplate.getForEntity(overlapBaseUrl + "/ids={id}", ScheduleOverlapResponse.class, id);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(0, responseEntity.getBody().getOverlaps().size());
    }
}