package server.resources.DTO


data class UserAvailabilityDTO(
    val availabilityList: List<atomicAvailability>
)

data class atomicAvailability(
    val date: String,
    val startTime: String,
    val endTime: String
)