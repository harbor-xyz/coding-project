package common.resources

import com.microsoft.azure.eventhubs.ConnectionStringBuilder
import com.microsoft.azure.eventhubs.EventHubClient
import com.microsoft.azure.eventprocessorhost.EventProcessorHost
import com.microsoft.azure.storage.CloudStorageAccount
import java.net.URI
import java.util.concurrent.ScheduledExecutorService

/**
 * Resource Builder for Azure cloud storage accounts. Use [ResourceBuilder].cloudBlobClient as the entry point
 *
 * This assumes a Map type for configuration, with the following properties:
 * 1. account - storage account name
 * 2. accessKey - storage account access key
 * 3. protocol (optional, default = https)
 */
class StorageAccountBuilder(val builder: ResourceBuilder) {
    /**
     * Returns a [CloudStorageAccount], using the configuration specified in the builder
     */
    fun build(): CloudStorageAccount {
        return CloudStorageAccount.parse(connectionString())
    }

    fun connectionString(): String {
        val account = builder.descriptor["account"]!!
        val accessKey = builder.descriptor["accessKey"]!!
        val protocol = builder.descriptor["protocol"] ?: "https"
        val endpoint = builder.descriptor["endpoint"]

        var connStr = "DefaultEndpointsProtocol=$protocol;AccountName=$account;AccountKey=$accessKey"
        if (endpoint != null) {
            connStr = "$connStr;TableEndpoint=$endpoint"
        }
        return connStr
    }

    fun accountName(): String = builder.descriptor["account"]!!
}

class EventHubsBuilder(val builder: ResourceBuilder) {
    fun buildEventProcessorHost(storageAccountBuilder: StorageAccountBuilder): EventProcessorHost {
        val descriptor = builder.descriptor
        val eventHubConnectionString = connectionString()
        return EventProcessorHost(
            EventProcessorHost.createHostName(null),
            descriptor["eventHubName"],
            descriptor["consumerGroupName"],
            eventHubConnectionString,
            storageAccountBuilder.connectionString(),
            storageAccountBuilder.accountName()
        )
    }

    fun buildEventProcessorHost(
        storageAccountBuilder: StorageAccountBuilder,
        executorService: ScheduledExecutorService
    ): EventProcessorHost {
        val descriptor = builder.descriptor
        val eventHubConnectionString = connectionString()
        return EventProcessorHost(
            EventProcessorHost.createHostName(null),
            descriptor["eventHubName"],
            descriptor["consumerGroupName"],
            eventHubConnectionString,
            storageAccountBuilder.connectionString(),
            storageAccountBuilder.accountName(),
            executorService
        )
    }

    fun buildEventProcessorHost(
        consumerGroupName: String,
        storageAccountBuilder: StorageAccountBuilder
    ): EventProcessorHost {
        val descriptor = builder.descriptor
        val eventHubConnectionString = connectionString()
        return EventProcessorHost(
            EventProcessorHost.createHostName(null),
            descriptor["eventHubName"],
            consumerGroupName,
            eventHubConnectionString,
            storageAccountBuilder.connectionString(),
            storageAccountBuilder.accountName()
        )
    }

    fun connectionString(): String {
        val descriptor = builder.descriptor

        val connectionStringBuilder = ConnectionStringBuilder()
        connectionStringBuilder.eventHubName = descriptor["eventHubName"]
        connectionStringBuilder.sasKeyName = descriptor["sasKeyName"]
        connectionStringBuilder.sasKey = descriptor["sasKey"]

        if (descriptor.containsKey("namespaceName")) {
            connectionStringBuilder.setNamespaceName(descriptor["namespaceName"])
        } else if (descriptor.containsKey("endpointName")) {
            connectionStringBuilder.endpoint = URI(descriptor["endpointName"])
        } else {
            throw IllegalArgumentException("connection string must contain either namespaceName or endpointName")
        }

        return connectionStringBuilder.toString()
    }
}
