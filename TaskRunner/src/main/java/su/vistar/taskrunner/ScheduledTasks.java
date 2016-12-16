package su.vistar.taskrunner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import su.vistar.taskrunner.mapper.SensorDataMapper;
import su.vistar.taskrunner.model.Location;

@Component
public class ScheduledTasks {
    //http://www.cronmaker.com/
    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    
    @Autowired
    private  SensorDataMapper sensorDataMapper;
    
    @Scheduled(/*fixedRate = 5000*/cron="0 0/1 * 1/1 * ?")
    public void reportCurrentTime() {
        //List<Location> lastLocations = sensorDataMapper.getLastLocation();
        //System.out.println(lastLocations.size());
        log.info("TIME IS NOW {}", dateFormat.format(new Date()));
    }
}
