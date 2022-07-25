package common

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.dropwizard.Configuration

@JsonIgnoreProperties(ignoreUnknown = true)
class CalendlyProjectConfiguration : Configuration()