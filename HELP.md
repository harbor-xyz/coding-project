# Getting Started

This is a calendly kind of app, which allows users to create events, which can later be booked by the guests. This only contains API and a postman collection to simulate the flow.

### Reference Documentation

For running the application, you should have docker and docker-compose. If not, you can try and run below components for proper functioning of the app

1. Prometheus running at port 9090
2. Mongodb with proper credentials configured in application.properties

If you have docker and docker-compose, simply run

./setup.sh

### APIs

This has 5 different APIs which are there for demo of MVP.

1. Create a user.
2. Create an event for the registered user. This also includes defining availability slots.
3. Get an event information to book a free slot.
4. Book an event using the preferences of the user.
5. Get all details of the booked slots in the event.

Apart from the API, below are non-functional artifacts included with the same.

1. Integration test using Postman. This is reason, not included unit tests in MVP. 
2. This application also generates metrics in prometheus for better tracking of events like how many user registered. How many event created and how many event are booked. This also tracks lot of application metrics.
3. This uses complete dockerized setup, so that it can run in any container based environment like kubernetes, etc.



