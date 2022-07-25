package common.core

import common.core.mappers.UserAvailability
import java.util.*


// DTO transformer

data class AvailabilityDTO(
    val userId: Int,
    val date: String,
    val startTime: String,
    val endTime: String
)

object AvailabilityDTOTransformer {

    //Parsing the given String to Date object
    fun toDTO(userAvailability: UserAvailability): AvailabilityDTO {
        val date = Date(userAvailability.date)
        return AvailabilityDTO(
            userId = userAvailability.userId,
            date = date.toString(),
            startTime = userAvailability.startTime.toString(),
            endTime = userAvailability.endTime.toString()
        )
    }
}