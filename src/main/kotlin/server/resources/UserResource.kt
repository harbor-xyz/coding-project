package server.resources

import com.google.inject.Inject
import server.core.AvailabilityDTO
import server.core.AvailabilityDTOTransformer
import server.core.mappers.User
import server.core.service.UserAvailabilityService
import server.core.service.UserService
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/user")
class UserResource @Inject constructor(
    private val userService: UserService
) {

    @POST
    @Path("/create/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getAvailabilityByUser(userObject: User): Response {
        userService.createUser(userObject)
        return Response.ok().build()
    }

    @GET
    @Path("/get/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getUserById(@PathParam("id") userId: Int): Response {
        val user = userService.getUserById(userId)
        return Response.ok(user).build()
    }

    @GET
    @Path("/getByMobileNumber/{mobileNumber}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getUserByMobileNumber(@PathParam("mobileNumber") mobileNumber: String): Response {
        userService.getUserByMobileNumber(mobileNumber)
        return Response.ok().build()
    }
}