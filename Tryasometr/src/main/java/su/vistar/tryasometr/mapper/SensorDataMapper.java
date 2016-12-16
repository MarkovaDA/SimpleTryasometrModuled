
package su.vistar.tryasometr.mapper;

import su.vistar.tryasometr.model.Acceleration;
import su.vistar.tryasometr.model.Location;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;


public interface SensorDataMapper {
    
    @InsertProvider(type = SensorDataProvider.class, method="insertListOfLocations")
    void insertListOfLocations(@Param("list")List<Location> list);
    
    @InsertProvider(type = SensorDataProvider.class, method="insertListOfAccelerations")
    void insertListOfAcceleration(@Param("list")List<Acceleration> list);

    @Insert("insert into tryasometr_v2.current_locations (lon,lat,deviceImei,dateTime) values(#{lon},#{lat},#{deviceImei},#{dataTime})")
    void insertLocation(Location location);
    
    @Insert("insert into tryasometr_v2.current_accelerations values(#{accelX},#{accelY},#{accelZ},#{deviceImei},#{dataTime})")
    void insertAcceleration(Acceleration acceleration);
       
}
