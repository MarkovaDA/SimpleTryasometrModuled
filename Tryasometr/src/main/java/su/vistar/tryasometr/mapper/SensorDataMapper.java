
package su.vistar.tryasometr.mapper;

import su.vistar.tryasometr.model.Acceleration;
import su.vistar.tryasometr.model.Location;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
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
    
    @Select("select * from tryasometr_v2.sections limit #{from}, #{count}")
    List<Section> selectCountSections(@Param("from")Integer from, @Param("count")Integer count);
    
    @Insert("insert into tryasometr_v2.sections_params (k1,k2,k3,m1,m2,m3,section_id) values (#{k1},#{k2},#{k3},#{m1},#{m2},#{m3},#{section_id})")
    void insertSectionParamThree(@Param("k1")Double k1, @Param("k2")Double k2, @Param("k3")Double k3, @Param("m1")Double m1, @Param("m2")Double m2, @Param("m3")Double m3, @Param("section_id")Integer section_id);
    
    @Insert("insert into tryasometr_v2.sections_params (k1,m1,section_id) values (#{k1},#{m1},#{section_id})")
    void insertSectionParamOne(@Param("k1")Double k1, @Param("m1")Double m1, @Param("section_id")Integer section_id);
    
    @Insert("insert into tryasometr_v2.sections_params (k1,k2,m1,m2,section_id) values (#{k1},#{k2},#{m1},#{m2},#{section_id})")
    void insertSectionParamTwo(@Param("k1")Double k1, @Param("k2")Double k2,  @Param("m1")Double m1, @Param("m2")Double m2, @Param("section_id")Integer section_id);
    
    @Select("select * from tryasometr_v2.sections where "
            + "(lat1 between #{minLat} and #{maxLat} and "
            + "lon1 between #{minLon} and #{maxLon}) or "
            + "(lat4 between #{minLat} and #{maxLat} and "
            + "lon4 between #{minLon} and #{maxLon})"
    )
    List<Section> selectSectionsByBounds(@Param("minLat")Double minLat, @Param("minLon")Double minLon,
                                            @Param("maxLat")Double maxLat, @Param("maxLon")Double maxLon);
    
    @Select("select * from tryasometr_v2.sections where sectionID = #{sectionID}")
    Section getSectionById(@Param("sectionID")Integer sectionID);
    
    @Update("update tryasometr_v2.sections set length = #{dist} where sectionID = #{sectionID}")
    void calculateDistance(@Param("dist")Double dist, @Param("sectionID")Integer sectionID);
    
    @Update("update tryasometr_v2.sections set azimuth1 = #{value1}, azimuth2 = #{value2}, azimuth3 = #{value3} where sectionID = #{sectionID}")
    void calculateMyAzimuth(@Param("value1")Integer value1, @Param("value2")Integer value2, 
    @Param("value3")Integer value3, @Param("sectionID")Integer sectionID);
    
    @Select("select * from tryasometr_v2.sections where azimuth1 between #{start_azimuth} and #{end_azimuth}")
    List<Section> selectSectionByAzimuth(@Param("start_azimuth")Integer startAzimuth, @Param("end_azimuth")Integer endAzimuth);
    
}
