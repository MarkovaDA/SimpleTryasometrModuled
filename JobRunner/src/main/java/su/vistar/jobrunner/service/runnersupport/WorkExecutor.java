package ru.alidi.horeca.jobrunner.service.runnersupport;

import java.util.Collection;
import java.util.Date;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.alidi.horeca.jobrunner.service.work.AbstractWork;
import java.util.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runnable for work execution
 * @author Aleksandr Gorovoi<alexander.gorovoy@vistar.su>
 */
@Component
@Scope("prototype")
public class WorkExecutor implements Runnable {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private AbstractWork currentWork;    
    private final Long maxAttemptCount;    
    private final Queue<AbstractWork> queue;
    private final Collection<AbstractWork> storage;

    @Override
    public void run() {
        logger.info("Start work");
        while (true) {
            synchronized (queue) {
                if (queue.isEmpty()) {
                    try {
                        queue.wait();
                    } catch (InterruptedException ex) {
                        logger.error("Worker executor interrupted", ex);
                        break;
                    }
                }
                currentWork = queue.poll();
            }
            if (currentWork == null) continue;
            logger.info(String.format(
                    "Work %s launched, attempt %d",
                    currentWork.toString(), currentWork.getAttemptCount()
            ));
            try {
                currentWork.setAttemptCount(currentWork.getAttemptCount() + 1);
                currentWork.setLastAttempt(new Date());                
                currentWork.run();
            } catch (RuntimeException e) {
                logger.error(
                        "Error on process work " + currentWork.toString(), e
                );
                currentWork.onError(e);
                if (currentWork.getAttemptCount() < maxAttemptCount) {
                    storage.add(currentWork);
                }
                continue;
            }
            if (currentWork.isSkipped()) {
                currentWork.onSkip();
                logger.info(String.format("Work %s skipped", currentWork.toString()));
            } else {
                currentWork.onSuccess();
                logger.info(String.format("Work %s finished", currentWork.toString()));
            }
        }
    }
    
    public WorkExecutor(Queue queue, Collection storage, Long maxAttemptCount) {
        this.queue = queue;
        this.storage = storage;
        this.maxAttemptCount = maxAttemptCount;
    }

}
