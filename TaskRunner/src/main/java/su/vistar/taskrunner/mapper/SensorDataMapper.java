
package su.vistar.taskrunner.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import su.vistar.taskrunner.model.AverageAcceleration;
import su.vistar.taskrunner.model.DataBySection;
import su.vistar.taskrunner.model.Location;
import su.vistar.taskrunner.model.Section;


public interface SensorDataMapper {   
    @Select("select * from tryasometr_v2.current_locations group by dateTime order by dateTime limit #{count}")
    List<Location> getLastLocation(@Param("count")int count);
    
    @Delete("delete from tryasometr_v2.current_locations limit #{count}")
    void deleteLocations(@Param("count")int count);
    
    @Delete("delete from tryasometr_v2.current_accelerations where dateTime between #{startTime} and #{endTime}")
    void deleteAccelerations(@Param("startTime")String startTime, @Param("endTime")String endTime);
    //средние ускорения по отрезку
    @Select("SELECT avg(accelX) as accelX, " +
	   "avg(accelY) as accelY, " +
	   "avg(accelZ) as accelZ, " +
	   "deviceImei "+
    "FROM tryasometr_v2.current_accelerations where dateTime between #{startTime} and #{endTime} "+
    "group by deviceImei")
    List<AverageAcceleration> averageForLine(@Param("startTime")String startTime, @Param("endTime")String endTime);
    
    @InsertProvider(type = su.vistar.taskrunner.mapper.SensorDataProvider.class, 
            method="insertAveragedAccelerations")
    void insertAveragedAccelerations(@Param("list")List<AverageAcceleration> list, 
            @Param("locStart")Location start, @Param("locEnd")Location end);
    
    @Select("select * from tryasometr_v2.current_accelerations limit #{count}")
    List<AverageAcceleration> getAverages(@Param("count")int count);
    
    @Delete("delete from tryasometr_v2.current_accelerations limit #{count}")
    void deleteAverages(@Param("count")int count);
    
    @Select("select * from tryasometr_v2.sections")
    List<Section> getSections();
    
    @Select("select * from tryasometr_v2.data_by_sections where section_id = #{section_id}")
    DataBySection getDataBySection(@Param("section_id")Integer sectionId);
    
    @Update("update tryasometr_v2.data_by_sections set section_id=#{section_id}, value_x = #{value_x}, value_y = #{value_y}, value_z = #{value_z} where id =#{id}")
    void updateDataBySection(DataBySection data);
}
