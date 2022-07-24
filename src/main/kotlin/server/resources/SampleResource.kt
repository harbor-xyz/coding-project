package server.resources

import com.google.inject.Inject
import com.google.inject.Singleton
import server.core.service.CourseService
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
@Path("/debug")
class SampleResource @Inject constructor(
    private val courseService: CourseService
){

    @GET
    @Path("/dude")
    @Produces(MediaType.APPLICATION_JSON)
    fun hello(): Response  {
        return Response.ok("hello").build()
    }

    @GET
    @Path("/course/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getCourse(@PathParam("id") id: Int): Response {
        val data = courseService.getCourseDataById(id)
        return Response.ok(data).build()
    }
}