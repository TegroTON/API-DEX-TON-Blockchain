package finance.tegro.observer.configuration

import finance.tegro.observer.AutowiringSpringBeanJobFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.quartz.SpringBeanJobFactory


@Configuration
class QuartzConfiguration {
    @Bean
    fun springBeanJobFactory(applicationContext: ApplicationContext): SpringBeanJobFactory? {
        val jobFactory = AutowiringSpringBeanJobFactory()
        jobFactory.setApplicationContext(applicationContext)
        return jobFactory
    }
}
