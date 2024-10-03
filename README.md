
## MVP Features supported:
- User is able to create, view, update, delete his profile
- User is able to create, view , modify and delete his availability
- User is able to share his entire availability to other users
- User is able to see all overlaps with any set of registered users
- user is able to create, view, update, cancel booking meeting 

## Test Coverage
- Added unit test for userService.(Skipped other classes in the interest of time)
- Exhaustive Integration Test Coverage using RestTemplate for all features except BookingController.
- Integration test coverage ensures working Apis and its correctness

## Assumptions 
-Using InMemory Database for persisting data.
-All users register on the application to be able to use the features
- Have skipped some validation and incremental logic in some parts of application.Will keep on iterating on the same.

## Future Improvements
- Add logging framework
- Emit appropriate metrics
- Integrate Swagger
- Integrate Java Cods Coverage






