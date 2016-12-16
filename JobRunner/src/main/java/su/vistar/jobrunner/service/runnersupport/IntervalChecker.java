package ru.alidi.horeca.jobrunner.service.runnersupport;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.alidi.horeca.jobrunner.service.work.AbstractWork;

/**
 *
 * @author Aleksandr Gorovoi<alexander.gorovoy@vistar.su>
 */
@Component
@Scope("prototype")
public class IntervalChecker implements Runnable {
    
    Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private final Long runInterval;
    private final Long checkInterval;
    
    private final Collection<AbstractWork> storageCollection;
    private final Collection<AbstractWork> executeCollection;

    
    @Override
    public void run() {
        while (true) {
            logger.info("Start limits checking...");
            Date timeLimit = new Date(new Date().getTime() - runInterval);
            List<AbstractWork> works = storageCollection.stream()
                    .filter(_aw -> _aw.getLastAttempt() == null 
                            || _aw.getLastAttempt().before(timeLimit))
                    .collect(Collectors.toList());
            synchronized (executeCollection) {
                executeCollection.addAll(works);
                storageCollection.removeAll(works);
                executeCollection.notifyAll();
            }
            logger.info(String.format("Added to queue: %d", works.size()));
            try {
                Thread.sleep(this.checkInterval);
            } catch (InterruptedException ex) {
                logger.error("Interval checking interrupted", ex);
                break;
            }
        }
    }
    
    public IntervalChecker(Long runInterval, Long checkInterval,
            Collection<AbstractWork> executeCollection,
            Collection<AbstractWork> storageCollection) {
        this.runInterval = runInterval;
        this.checkInterval = checkInterval;
        this.storageCollection = storageCollection;
        this.executeCollection = executeCollection;
    }
    
}
