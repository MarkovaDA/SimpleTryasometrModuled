package ru.alidi.horeca.jobrunner.service.taskexecutor;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alidi.horeca.jobrunner.service.JobrunnerTaskService;
import ru.alidi.horeca.persistence.entity.JobrunnerTaskEntity;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;


/**
 *
 * Выполняет задачи по крону.
 *
 * Created by shevchenko.ru on 15.11.16.
 */
@Service
public class KnownTaskExecutorService {

    private final static Logger log = LoggerFactory.getLogger(KnownTaskExecutorService.class);

    private final Map<String, ScheduledFuture> futureMap = new TreeMap<>();

    private ThreadPoolTaskScheduler scheduler;
    private KnownTaskFactoryBean knownTaskFactoryBean;
    private JobrunnerTaskService jobrunnerTaskService;

    @Autowired
    public KnownTaskExecutorService(ThreadPoolTaskScheduler scheduler,
                                    KnownTaskFactoryBean knownTaskFactoryBean,
                                    JobrunnerTaskService jobrunnerTaskService) {

        this.scheduler = scheduler;
        this.knownTaskFactoryBean = knownTaskFactoryBean;
        this.jobrunnerTaskService = jobrunnerTaskService;
    }

    @PostConstruct
    public void init(){

        log.info("Инициализация задач");

        List<JobrunnerTaskEntity> jobs = jobrunnerTaskService.getAllTasks();

        jobs.stream().filter(j -> !j.isManual()).forEach(job -> {

                log.info("Инициализация задачи {}[{}]", job.getName(), job.getKey());

                try {
                    ScheduledFuture schedule = scheduler.schedule(
                            knownTaskFactoryBean.createTask(job.getKey()),
                            new CronTrigger(job.getCronTrigger())
                    );

                    futureMap.put(job.getKey(), schedule);

                } catch (IllegalArgumentException ex1){
                    log.error("Задача {}[{}] не была поставлена в очередь, т.к. неизвестна для текущей версии приложения",
                            job.getName(), job.getKey(), ex1);
                } catch (Exception ex2){
                    log.error("Задача {}[{}] не была поставлена в очередь", job.getName(), job.getKey(), ex2);
                }
        });
    }

    @Transactional(transactionManager = "transactionManager", readOnly = true)
    public void runKnownTaskAsync(JobrunnerTaskEntity job){

        Preconditions.checkNotNull(job);

        if (job.isManual()) {
            scheduler.execute(
                    knownTaskFactoryBean.createTask(job.getKey())
            );
        } else {
            throw new RuntimeException("Задача " + job.getKey() + " не может быть выполнена вручную");
        }
    }
}
