package su.vistar.tryasometr.model.objectmanager;

import java.util.ArrayList;
import java.util.List;


public class GeoObjectCollection {
    private String type;
    private List<Feature> features;

    public GeoObjectCollection() {
        this.type = "FeatureCollection";
        this.features = new ArrayList<>();
    }
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }
}
