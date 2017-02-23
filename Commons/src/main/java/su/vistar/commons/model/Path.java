
package su.vistar.commons.model;

import java.util.ArrayList;
import java.util.List;

public class Path {
    private int id;
    private List<Segment> segments;
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Segment> getSegments() {
        return segments;
    }

    public void setSegments(List<Segment> segments) {
        this.segments = segments;
    }
    
}
