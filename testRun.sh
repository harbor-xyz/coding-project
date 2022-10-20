#!/bin/bash

. utils.sh

export BASE_URL=http://localhost:8080

print_line

echo "This script will help simulate a run of the product. This will print the scenario and will execute the same with certain delay. It will terminate once done. Sit back, Relax and Go through the scenario. Starting in 3 seconds"

await_action

print_line

echo "Creating a user of calendly with name Sam. Consider Sam as employee of a consulting firm, who allows their client to select a 30 mins consulting slot"

await_action

print_line

userID=$(curl -s -X POST "$BASE_URL/user" -d '{ "name": "SAM","email": "sam.consultant@gmail.com","mobileNumber": "91902866608"}' -H 'content-type:application/json' | jq -r '.data.userId')

echo "User SAM is created successfully and userId of the same is: $userID"

echo "Now let's create a consulting event for Sam, who allow his clients to interact with him on Weekdays between 9:00 AM to 5:00 PM GMT for a 30 mins slot"

await_action

print_line

echo "Event Start Date $(date -d @1665792000)"
echo "Event End Date $(date -d @1667088000)"

eventId=$(curl -s -X POST "$BASE_URL/event" -H "userId: $userID" -H 'Content-Type: application/json' -d'{"eventName": "Harbor consulting hours", "eventStartDateTime" : 1665792000, "eventEndDateTime": 1667088000}' | jq -r '.data.id')

echo "Event is created successfully with id: $eventId"

echo "Now we will try 5 different scenarios for testing using 5 different guest customers"

echo "Let's assume there is a customer A, who wants to book a slot for Friday between 9:00 AM to 9:30 AM GMT (Happy Path)"

await_action

print_line

echo "Booking Start Date $(date -d @1666344600)"
echo "Booking End Date $(date -d @1666346400)"

curl -s -X POST "$BASE_URL/event/$eventId/book" -H "userId: $userId" -H 'Content-Type: application/json' -d "{\"eventId\": \"$eventId\", \"userId\": \"$userId\", \"slotStartTime\": 1666344600, \"slotEndTime\": 1666346400, \"userInformation\" : { \"name\" : \"Customer A\", \"email\": \"progix.21@gmail.com\", \"mobileNumber\" : \"9028686607\"  } }" | jq ''

print_line

await_action

echo "Now assume that Customer B books the same slot, This request should fail with proper error"

await_action

print_line

echo "Booking Start Date $(date -d @1666344600)"
echo "Booking End Date $(date -d @1666346400)"

curl -s -X POST "$BASE_URL/event/$eventId/book" -H "userId: $userId" -H 'Content-Type: application/json' -d "{\"eventId\": \"$eventId\", \"userId\": \"$userId\", \"slotStartTime\": 1666344600, \"slotEndTime\": 1666346400, \"userInformation\" : { \"name\" : \"Customer B\", \"email\": \"progix.21+1@gmail.com\", \"mobileNumber\" : \"9038686607\"  } }" | jq ''

print_line

await_action

echo "Now assume that Customer B books the slot just on the edge of the endTime of previous slot. It should succeed as default buffer time is 0"

await_action

print_line

echo "Booking Start Date $(date -d @1666346400)"
echo "Booking End Date $(date -d @1666348200)"

curl -s -X POST "$BASE_URL/event/$eventId/book" -H "userId: $userId" -H 'Content-Type: application/json' -d "{\"eventId\": \"$eventId\", \"userId\": \"$userId\", \"slotStartTime\": 1666346400, \"slotEndTime\": 1666348200, \"userInformation\" : { \"name\" : \"Customer B\", \"email\": \"progix.21+1@gmail.com\", \"mobileNumber\" : \"9038686607\"  } }" | jq ''

print_line

await_action

echo "Now assume that Customer C is trying to book a slot which is less than 30 mins lead time from now. This should fail"

await_action

print_line

echo "Booking Start Date $(date -d @1666260000)"
echo "Booking End Date $(date -d @1666261800)"

