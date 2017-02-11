
package su.vistar.tryasometr.model.objectmanager;

import java.util.HashMap;
import java.util.Map;


public class Feature {
    private String type;
    private Integer id;
    
    private Geometry geometry;
    private Map<String, Object> properties;
    private Map<String, Object> options;

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }

    public Feature(){
        this.properties = new HashMap<>();
        this.options = new HashMap<>();
        this.type = "Feature";
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    @Override
    public String toString() {
        return "Feature{" + "type=" + type + ", id=" + id + ", geometry=" + geometry + ", options=" + options + '}';
    }
    
    
}
