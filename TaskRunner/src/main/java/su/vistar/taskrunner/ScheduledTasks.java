package su.vistar.taskrunner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import su.vistar.taskrunner.mapper.SensorDataMapper;
import su.vistar.taskrunner.model.AverageAcceleration;
import su.vistar.taskrunner.model.Location;

@Component
public class ScheduledTasks {
    //http://www.cronmaker.com/
    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private static final  int COUNT = 100;//порция обрабатываемых за раз данных
    
    @Autowired
    private  SensorDataMapper sensorDataMapper;
    
    @Scheduled(/*fixedRate = 5000*/cron="0 0/1 * 1/1 * ?") //раз в 10 минут
    public void reportCurrentTime() {
        calculateAverageAccelerations();
        log.info("TIME IS NOW {}", dateFormat.format(new Date()));
    }
    //в отдельном потоке выполняется обработка таблицы average_accelerations
    private void calculateAverageAccelerations(){
        int from = 0;
        List<Location> oneHundred = new ArrayList<>();
        Location locStart, locEnd; //начало и конец очередного отрезка
        List<AverageAcceleration> averagedList; 
        do{
            oneHundred = sensorDataMapper.getLastLocation(from, COUNT);//запрос должен быть с удалением
            for(int j=0; j< oneHundred.size()-1; j+=1){
                locStart = oneHundred.get(j);
                locEnd = oneHundred.get(j + 1);               
                if (locStart.getDateTime().equals(locEnd.getDateTime()))
                    continue;
                //усредненные ускорения на отрезке [locStart,locEnd]
                averagedList = sensorDataMapper.averageForLine(locStart.getDateTime(), locEnd.getDateTime());
                System.out.println(averagedList.size());
                if (!averagedList.isEmpty()) 
                    sensorDataMapper.insertAveragedAccelerations(averagedList, locStart, locEnd);
            }
            from += COUNT;
        }
        while(!oneHundred.isEmpty());       
    }
    //это тоже будет выполняться в отдельном потоке с определенной переодичностью
    private void calculateKoeffForSections(){
        //алгоритм таков: беру отрезок (две точки)
        //нахожу секцию,к которой принадлжет каждая из точек,если номера секций совпадают, то значит - 
        //точка действительно принадлжеит секции
        //принадлежность точки в секции проверяется путем подстановки в уравнение прямой секции и сверка равенства
    }
}
