package common.server

import org.slf4j.LoggerFactory
import java.io.Closeable
import java.lang.IllegalArgumentException

class LifeCycleObjectRepo private constructor() : Closeable {
    companion object {
        private val logger = LoggerFactory.getLogger(LifeCycleObjectRepo::class.java)

        private val global by lazy { LifeCycleObjectRepo() }
        fun global() = global

        init {
            Runtime.getRuntime().addShutdownHook(Thread {
                global().close()
            })
        }
    }

    private val closeableObjects: MutableList<SequencedAutoCloseable> = mutableListOf()

    fun register(closeable: AutoCloseable): LifeCycleObjectRepo {
        register(closeable, Short.MAX_VALUE)
        return this
    }

    fun register(closeable: AutoCloseable, sequenceNumber: Short): LifeCycleObjectRepo {
        if (closeable == global) {
            return this
        }
        if (sequenceNumber < 0) {
            throw IllegalArgumentException("sequenceNumber is negative.")
        }
        if (!closeableObjects.contains(closeable) && closeableObjects.add(SequencedAutoCloseable(sequenceNumber, closeable))) {
            logger.info("Register {} for close at shutdown", closeable)
        }
        return this
    }

    @Synchronized
    override fun close() {
        closeableObjects.sortBy { it.sequenceNumber }
        closeableObjects.forEach { c ->
            try {
                logger.info("Closing {}", c)
                c.close()
            } catch (e: Exception) {
                logger.error("Error closing object", e)
            }
        }
        closeableObjects.clear()
    }

    private data class SequencedAutoCloseable(val sequenceNumber: Short, val closeable: AutoCloseable): AutoCloseable {
        override fun close() {
            closeable.close()
        }

        override fun toString(): String = "SequencedAutoCloseable [${closeable}]"
    }
}