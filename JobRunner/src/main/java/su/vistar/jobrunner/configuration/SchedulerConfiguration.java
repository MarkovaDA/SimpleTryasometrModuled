package su.vistar.jobrunner.configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;


@Configuration
public class SchedulerConfiguration {

    @Autowired
    private Environment env;

    @Bean
    public ThreadPoolTaskScheduler taskScheduler(){

        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        scheduler.setPoolSize(env.getProperty("cronexecutor.threads", Integer.TYPE, 1));
        scheduler.setThreadFactory(new CustomizableThreadFactory("sched-thread"));

        return scheduler;
    }


}
