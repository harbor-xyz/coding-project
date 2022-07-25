package common.server

import com.google.inject.AbstractModule
import com.google.inject.Module
import com.google.inject.Scope
import com.google.inject.Scopes
import com.google.inject.matcher.Matchers
import com.google.inject.spi.BindingScopingVisitor
import com.google.inject.spi.ProvisionListener
import io.dropwizard.lifecycle.Managed
import io.dropwizard.setup.Environment
import javax.inject.Singleton

class GuiceClosableListener(private val consumer: (AutoCloseable) -> Any) : ProvisionListener {
    override fun <T> onProvision(provisionInvocation: ProvisionListener.ProvisionInvocation<T>) {
        val provision = provisionInvocation.provision()
        if (provision is AutoCloseable && shouldManage(provisionInvocation)) {
            consumer(provision as AutoCloseable)
        }
    }

    private fun shouldManage(provisionInvocation: ProvisionListener.ProvisionInvocation<*>): Boolean {
        return provisionInvocation.binding.acceptScopingVisitor(object : BindingScopingVisitor<Boolean> {
            override fun visitEagerSingleton(): Boolean? {
                return true
            }

            override fun visitScope(scope: Scope): Boolean? {
                return scope === Scopes.SINGLETON
            }

            override fun visitScopeAnnotation(scopeAnnotation: Class<out Annotation>): Boolean? {
                return scopeAnnotation.isAssignableFrom(Singleton::class.java)
            }

            override fun visitNoScoping(): Boolean? {
                return false
            }
        })
    }
}

class LifecycleAwareModule(
    private val module: Module,
    private val lcObjectRepo: LifeCycleObjectRepo = LifeCycleObjectRepo.global())
    : AbstractModule() {
    override fun configure() {
        bindListener(Matchers.any(), GuiceClosableListener(lcObjectRepo::register))
        install(module)
    }
}

class DropwizardAwareModule(
    private val module: Module,
    private val environment: Environment)
    : AbstractModule() {
    override fun configure() {
        bindListener(Matchers.any(), GuiceClosableListener(environment::register))
        install(module)
    }
}


// extensions!


fun AutoCloseable.toManaged() = object : Managed {
    override fun stop() {
        close()
    }

    override fun start() {
    }
}


fun Environment.register(closeable: AutoCloseable) = apply {
    lifecycle().manage(closeable.toManaged())
}