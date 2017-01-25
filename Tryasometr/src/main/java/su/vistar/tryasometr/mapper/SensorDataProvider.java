package su.vistar.tryasometr.mapper;

import su.vistar.tryasometr.model.Acceleration;
import su.vistar.tryasometr.model.Location;
import java.util.List;
import java.util.Map;



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
        String str = query.toString();
        return query.toString();
    }
}
