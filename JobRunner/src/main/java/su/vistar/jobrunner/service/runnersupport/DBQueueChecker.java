package ru.alidi.horeca.jobrunner.service.runnersupport;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.alidi.horeca.jobrunnerapi.service.MailQueueService;
import ru.alidi.horeca.jobrunnerapi.service.dto.MailQueueDTO;
import ru.alidi.horeca.jobrunner.service.work.AbstractWork;
import ru.alidi.horeca.jobrunner.service.work.MailWork;

/**
 * Check DB mail queue.
 * Based on fact, that id of later inserted entity is greater.
 * @author Aleksandr Gorovoi<alexander.gorovoy@vistar.su>
 */
@Component
@Scope("prototype")
public class DBQueueChecker implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    protected final Long checkInterval;
    private Long lastId;
    protected final Collection<AbstractWork> workCollection;
    
    @Autowired
    private MailQueueService service;
    
    @Autowired
    private ApplicationContext ctx;

    @Override
    public void run() {
        while (true) {
            logger.info("Start DB checking...");
            List<MailQueueDTO> lastValues = service.getAll().stream()
                    .filter(_qd -> _qd.getId() > lastId)
                    .collect(Collectors.toList());
            lastId = lastValues.stream()
                    .mapToLong(_qd -> _qd.getId())
                    .max().orElse(lastId);
            long count = 0;
            for (MailQueueDTO dto : lastValues) {
                if (dto.getMailId() != null) {
                    synchronized (workCollection) {
                        workCollection.add(
                                ctx.getBean(MailWork.class, dto.getMailId())
                        );
                        workCollection.notifyAll();
                    }
                    count++;
                } else {
                    service.delete(dto.getId());
                }
            }
            logger.info(String.format(
                    "Added %d works, lastId: %d",
                    count, lastId
            ));
            try {
                Thread.sleep(checkInterval);
            } catch (InterruptedException ex) {
                logger.info("DB Queue checker interrupred");
                break;
            }
        }
    }

    public DBQueueChecker(Collection<AbstractWork> workCollection, Long checkInterval) {
        this.checkInterval = checkInterval;
        lastId = -1l;
        this.workCollection = workCollection;
        
    }


}
