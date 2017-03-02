package su.vistar.commons.db;

import java.util.Iterator;
import su.vistar.commons.model.Acceleration;
import su.vistar.commons.model.Location;
import java.util.List;
import java.util.Map;
import su.vistar.commons.model.AverageAcceleration;

public class SensorDataProvider {
    
    public String insertListOfLocations(Map params){
        
        List<Location> locations = (List<Location>)params.get("list");       
        StringBuilder query = 
            new StringBuilder("INSERT INTO tryasometr_v2.current_locations (lon,lat,deviceImei,dateTime) VALUES ");
        String comma = ",";
        String leftBkt = "(";
        String rightBkt = ")";
        locations.forEach(point -> query
                .append(leftBkt)
                .append(point.getLon())
                .append(comma)
                .append(point.getLat())
                .append(comma)
                .append("'" + point.getDeviceImei() + "'")
                .append(comma)
                .append("'" + point.getDataTime() + "'")
                .append(rightBkt)
                .append(comma)
        );
        query.deleteCharAt(query.length() - 1);
        return query.toString();
    }
    
    public String insertListOfAccelerations(Map params){
        List<Acceleration> accelerations = (List<Acceleration>)params.get("list");       
        StringBuilder query = 
            new StringBuilder("INSERT INTO tryasometr_v2.current_accelerations (accelX,accelY,accelZ,deviceImei,dateTime) VALUES ");
        String comma = ",";
        String leftBkt = "(";
        String rightBkt = ")";
        accelerations.forEach(point -> query
                .append(leftBkt)
                .append(point.getAccelX())
                .append(comma)
                .append(point.getAccelY())
                .append(comma)
                .append(point.getAccelZ())
                .append(comma)
                .append("'" + point.getDeviceImei() + "'")
                .append(comma)
                .append("'" + point.getDataTime() + "'")
                .append(rightBkt)
                .append(comma)
        );
        query.deleteCharAt(query.length() - 1);
        //String str = query.toString();
        return query.toString();
        
    }

    public String insertAveragedAccelerations(Map<String, Object> params) {

        List<AverageAcceleration> locations = (List<AverageAcceleration>) params.get("list");
        Location startPoint = (Location) params.get("locStart");
        Location endPoint = (Location) params.get("locEnd");
        StringBuilder query
                = new StringBuilder("INSERT INTO tryasometr_v2.average_accelerations "
                        + "(accelX,accelY,accelZ,lat1,lon1,lat2,lon2,deviceImei) VALUES ");
        String comma = ",";
        String leftBkt = "(";
        String rightBkt = ")";
        Iterator<AverageAcceleration> iterator = locations.iterator();
        AverageAcceleration average;
        while (iterator.hasNext()) {
            average = iterator.next();
            query.append(leftBkt)
                .append(average.getAccelX())
                .append(comma)
                .append(average.getAccelY())
                .append(comma)
                .append(average.getAccelZ())
                .append(comma)
                .append(startPoint.getLat())
                .append(comma)
                .append(startPoint.getLon())
                .append(comma)
                .append(endPoint.getLat())
                .append(comma)
                .append(endPoint.getLon())
                .append(comma)
                .append("'" + average.getDeviceImei() + "'")
                .append(rightBkt)
                .append(comma);
        }
        /*locations.forEach(average -> query
                .append(leftBkt)
                .append(average.getAccelX())
                .append(comma)
                .append(average.getAccelY())
                .append(comma)
                .append(average.getAccelZ())
                .append(comma)
                .append(startPoint.getLat())
                .append(comma)
                .append(startPoint.getLon())
                .append(comma)
                .append(endPoint.getLat())
                .append(comma)
                .append(endPoint.getLon())
                .append(comma)
                .append("'" + average.getDeviceImei() + "'")
                .append(rightBkt)
                .append(comma)
        );*/
        query = query.deleteCharAt(query.length() - 1); //отменить присваивание
        return query.toString();
    }
}
