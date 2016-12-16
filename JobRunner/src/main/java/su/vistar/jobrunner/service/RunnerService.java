package ru.alidi.horeca.jobrunner.service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import ru.alidi.horeca.jobrunner.service.runnersupport.DBQueueChecker;
import ru.alidi.horeca.jobrunner.service.runnersupport.IntervalChecker;
import ru.alidi.horeca.jobrunner.service.runnersupport.WorkExecutor;
import ru.alidi.horeca.jobrunner.service.work.AbstractWork;

/**
 * Service for running work
 * @author Aleksandr Gorovoi<alexander.gorovoy@vistar.su>
 */
@Service
@PropertySource("classpath:runner.properties")
public class RunnerService {
    
    Logger logger = LoggerFactory.getLogger(RunnerService.class);
    
    @Autowired
    private ApplicationContext ctx;
    
    private final Queue<AbstractWork> workQueue = new ConcurrentLinkedQueue<>();
    
    private final List<AbstractWork> workStorage = Collections
            .synchronizedList(new LinkedList<>());
    
    private List<Thread> workerThreads;
    private Thread dbQueueThread;
    private Thread intervalCheckerThread;
    
    private Long maxAttemptCount;
    private Long dbCheckInterval;
    private Long timeCheckInterval;
    private Long runInterval;
    private Integer workerCount;
    
    public void addWork(AbstractWork runnable) {
        workStorage.add(runnable);
    }
    
    @PostConstruct
    public void init() {
        this.workerThreads = new LinkedList<>();

        for (int i = 0; i < getWorkerCount(); i++) {
            Thread workerThread = new Thread(ctx.getBean(WorkExecutor.class,
                    workQueue, workStorage, maxAttemptCount
            ));
            workerThread.setName(WorkExecutor.class.getSimpleName() + " #" + i);
            workerThreads.add(workerThread);
            workerThread.start();
        }
        
        dbQueueThread = new Thread(ctx.getBean(DBQueueChecker.class,
                workStorage, dbCheckInterval
        ));
        dbQueueThread.setName(DBQueueChecker.class.getSimpleName());
        dbQueueThread.start();
        
        intervalCheckerThread = new Thread(ctx.getBean(IntervalChecker.class,
                runInterval, timeCheckInterval, workQueue, workStorage
        ));
        intervalCheckerThread.setName(IntervalChecker.class.getSimpleName());
        intervalCheckerThread.start();
    }
    
    @PreDestroy
    public void destroy() {
        for (Thread workerThread : workerThreads) {
            workerThread.interrupt();
        }
        this.dbQueueThread.interrupt();
        this.intervalCheckerThread.interrupt();
    }

    public Long getMaxAttemptCount() {
        return maxAttemptCount;
    }

    public void setMaxAttemptCount(Long maxAttemptCount) {
        this.maxAttemptCount = maxAttemptCount;
    }

    public Long getDbCheckInterval() {
        return dbCheckInterval;
    }

    public void setDbCheckInterval(Long dbCheckInterval) {
        this.dbCheckInterval = dbCheckInterval;
    }

    public Long getTimeCheckInterval() {
        return timeCheckInterval;
    }

    public void setTimeCheckInterval(Long timeCheckInterval) {
        this.timeCheckInterval = timeCheckInterval;
    }

    public Long getRunInterval() {
        return runInterval;
    }

    public void setRunInterval(Long runInterval) {
        this.runInterval = runInterval;
    }

    public Integer getWorkerCount() {
        return workerCount;
    }

    public void setWorkerCount(Integer workerCount) {
        this.workerCount = workerCount;
    }
}
