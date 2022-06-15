# Harbor Take Home Project

Welcome to the Harbor take home project. We hope this is a good opportunity for you to showcase your skills.

## Setup Instructions
Please follow `With Docker` if you have docker installed.
### With Docker
1. Build the docker image
```
docker build . -t saksham115/interview
```
2. Run the docker image (while exposing the 3001 port)
```
docker run -p 3001:3001 saksham115/interview
```

### Without Docker

#### Pre-requisites
1. Install node  >= v16.
2. Install npm
3. Make sure sqlite is running without username and password

#### Installation
```
# Make sure you are in the directory
npm install
npm run start
```

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