curl -s -X POST "$BASE_URL/event/$eventId/book" -H "userId: $userId" -H 'Content-Type: application/json' -d "{\"eventId\": \"$eventId\", \"userId\": \"$userId\", \"slotStartTime\": 1666260000, \"slotEndTime\": 1666261800, \"userInformation\" : { \"name\" : \"Customer B\", \"email\": \"progix.21+1@gmail.com\", \"mobileNumber\" : \"9038686607\"  } }" | jq ''

print_line

await_action

echo "Now assume a customer tries to book a slot on Weekend, which is not permitted"

await_action

print_line

echo "Booking Start Date $(date -d @1666454400)"
echo "Booking End Date $(date -d @1666456200)"

curl -s -X POST "$BASE_URL/event/$eventId/book" -H "userId: $userId" -H 'Content-Type: application/json' -d "{\"eventId\": \"$eventId\", \"userId\": \"$userId\", \"slotStartTime\": 1666454400, \"slotEndTime\": 1666456200, \"userInformation\" : { \"name\" : \"Customer C\", \"email\": \"progix.21+2@gmail.com\", \"mobileNumber\" : \"9038686607\"  } }" | jq ''

print_line

await_action

echo "Now we will create another event for same user Sam, but this time, imagine, he is a doctor, who sees patient only on Mon, Tue between 2:00 PM to 3:00 PM and booking slot is only for 15 mins. We will create this event for 1 Year duration"

await_action

print_line

echo "Event Start Date $(date -d @1665792000)"
echo "Event End Date $(date -d @1697992200)"

eventId=$(curl -s -X POST "$BASE_URL/event" -H "userId: $userID" -H 'Content-Type: application/json' -d'{"eventName": "Doctors appointment", "eventStartDateTime" : 1665792000, "eventEndDateTime": 1697992200, "freeSlots": [              {                  "dow": "MONDAY",                  "freeSlots": [                      {                          "slotId": null,                          "startTime": 50400,                          "endTime": 54000,                          "slotType": "FREE",                          "timeType": "ABS"                      }                  ],                  "bookedSlots": null              },              {                  "dow": "TUESDAY",                  "freeSlots": [                      {                          "slotId": null,                          "startTime": 50400,                          "endTime": 54000,                          "slotType": "FREE",                          "timeType": "ABS"                      }                  ],                  "bookedSlots": null              }          ], "slotDurationInMins": 15}' | jq -r '.data.id')

echo "Event is created successfully with id: $eventId"

echo "Now we will try 5 different scenarios for testing on this new event of the doctor"

await_action

print_line

echo "Imagine that patient A wants to book a slot for Monday for 15 mins (Happy Path)"

await_action

print_line

echo "Booking Start Date $(date -d @1666621800)"
echo "Booking End Date $(date -d @1666622700)"

curl -s -X POST "$BASE_URL/event/$eventId/book" -H "userId: $userId" -H 'Content-Type: application/json' -d "{\"eventId\": \"$eventId\", \"userId\": \"$userId\", \"slotStartTime\": 1666621800, \"slotEndTime\": 1666622700, \"userInformation\" : { \"name\" : \"Patient A\", \"email\": \"progix.21+2@gmail.com\", \"mobileNumber\" : \"9038686607\"  } }" | jq ''

print_line

await_action

echo "Now assume patient B wants to book the slot for 15 mins which is overlapping the valid time i.e. Tuesday but 2:50 PM"

print_line

await_action

echo "Booking Start Date $(date -d @1666709400)"
echo "Booking End Date $(date -d @1666710300)"

curl -s -X POST "$BASE_URL/event/$eventId/book" -H "userId: $userId" -H 'Content-Type: application/json' -d "{\"eventId\": \"$eventId\", \"userId\": \"$userId\", \"slotStartTime\": 1666709400, \"slotEndTime\": 1666710300, \"userInformation\" : { \"name\" : \"Patient B\", \"email\": \"progix.21+2@gmail.com\", \"mobileNumber\" : \"9038686607\"  } }" | jq ''


await_action

print_line

echo "Now assume patient C wants to book the slot for 15 mins but on a Wednesday, this should fail"

print_line

