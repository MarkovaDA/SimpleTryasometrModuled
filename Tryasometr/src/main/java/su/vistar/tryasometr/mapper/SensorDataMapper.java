
package su.vistar.tryasometr.mapper;

import su.vistar.tryasometr.model.Acceleration;
import su.vistar.tryasometr.model.Location;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import su.vistar.tryasometr.model.Section;


public interface SensorDataMapper {
    
    @InsertProvider(type = SensorDataProvider.class, method="insertListOfLocations")
    void insertListOfLocations(@Param("list")List<Location> list);
    
    @InsertProvider(type = SensorDataProvider.class, method="insertListOfAccelerations")
    void insertListOfAcceleration(@Param("list")List<Acceleration> list);

    @Insert("insert into tryasometr_v2.current_locations (lon,lat,deviceImei,dateTime) values(#{lon},#{lat},#{deviceImei},#{dataTime})")
    void insertLocation(Location location);
    
    @Insert("insert into tryasometr_v2.current_accelerations values(#{accelX},#{accelY},#{accelZ},#{deviceImei},#{dataTime})")
    void insertAcceleration(Acceleration acceleration);
    
    @Select("select * from tryasometr_v2.sections")
    List<Section> selectAllSections();
    
    @Insert("insert into tryasometr_v2.sections_params (k1,k2,k3,m1,m2,m3,section_id) values (#{k1},#{k2},#{k3},#{m1},#{m2},#{m3},#{section_id})")
    void insertSectionParamThree(@Param("k1")Double k1, @Param("k2")Double k2, @Param("k3")Double k3, @Param("m1")Double m1, @Param("m2")Double m2, @Param("m3")Double m3, @Param("section_id")Integer section_id);
    
    @Insert("insert into tryasometr_v2.sections_params (k1,m1,section_id) values (#{k1},#{m1},#{section_id})")
    void insertSectionParamOne(@Param("k1")Double k1, @Param("m1")Double m1, @Param("section_id")Integer section_id);
    
    @Insert("insert into tryasometr_v2.sections_params (k1,k2,m1,m2,section_id) values (#{k1},#{k2},#{m1},#{m2},#{section_id})")
    void insertSectionParamTwo(@Param("k1")Double k1, @Param("k2")Double k2,  @Param("m1")Double m1, @Param("m2")Double m2, @Param("section_id")Integer section_id);
    
}
