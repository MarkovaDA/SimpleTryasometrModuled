package su.vistar.commons.db;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import su.vistar.commons.model.Section;

public interface TryasometrWebMapper {

    @Select("select * from tryasometr_v2.sections where "
            + "(lat1 between #{minLat} and #{maxLat} and "
            + "lon1 between #{minLon} and #{maxLon}) or "
            + "(lat4 between #{minLat} and #{maxLat} and "
            + "lon4 between #{minLon} and #{maxLon})"
    )
    List<Section> selectSectionsByBounds(@Param("minLat") Double minLat, @Param("minLon") Double minLon,
            @Param("maxLat") Double maxLat, @Param("maxLon") Double maxLon);
    
    @Select("select * from tryasometr_v2.sections")
    List<Section> selectAllSections();
    
    @Update("update tryasometr_v2.sections set azimuth1 = #{value1}, azimuth2 = #{value2}, azimuth3 = #{value3} where sectionID = #{sectionID}")
    void calculateMyAzimuth(@Param("value1")Integer value1, @Param("value2")Integer value2, 
    @Param("value3")Integer value3, @Param("sectionID")Integer sectionID);
}
