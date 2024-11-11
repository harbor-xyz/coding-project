# Steps to install and run the project

1. Ensure you have Ruby and Rails installed on your system. This project was built using Ruby 3.3.4 and Rails 7.2.2.

2. Clone the repository.

3. Navigate to the project directory:
   ```
   cd coding-project
   ```

4. Install the required gems:
   ```
   bundle install
   ```

   4a. Set up credentials:
   ```bash
   # Generate a new credentials file and master key
   rails credentials:edit
   ```

   This will create:

   ```
   - config/credentials.yml.enc
   - config/master.key
   ```

   Note: Keep master.key secure and never commit it to the repository


5. Create and migrate the database:
   ```
   rails db:create
   rails db:migrate
   rails db:seed # To seed timezone entries
   ```

6. Start the Rails server:
   ```
   rails s
   ```

The application should now be running at `http://localhost:3000`.

# API Endpoints

## Users

- GET `/users` - List all users
- GET `/users/:id` - Get user details
- POST `/users` - Create a new user
- PUT/PATCH `/users/:id` - Update a user
- DELETE `/users/:id` - Delete a user
- GET `/users/:id/availabilities` - Get user's availabilities
- POST `/users/:id/set_availability` - Set availability for a user
- GET `/users/:id/find_overlap/:other_user_id` - Find overlapping availabilities between two users

## Timezones

- GET `/timezones` - List all timezones

## Availabilities
- GET `/availabilities` - List all availabilities
- GET `/availabilities/:id` - Get availability details
- POST `/availabilities` - Create a new availability
- PUT/PATCH `/availabilities/:id` - Update an availability
- DELETE `/availabilities/:id` - Delete an availability

## Events

- GET `/events` - List all events
- GET `/events/:id` - Get event details
- POST `/events` - Create a new event
- PUT/PATCH `/events/:id` - Update an event
- DELETE `/events/:id` - Delete an event
- POST `/events/:id/invite` - Invite a user to an event
- POST `/schedule_meeting/:user_id` - Schedule a meeting with a user

## Invitations

- GET `/invitations` - List all invitations
- GET `/invitations/:id` - Get invitation details
- POST `/invitations` - Create a new invitation
- PUT/PATCH `/invitations/:id` - Update an invitation
- DELETE `/invitations/:id` - Delete an invitation

## Notifications

- GET `/notifications` - List all notifications
- PUT/PATCH `/notifications/:id` - Update a notification

# Deploy the project on [Render](https://render.com).

