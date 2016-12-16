package ru.alidi.horeca.jobrunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.DispatcherServlet;
import ru.alidi.horeca.jobrunner.configuration.WebMvcConfig;
import ru.alidi.horeca.jobrunner.configuration.WebSecurityConfig;
import ru.alidi.horeca.jobrunner.service.RunnerService;
import ru.alidi.horeca.persistence.configuration.MsSqlPersistenceConfig;
import ru.alidi.horeca.persistence.configuration.PostgresPersistenceConfig;
import ru.alidi.horeca.persistence.mongo.MongoPersistenceConfig;

/**
 * @author Aleksandr Gorovoi<alexander.gorovoy@vistar.su>
 */
@EnableAsync
@SpringBootConfiguration
@ComponentScan({"ru.alidi.horeca"})
@Import(value = {
        PostgresPersistenceConfig.class,
        MsSqlPersistenceConfig.class,
        MongoPersistenceConfig.class
})
public class Application extends SpringBootServletInitializer {
    
    private static Logger log = LoggerFactory.getLogger(Application.class);
    
    public static void main(String[] args){
        SpringApplication.run(Application.class, args);
    }
    
    @Autowired
    AnnotationConfigEmbeddedWebApplicationContext ctx;
    
    @Autowired
    private Environment env;

    @Bean
    public ServletRegistrationBean dispatcherServlet() {
        DispatcherServlet dispatcherServlet = new DispatcherServlet();   
        ctx.register(WebMvcConfig.class, WebSecurityConfig.class);
        dispatcherServlet.setApplicationContext(ctx);
        ServletRegistrationBean servletRegistrationBean 
                = new ServletRegistrationBean(dispatcherServlet, "/");
        return servletRegistrationBean;
    }
    
    @Bean
    public RunnerService runnerService() {
        RunnerService runner = new RunnerService();

        runner.setWorkerCount(env
                .getProperty("worker.workercount", Integer.TYPE, 1));
        runner.setMaxAttemptCount(env
                .getProperty("worker.attemptcount", Long.TYPE, 20L));
        runner.setDbCheckInterval(env
                .getProperty("worker.db.checkInterval", Long.TYPE, 1000L));
        runner.setTimeCheckInterval(env
                .getProperty("worker.time.checkInteval", Long.TYPE, 1000L));
        runner.setRunInterval(env
                .getProperty("worker.time.runInterval", Long.TYPE, 1000L));
        
        return runner;
    }
    
}
