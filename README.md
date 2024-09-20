## This is my implementation of calendly with below-mentioned components, features, use cases, assumptions, future scopes, etc.

## Components
1. This is a spring boot application which is divided into controller(API layer), service(service layer), model(contains object/DTOs), datastore(in-memeroy DB). 
2. We have 2 entities i.e. User & Meeting, these entities in turn have 2 dedicated singleton datastores, 2 services and 2 controllers.
3. UserController has user CRUD APIs.
4. MeetingController has scheduleMeeting() API which is used to schedule a meeting between a requestor and a requestee.

## Features
1. Users can be created, deleted, read and modified(new available slots can be added).
2. Meetings can be scheduled between 2 people.
3. Exception handling is added at all the required places.

## APIs
1. Create User 
2. Delete User
3. Display all users
4. Display a specific user
5. Add available slots to an user
6. Schedule meeting between the requestor and the requestee.

## Assumptions:
1. A user will always call an API with only his userId as a requestor to schedule a meeting.
2. An in-memory singleton database is used.
3. Requestor will schedule a meet in his own available slot, if not then our pre-validation check throws an exception.
4. By default, all slots are unavailable for a new user.
5. Time format used is [yyyy-MM-dd HH:mm].

## Use Case
1. Create user1
2. Add available slots to user1 e.g. [2024-09-20 10:00 to 2024-09-20 10:30], [2024-09-20 12:00 to 2024-09-20 13:00], [2024-09-20 16:00 to 2024-09-20 16:45]  
3. Create user2
4. Add available slots to user1 e.g. [2024-09-20 10:00 to 2024-09-20 10:30], [2024-09-20 13:00 to 2024-09-20 14:00], [2024-09-20 16:00 to 2024-09-20 16:30]
5. user1 requests to schedule a meeting with user2 at [2024-09-20 10:00 to 2024-09-20 10:30]