Follow steps mentioned in [official render rails docs](https://docs.render.com/deploy-rails)

<details>
<summary>Sample API for POST Requests/Responses</summary>

## Create a User
POST `/users`

Request:
```json
{
  "user": {
    "name": "Rajendra Kadam",
    "email": "rajendrakadam249@gmail.com",
    "timezone_id": 18
  }
}
```

Response:
```json
{
	"id": 1,
	"name": "Raj Kadam",
	"email": "rajendrakadam249@gmail.com",
	"created_at": "2024-11-08T12:07:55.644Z",
	"updated_at": "2024-11-08T12:07:55.644Z",
	"timezone_id": 18
}
```

## Set Availability
POST `/users/1/set_availability`

Request:
```json
{
  "availability": {
    "start_time": "2024-11-08T09:00:00Z",
    "end_time": "2024-11-08T17:00:00Z",
    "timezone_id": 18
  }
}
```

Response:
```json
{
  "id": 1,
  "user_id": 1,
  "start_time": "2024-11-08T09:00:00Z",
  "end_time": "2024-11-08T17:00:00Z",
  "timezone_id": 18,
  "created_at": "2024-11-08T12:07:55.644Z",
  "updated_at": "2024-11-08T12:07:55.644Z",
}
```

## Schedule a Meeting
POST `/schedule_meeting/1`

Request:
```json
{
  "title": "Project Review",
  "description": "Weekly project status review",
  "start_time": "2024-11-08T10:00:00Z",
  "duration": 60,
  "organizer_name": "Raj Kadam",
  "organizer_email": "rajkadam@gmail.com"
}
```

Response:
```json
{
  "id": 1,
  "title": "Project Review",
  "description": "Weekly project status review",
  "start_time": "2024-11-08T10:00:00Z",
  "end_time": "2024-11-08T11:00:00Z",
  "duration": 60,
  "timezone_id": 18,
  "organizer_name": "Raj Kadam",
  "organizer_email": "rajkadam@gmail.com",
  "created_at": "2024-11-08T12:07:55.644Z",
  "updated_at": "2024-11-08T12:07:55.644Z",
}
```

## Find Overlapping Availabilities
GET `/users/1/find_overlap/2`

Response:
```json
[
  {
    "start_time": "2024-11-08T10:00:00Z",
    "end_time": "2024-11-08T15:00:00Z"
  }
]
```

## Delete Event
DELETE `/events/1`

Response: HTTP 204 No Content

</details>

<details>
<summary>Project Assumptions & Design Decisions</summary>

## Time-Related Assumptions
- All times are stored in UTC for consistency across timezones.
- Duration is stored in minutes for simplicity and human readability.

## Availability Management
- Users can have multiple non-overlapping availability slots.
- When an event is scheduled, it intelligently splits or consumes the availability slot.
- Adjacent availability slots are automatically adjusted when restored for cleanup.
- Availability can be split into multiple slots when partially consumed, preserving the remaining time slots.

## Event Handling
- Events have a fixed duration that doesn't change once set.
- Each event requires basic organizer info (name and email) for contact purposes.
- Events can have multiple attendees through invitations.
- Double-booking prevention: events cannot overlap for the same user.
- End time is automatically calculated based on duration and start time.

## Invitation System
- Simple invitation state machine: pending → accepted/declined.
- Accepting an invitation automatically consumes the user's availability.
- Availability is automatically restored when invitation/event is deleted.
- One user can't have multiple invitations to the same event (prevents duplicates).
- No emails are sent for invitations yet.

## User Management
- Users require name and unique email for identification.
- Timezone association is mandatory for proper time handling but it is not used anywhere even if supplied in interest of time.
- Users can manage multiple availabilities and invitations.
- All user-related operations maintain data integrity through transactions.

## Timezone Handling
- Timezones are predefined entities with name and offset.
- All time-based entities (availability, event, user) must specify timezone.
- Timezone names must be unique for consistency.

## Data Integrity
- All availability modifications are wrapped in transactions.
- Event deletion triggers automatic availability restoration.
- Cascading deletes handle cleanup of dependent relationships.

## API Design Choices
- JSON responses for modern API compatibility.
- RESTful conventions for predictable endpoints.
- No authentication (MVP approach).
- No rate limiting (can be added later).
- Consistent error responses with appropriate HTTP status codes.

## Database Design
- Foreign key constraints ensure referential integrity.
- Email uniqueness enforced at database level.
- Dependent relationships use cascading deletes.
- Using SQLite for simplicity (can be upgraded to PostgreSQL/MySQL).

</details>

---

# Harbor Take Home Project

Welcome to the Harbor take home project. We hope this is a good opportunity for you to showcase your skills.

## The Challenge

Build us a REST API for calendly. Remember to support

- Setting own availability
- Showing own availability
- Finding overlap in schedule between 2 users

It is up to you what else to support.

## Expectations

We care about

- Have you thought through what a good MVP looks like? Does your API support that?
- What trade-offs are you making in your design?
- Working code - we should be able to pull and hit the code locally. Bonus points if deployed somewhere.
- Any good engineer will make hacks when necessary - what are your hacks and why?

We don't care about

- Authentication
- UI
- Perfection - good and working quickly is better

It is up to you how much time you want to spend on this project. There are likely diminishing returns as the time spent goes up.

## Submission

Please fork this repository and reach out to Prakash when finished.

## Next Steps

After submission, we will conduct a 30 to 60 minute code review in person. We will ask you about your thinking and design choices.
