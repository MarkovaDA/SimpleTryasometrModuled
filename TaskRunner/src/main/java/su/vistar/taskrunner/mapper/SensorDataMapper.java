
package su.vistar.taskrunner.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import su.vistar.taskrunner.model.Location;


public interface SensorDataMapper {   
    @Select("select * from tryasometr_v2.current_locations limit 100")
    List<Location> getLastLocation();
}
