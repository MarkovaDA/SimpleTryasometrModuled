package su.vistar.taskrunner;


import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import su.vistar.commons.db.TaskRunnerMapper;
import su.vistar.commons.model.AverageAcceleration;
import su.vistar.commons.model.Location;

/*
*task первичного анализа данных: вычисление среднего ускорения по отрезкам
*/
@Component
public class PrimaryAnalizeTask {
    //http://www.cronmaker.com/
    private static final int COUNT = 100;//порция обрабатываемых за раз данных

    @Autowired
    private TaskRunnerMapper taskRunnerMapper;

    @Scheduled(/*fixedRate = 5000*/cron = "0 0/1 * 1/1 * ?") 
    public void run() {
        calculateAverageAccelerations();
    }
    
    private void calculateAverageAccelerations() {
        List<Location> portionData; //порция данных
        Location locStart, locEnd; //начало и конец очередного отрезка
        List<AverageAcceleration> averagedList;
        do {
            portionData = taskRunnerMapper.getLastLocation(COUNT);
            //взяли и сразу удаляем
            //sensorDataMapper.deleteLocations(COUNT);
            for (int j = 0; j < portionData.size() - 1; j += 1) {
                locStart = portionData.get(j);
                locEnd = portionData.get(j + 1);
                //усредненные ускорения на отрезке [locStart,locEnd]
                averagedList = taskRunnerMapper.averageForLine(locStart.getDataTime(), locEnd.getDataTime());
                //sensorDataMapper.deleteAccelerations(locStart.getDateTime(), locStart.getDateTime());
                System.out.println(averagedList.size());
                if (!averagedList.isEmpty()) {
                    taskRunnerMapper.insertAveragedAccelerations(averagedList, locStart, locEnd);
                }
            }
        } while (!portionData.isEmpty());
    }
}
