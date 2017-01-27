package su.vistar.taskrunner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import su.vistar.taskrunner.mapper.SensorDataMapper;
import su.vistar.taskrunner.model.AverageAcceleration;
import su.vistar.taskrunner.model.Location;

@Component
public class PrimaryAnalizeTask {

    //http://www.cronmaker.com/
    private static final Logger log = LoggerFactory.getLogger(PrimaryAnalizeTask.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private static final int COUNT = 100;//порция обрабатываемых за раз данных

    @Autowired
    private SensorDataMapper sensorDataMapper;

    @Scheduled(/*fixedRate = 5000*/cron = "0 0/1 * 1/1 * ?") //раз в  минуту
    public void run() {
        calculateAverageAccelerations();
    }

    private void calculateAverageAccelerations() {
        List<Location> oneHundred = new ArrayList<>();
        Location locStart, locEnd; //начало и конец очередного отрезка
        List<AverageAcceleration> averagedList;
        do {
            oneHundred = sensorDataMapper.getLastLocation(COUNT);
            //sensorDataMapper.deleteLocations(COUNT);
            //как удалить ускорения?
            for (int j = 0; j < oneHundred.size() - 1; j += 1) {
                locStart = oneHundred.get(j);
                locEnd = oneHundred.get(j + 1);
                //усредненные ускорения на отрезке [locStart,locEnd]
                averagedList = sensorDataMapper.averageForLine(locStart.getDateTime(), locStart.getDateTime());
                //sensorDataMapper.deleteAccelerations(locStart.getDateTime(), locStart.getDateTime());
                System.out.println(averagedList.size());
                if (!averagedList.isEmpty()) {
                    sensorDataMapper.insertAveragedAccelerations(averagedList, locStart, locEnd);
                }
            }
        } while (!oneHundred.isEmpty());
    }
}