await_action

echo "Booking Start Date $(date -d @1666792800)"
echo "Booking End Date $(date -d @1666793700)"

curl -s -X POST "$BASE_URL/event/$eventId/book" -H "userId: $userId" -H 'Content-Type: application/json' -d "{\"eventId\": \"$eventId\", \"userId\": \"$userId\", \"slotStartTime\": 1666792800, \"slotEndTime\": 1666793700, \"userInformation\" : { \"name\" : \"Patient C\", \"email\": \"progix.21+2@gmail.com\", \"mobileNumber\" : \"9038686607\"  } }" | jq ''


await_action

print_line

echo "Now assume patient D wants to book the slot for 15 mins but after 10 months"

print_line

await_action

echo "Booking Start Date $(date -d @1692627300)"
echo "Booking End Date $(date -d @1692628200)"

curl -s -X POST "$BASE_URL/event/$eventId/book" -H "userId: $userId" -H 'Content-Type: application/json' -d "{\"eventId\": \"$eventId\", \"userId\": \"$userId\", \"slotStartTime\": 1692627300, \"slotEndTime\": 1692628200, \"userInformation\" : { \"name\" : \"Patient D\", \"email\": \"progix.21+2@gmail.com\", \"mobileNumber\" : \"9038686607\"  } }" | jq ''


await_action

print_line

echo "Now assume patient E wants to book the slot for 30 mins but after 10 months"

print_line

await_action

echo "Booking Start Date $(date -d @1692714600)"
echo "Booking End Date $(date -d @1692716400)"

curl -s -X POST "$BASE_URL/event/$eventId/book" -H "userId: $userId" -H 'Content-Type: application/json' -d "{\"eventId\": \"$eventId\", \"userId\": \"$userId\", \"slotStartTime\": 1692714600, \"slotEndTime\": 1692716400, \"userInformation\" : { \"name\" : \"Patient E\", \"email\": \"progix.21+2@gmail.com\", \"mobileNumber\" : \"9038686607\"  } }" | jq ''


await_action

print_line


echo "If you liked so far, why not use this system to book the next round of discussion. We will create a user called Priyesh and will try and book a slot with him. You should also get an email with confirmation"

userID=$(curl -s -X POST "$BASE_URL/user" -d '{ "name": "Priyesh Potdar","email": "progix.21@gmail.com","mobileNumber": "91902866608"}' -H 'content-type:application/json' | jq -r '.data.userId')

echo "User Priyesh Potdar is created successfully and userId of the same is: $userID"

await_action

print_line

echo "Now let's go ahead and create an event for availability"

await_action

print_line

echo "Event Start Date $(date -d @1666332000)"
echo "Event End Date $(date -d @1666355400)"

eventId=$(curl -s -X POST "$BASE_URL/event" -H "userId: $userID" -H 'Content-Type: application/json' -d'{"eventName": "Next round of discussion", "eventStartDateTime" : 1666332000, "eventEndDateTime": 1666355400, "slotDurationInMins": 60, "freeSlots": [ { "dow": "FRIDAY","freeSlots": [ { "slotId": null,  "startTime": 28800, "endTime": 46800, "slotType": "FREE", "timeType": "ABS" } ], "bookedSlots": null }  ],                  "bookedSlots": null } ]}' | jq -r '.data.id')

echo "Event is created successfully with id: $eventId"

await_action

print_line

echo "Booking Start Date $(date -d @1666341000)"
echo "Booking End Date $(date -d @1666344600)"

curl -s -X POST "$BASE_URL/event/$eventId/book?sendEmail=true" -H "userId: $userId" -H 'Content-Type: application/json' -d "{\"eventId\": \"$eventId\", \"userId\": \"$userId\", \"slotStartTime\": 1666341000, \"slotEndTime\": 1666344600, \"userInformation\" : { \"name\" : \"Prakash\", \"email\": \"prakash@goharbor.xyz\", \"mobileNumber\" : \"9028686607\"  } }" | jq ''

print_line

await_action

echo "Thank you going through all the scenarios. Further scenarioes can be tested on the Postman"


