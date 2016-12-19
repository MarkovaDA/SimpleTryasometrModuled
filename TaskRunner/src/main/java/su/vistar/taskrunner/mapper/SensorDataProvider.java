package su.vistar.taskrunner.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.jdbc.SQL;
import su.vistar.taskrunner.model.AverageAcceleration;
import su.vistar.taskrunner.model.Location;



public class SensorDataProvider {
    //сохранение усредненного ускорения для отрезка
    public String insertAveragedAccelerations(Map params){
        
        List<AverageAcceleration> locations = (List<AverageAcceleration>)params.get("list"); 
        Location startPoint = (Location)params.get("locStart");
        Location endPoint = (Location)params.get("locEnd");
        StringBuilder query = 
            new StringBuilder("INSERT INTO tryasometr_v2.average_accelerations "
                    + "(accelX,accelY,accelZ,lat1,lon1,lat2,lon2,deviceImei) VALUES ");
        String comma = ",";
        String leftBkt = "(";
        String rightBkt = ")";
        locations.forEach(average -> query
                .append(leftBkt)
                .append(average.getAccelX())
                .append(comma)
                .append(average.getAccelY())
                .append(comma)
                .append(average.getAccelZ())
                .append(comma)
                .append(startPoint.getLat())
                .append(startPoint.getLon())
                .append(average.getLocStart().getLon())
                .append(comma)
                .append(endPoint.getLat())
                .append(comma)
                .append(endPoint.getLon())
                .append(comma)
                .append("'" + average.getDeviceImei() + "'")
                .append(comma)
                .append(rightBkt)
                .append(comma)
        );
        query.deleteCharAt(query.length() - 1);
        String queryString = query.toString();
        return query.toString();
    }
}
