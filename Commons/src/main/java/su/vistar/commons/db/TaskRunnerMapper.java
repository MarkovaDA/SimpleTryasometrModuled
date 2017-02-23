
package su.vistar.commons.db;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import su.vistar.commons.model.AverageAcceleration;
import su.vistar.commons.model.DataBySection;
import su.vistar.commons.model.Location;
import su.vistar.commons.model.Section;


public interface TaskRunnerMapper {
    
    
    @Select("select * from tryasometr_v2.sections limit #{from}, #{count}")
    List<Section> selectCountSections(@Param("from")Integer from, @Param("count")Integer count);
      
    @Select("select * from tryasometr_v2.sections where sectionID = #{sectionID}")
    Section getSectionById(@Param("sectionID")Integer sectionID);
        
    @Select("select * from tryasometr_v2.current_locations group by dateTime order by dateTime limit #{count}")
    List<Location> getLastLocation(@Param("count")int count);
    
    @Delete("delete from tryasometr_v2.current_locations limit #{count}")
    void deleteLocations(@Param("count")int count);
    
    @Delete("delete from tryasometr_v2.current_accelerations where dateTime between #{startTime} and #{endTime}")
    void deleteAccelerations(@Param("startTime")String startTime, @Param("endTime")String endTime);
        
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
    
    //средние ускорения по отрезку
    @Select("SELECT avg(accelX) as accelX, " +
	   "avg(accelY) as accelY, " +
	   "avg(accelZ) as accelZ, " +
	   "deviceImei "+
    "FROM tryasometr_v2.current_accelerations where dateTime between #{startTime} and #{endTime} "+
    "group by deviceImei")
    List<AverageAcceleration> averageForLine(@Param("startTime")String startTime, @Param("endTime")String endTime);
    
    @InsertProvider(type = su.vistar.commons.db.SensorDataProvider.class, 
            method="insertAveragedAccelerations")
    void insertAveragedAccelerations(@Param("list")List<AverageAcceleration> list, 
            @Param("locStart")Location start, @Param("locEnd")Location end);
}
