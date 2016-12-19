
package su.vistar.taskrunner.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import su.vistar.taskrunner.model.Acceleration;
import su.vistar.taskrunner.model.AverageAcceleration;
import su.vistar.taskrunner.model.Location;


public interface SensorDataMapper {   
    @Select("select * from tryasometr_v2.current_locations order by dateTime limit #{from}, #{count}")
    List<Location> getLastLocation(@Param("from")int from, @Param("count")int count);
    
    @Select("delete from tryasometr_v2.current_locations limit #{from}, #{count}")
    void deleteAllLocations();
    
    //средние ускорения по отрезку
    @Select("SELECT avg(accelX) as accelX, " +
	   "avg(accelY) as accelY, " +
	   "avg(accelZ) as accelZ, " +
	   "deviceImei "+
    "FROM tryasometr_v2.current_accelerations where dateTime between #{startTime} and #{endTime} "+
    "group by deviceImei")
    List<AverageAcceleration> averageForLine(@Param("startTime")String startTime, @Param("endTime")String endTime);
    
    @InsertProvider(type = SensorDataProvider.class, method="insertAveragedAccelerations")
    void insertAveragedAccelerations(@Param("list")List<AverageAcceleration> list, 
            @Param("locStart")Location start, @Param("locEnd")Location end);
    
    
}
