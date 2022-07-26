# Harbor Take Home Project

Welcome to the Harbor take home project. We hope this is a good opportunity for you to showcase your skills.

## The Challenge

Build us a REST API for calendly. Remember to support

- Setting own availability
- Showing own availability
- Finding overlap in schedule between 2 users

It is up to you what else to support.



## How to test

### remote
Server is hosted remotely at via Docker & Azure Container at calendly.southindia.azurecontainer.io:7002
```
GET user:  http://localhost:7002/user/get/1
GET user availability:  http://calendly.southindia.azurecontainer.io:7002/availability/user/1
```
### local
```
git pull <this repo>
# if docker is configured, then follow below two steps
docker build -f Dockerfile -t calendly-application . 
docker run -p7002:7002 calendly-application:latest  

# to run plain jar, requirements: need Java8
java -jar calendly-application-1.0.0-snapshot.jar
```

#### testing
```
Few GET/POST APIs  
please replace 127.0.0.1 with calendly.southindia.azurecontainer.io for remote testing

# get user 
127.0.0.1:7002/user/get/1

# get availability of user 
127.0.0.1:7002/availability/user/1

# POST availability for user with id 1
curl -X POST -H "Content-Type: application/json" -d  '{"availabilityList": [{"date": "1658807368000", "startTime": "1658807368000", "endTime": "1658814595000"}]}' http://127.0.0.1:7002/availability/userSubmitAvailability/1

# GET Overlappping availability between two users 
http://127.0.0.1:7002/availability/userAvailability/getOverlappingAvailability/user1/1/userId2/2
```

![submit availability](https://ibb.co/C1d0pTn)
![overlapped availability](https://ibb.co/zQGjP0p)


### Tech choices:
```
- application is coded in Kotlin 
- java 8, maven 3.2, kotlin 1.6.0, dropwizard, Azure SQL
- Persistence is mandatory, achieved via Azure SQL 
- UI is out of scope for this application
```

### Design choices:

Would love to talk about it


## Build Locally
```
git clone <this repo> -o calendly
cd calendly 

# open IntelliJ and import project via POM.xml 
# click Maven on the right side bar and let it download depencies
# Edit configurations:  
### Main class: common.ApplicationKt
### Use classpath of module:  common 
### working directory:  <current project parent directory>
# click run on top which should run the application in IntelliJ 

# to create a fat JAR
mvn clean package
```


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

