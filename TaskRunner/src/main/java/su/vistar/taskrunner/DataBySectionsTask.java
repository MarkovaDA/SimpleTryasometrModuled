package su.vistar.taskrunner;


import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import su.vistar.commons.db.TaskRunnerMapper;
import su.vistar.commons.model.AverageAcceleration;
import su.vistar.commons.model.DataBySection;
import su.vistar.commons.model.Section;
/*
*вторичный анализ данных - соотнесение отрезко по ускорениям
*/
@Component
public class DataBySectionsTask {

    @Autowired
    private TaskRunnerMapper taskRunnerMapper;
    
    private final int COUNT = 100; //порция обрабатываемых усредненных данных 


    class Point {

        public double lon;
        public double lat;

        public Point(double lon, double lat) {
            this.lon = lon;
            this.lat = lat;
        }

    }

    //cоотносим усредненные отрезки ускорений с секциями
    @Scheduled(cron = "0 0/2 * 1/1 * ?") //раз в две минуты
    public void run() {
        List<AverageAcceleration> averagedAccelerations = taskRunnerMapper.getAverages(COUNT);
        averagedAccelerations.forEach(average -> {
          
        });
    }

}
