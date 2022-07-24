package server.resources

import kotlinx.coroutines.*
import server.core.AvailabilityDTO
import server.core.AvailabilityDTOTransformer
import server.core.mappers.UserAvailability
import server.core.service.UserAvailabilityService
import server.resources.DTO.UserAvailabilityDTO
import java.time.Instant
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

/*
    this resource holds all the API methods exposed to the frontend

        - getAvailabilityByUser
            userId

        - getOverlappingAvailability
            userId1
            userId2
            => compares UTC timestamps and expose common timestamps in UTC

        - createAvailability
            {userId1
                [
                    {date, startTime, endTime, timezone},
                    {date, startTime, endTime, timezone}
                ]
            }
 */
@Path("/availability")
class UserAvailabilityResource @Inject constructor(
    private val userAvailabilityService: UserAvailabilityService
) {

    @GET
    @Path("/user/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getAvailabilityByUser(@PathParam("id") userId: Int): Response {
            val data = runBlocking {  userAvailabilityService.getUserAvailability(userId) }
            val avaiabilityOfUser = mutableListOf<AvailabilityDTO>()
            data!!.forEach {
                avaiabilityOfUser.add(AvailabilityDTOTransformer.toDTO(it))
            }
        return Response.ok(avaiabilityOfUser).build()
    }

    @POST
    @Path("/userSubmitAvailability/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    fun submitAvailability(@PathParam("userId") userId: Int, userAvailabilityObject: UserAvailabilityDTO): Response {
        println(userAvailabilityObject)
            userAvailabilityObject.availabilityList.forEach {
                userAvailabilityService.createUserAvailability(
                    UserAvailability(
                        userId = userId,
                        date = it.date.toLong(),
                        startTime = it.startTime.toLong(),
                        endTime = it.endTime.toLong()
                    )
                )
            }

        return Response.ok().build()
    }

}