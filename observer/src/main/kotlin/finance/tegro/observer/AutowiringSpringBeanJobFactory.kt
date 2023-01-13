package finance.tegro.observer

import org.quartz.spi.TriggerFiredBundle
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.scheduling.quartz.SpringBeanJobFactory


class AutowiringSpringBeanJobFactory : SpringBeanJobFactory(), ApplicationContextAware {
    @Transient
    private var beanFactory: AutowireCapableBeanFactory? = null

    override fun setApplicationContext(
        context: ApplicationContext
    ) {
        beanFactory = context.getAutowireCapableBeanFactory()
    }

    @Throws(Exception::class)
    override fun createJobInstance(
        bundle: TriggerFiredBundle
    ): Any {
        val job = super.createJobInstance(bundle)
        beanFactory!!.autowireBean(job)
        return job
    }
}
