
package su.vistar.tryasometr.model.objectmanager;

import java.util.ArrayList;
import java.util.List;


public class Geometry {
    private List<Double[]> coordinates;

    public List<Double[]> getCoordinates() {
        return coordinates;
    }

    private String type;
    private Integer radius;

    public Integer getRadius() {
        return radius;
    }

    public void setRadius(Integer radius) {
        this.radius = radius;
    }
    
    public Geometry() {
        coordinates = new ArrayList<>();
        this.radius = 15;
    }

    public Geometry(double[] coordinates, String type) {
        this.type = type;
    }
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
}
