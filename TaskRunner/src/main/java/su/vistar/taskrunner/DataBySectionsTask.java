package su.vistar.taskrunner;

import java.util.Iterator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import su.vistar.taskrunner.mapper.SensorDataMapper;
import su.vistar.taskrunner.model.AverageAcceleration;
import su.vistar.taskrunner.model.DataBySection;
import su.vistar.taskrunner.model.Section;

@Component
public class DataBySectionsTask {

    @Autowired
    private SensorDataMapper sensorDataMapper;
    private final int COUNT = 100; //порция обрабатываемых усредненных данных за раз
    private final int PARTS = 4; //кол-во частей, на которые делится секция

    class Point {

        public double lon;
        public double lat;

        public Point(double lon, double lat) {
            this.lon = lon;
            this.lat = lat;
        }

    }

    //усредняем данные по секциям
    @Scheduled(cron = "0 0/2 * 1/1 * ?") //раз в две минуты
    public void run() {
        List<AverageAcceleration> averages = sensorDataMapper.getAverages(COUNT);
        List<Section> sections = sensorDataMapper.getSections(); //образцы участков - секции
        averages.forEach(average -> {
            double lon1, lat1, lon2, lat2, middleLon, middleLat;
            lat1 = average.getLocStart().getLat();
            lon1 = average.getLocStart().getLon();
            lat2 = average.getLocEnd().getLat();
            lon2 = average.getLocEnd().getLon();
            middleLat = (lat1 + lat2) / 2;
            middleLon = (lon1 + lon2) / 2;
            Iterator<Section> iterator = sections.iterator();
            Section currentSection = null;
            Point[] parts = new Point[PARTS];//массив составных частей секций
            boolean find = false;
            //перебор секций и поиск соотвествующей
            int foundSection = -1;
            while (iterator.hasNext() && !find) {
                currentSection = iterator.next();
                parts[0] = new Point(currentSection.getLat1(), currentSection.getLon1());
                parts[1] = new Point(currentSection.getLat2(), currentSection.getLon2());
                parts[2] = new Point(currentSection.getLat3(), currentSection.getLon3());
                parts[3] = new Point(currentSection.getLat4(), currentSection.getLon4());
                double minLat, maxLat, minLon, maxLon;
                for (int i = 0; i < PARTS - 1; i++) {
                    minLat = Math.min(parts[i].lat, parts[i + 1].lat);
                    maxLat = (minLat != parts[i].lat) ? parts[i].lat
                            : parts[i + 1].lat;
                    minLon = Math.min(parts[i].lon, parts[i + 1].lon);
                    maxLon = (minLon != parts[i].lon) ? parts[i].lon
                            : parts[i + 1].lon;
                    if (minLon <= middleLon && middleLon <= maxLon
                            && minLat <= middleLat && middleLat <= maxLat) {
                        find = true;
                        foundSection = currentSection.getSectionID();
                        break;
                    }
                }
            }
            DataBySection dataBySection = sensorDataMapper.getDataBySection(foundSection);
            double ax = dataBySection.getValue_x();
            double ay = dataBySection.getValue_y();
            double az = dataBySection.getValue_z();
            dataBySection.setValue_x((ax + average.getAccelX()) / 2);
            dataBySection.setValue_y((ay + average.getAccelY()) / 2);
            dataBySection.setValue_z((az + average.getAccelZ()) / 2);
            sensorDataMapper.updateDataBySection(dataBySection);
        });
    }

}
