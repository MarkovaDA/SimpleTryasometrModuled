package ru.alidi.horeca.jobrunner.service.taskexecutor;

import com.google.common.base.Preconditions;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.stereotype.Component;
import ru.alidi.horeca.jobrunner.service.JobrunnerTaskService;
import ru.alidi.horeca.jobrunner.service.GenerateSitemapService;
import ru.alidi.horeca.jobrunner.service.dataimport.FullDataImportTaskExecutorService;
import ru.alidi.horeca.persistence.entity.JobrunnerTaskEntity;

import java.time.OffsetDateTime;

/**
 * Created by shevchenko.ru on 16.11.16.
 */
@Component
public class KnownTaskFactoryBean {

    private static final Logger log = LoggerFactory.getLogger(KnownTaskFactoryBean.class);

    protected static final String FULL_UPDATE_METHOD_NAME = "executeUpdate";
    
    protected static final String GENERATE_SITEMAP_METHOD_NAME = "generateSitemap";

    private ApplicationContext context;

    private JobrunnerTaskService jobrunnerTaskService;

    @Autowired
    public KnownTaskFactoryBean(ApplicationContext context, JobrunnerTaskService jobrunnerTaskService) {
        this.context = context;
        this.jobrunnerTaskService = jobrunnerTaskService;
    }

    /**
     *
     * @param knownTaskName  .toString из энума KnownTask
     * @return runnable соответствующей задачи
     * @throws IllegalArgumentException - если имени нет в энуме KnownTask
     */
    public Runnable createTask(String knownTaskName) {
        return this.createTask(KnownTask.valueOf(knownTaskName));
    }

    public Runnable createTask(KnownTask knownTasks) {

        Runnable task;

        switch (knownTasks) {
            case FullDataImportTask:
                task = createFullDataImportTask();
                break;
            case IncrementalDataImportTask:
                task = createIncrementalDataImportTask();
                break;
            case GenerateSitemapTask:
                task = createGenerateSitemapTask();
                break;
            default:
                throw new IllegalArgumentException("Неизвестная задача");
        }

        return new TaskWrapper(jobrunnerTaskService, task, knownTasks);

    }

    private Runnable createIncrementalDataImportTask() {

        throw new NotImplementedException(KnownTask.IncrementalDataImportTask + " неимплементировано");
    }
    
    private Runnable createGenerateSitemapTask(){
        
        Runnable executeUpdate = createScheduledMethodInvocation(
                GenerateSitemapService.class, GENERATE_SITEMAP_METHOD_NAME
        );
        return executeUpdate;
    }

    private Runnable createFullDataImportTask() {

        Runnable executeUpdate = createScheduledMethodInvocation(
                FullDataImportTaskExecutorService.class, FULL_UPDATE_METHOD_NAME
        );
        return executeUpdate;
    }

    private Runnable createScheduledMethodInvocation(Class clazz, String methodName) {

        try {
            Object bean = context.getBean(clazz);

            ScheduledMethodRunnable task = new ScheduledMethodRunnable(bean, methodName);

            return task;
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }

    }

    private static class TaskWrapper implements Runnable {

        private final JobrunnerTaskService jobrunnerTaskService;
        private final KnownTask knownTask;
        private final Runnable task;

        public TaskWrapper(JobrunnerTaskService jobrunnerTaskService, Runnable task, KnownTask knownTask) {

            Preconditions.checkNotNull(jobrunnerTaskService);
            Preconditions.checkNotNull(task);
            Preconditions.checkNotNull(knownTask);

            this.jobrunnerTaskService = jobrunnerTaskService;
            this.knownTask = knownTask;
            this.task = task;
        }

        @Override
        public void run() {

            try {

                this.task.run();

                this.updateTask();

            } catch (Exception ex){

                //TODO: handle error

                log.error("Неудачное выполнение задачи {}", knownTask, ex);

                throw ex;
            }
        }

        private void updateTask() {
            JobrunnerTaskEntity taskEntity = this.jobrunnerTaskService.get(knownTask.name());
            taskEntity.setLastExecutionDate(OffsetDateTime.now());
            this.jobrunnerTaskService.save(taskEntity);
        }
    }


}
