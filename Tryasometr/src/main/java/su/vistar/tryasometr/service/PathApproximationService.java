package su.vistar.tryasometr.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import su.vistar.tryasometr.mapper.SensorDataMapper;
import su.vistar.tryasometr.model.Section;
import su.vistar.tryasometr.model.objectmanager.Feature;
import su.vistar.tryasometr.model.objectmanager.GeoObjectCollection;
import su.vistar.tryasometr.model.objectmanager.Geometry;

@Service
public class PathApproximationService {

    @Autowired
    SensorDataMapper sensorDataMapper;

    private final int count = 50;
    private final String lineType = "LineString";
    private final String circleType = "Circle";
    private final double maxDistance = 0.0401916349175386;
    private final String reportStr = "point=[%.8f,%.8f],section_id=%d";

    public Integer getAppropriateSections(Double[] point) {
        List<Section> portionSection;
        int from = 0;
        do {
            portionSection = sensorDataMapper.selectCountSections(from, count);
            Iterator<Section> iterator = portionSection.iterator();
            Section currentSection;
            double minLat, maxLat, minLon, maxLon;
            while (iterator.hasNext()) {
                currentSection = iterator.next();
                double[] lons = {currentSection.getLat1(), currentSection.getLat2(), currentSection.getLat3(), currentSection.getLat4()};
                double[] lats = {currentSection.getLon1(), currentSection.getLon2(), currentSection.getLon3(), currentSection.getLon4()};
                for (int j = 0; j < lons.length - 1; j++) {
                    minLat = (lats[j] < lats[j + 1]) ? lats[j] : lats[j + 1];
                    minLon = (lons[j] < lons[j + 1]) ? lons[j] : lons[j + 1];
                    maxLat = (lats[j] > lats[j + 1]) ? lats[j] : lats[j + 1];
                    maxLon = (lons[j] > lons[j + 1]) ? lons[j] : lons[j + 1];

                    if ((minLat <= point[1] && point[1] <= maxLat)
                            && (minLon <= point[0] && point[0] <= maxLon)) {
                        String output = String.format(reportStr, point[0], point[1], minLat, maxLat, minLon, maxLon);
                        System.out.println(output);
                        return currentSection.getSeсtionID();
                    }
                }
            }
            from += count;
        } while (!portionSection.isEmpty());
        return null;
    }

    public List<Integer> findSectionsWhichPointBelongs(Double[] point) {        
        List<Section> filteredSections = sensorDataMapper.selectSectionsByBounds(point[0] - maxDistance, point[1] - maxDistance,
                point[0] + maxDistance, point[1] + maxDistance);
        Iterator<Section> iterator = filteredSections.iterator();
        Section currentSection;
        List<Integer> sectionIds = new ArrayList<>();
        double minLat, maxLat, minLon, maxLon;
        while (iterator.hasNext()) {
            currentSection = iterator.next();
            double[] lats = {currentSection.getLat1(), currentSection.getLat2(), currentSection.getLat3(), currentSection.getLat4()};
            double[] lons = {currentSection.getLon1(), currentSection.getLon2(), currentSection.getLon3(), currentSection.getLon4()};
            for (int j = 0; j < lons.length - 1; j++) {
                minLat = (lats[j] < lats[j + 1]) ? lats[j] : lats[j + 1];
                minLon = (lons[j] < lons[j + 1]) ? lons[j] : lons[j + 1];
                maxLat = (lats[j] > lats[j + 1]) ? lats[j] : lats[j + 1];
                maxLon = (lons[j] > lons[j + 1]) ? lons[j] : lons[j + 1];
                if ((minLat <= point[0] && point[0] <= maxLat)
                        && (minLon <= point[1] && point[1] <= maxLon)) {
                    //String output = String.format(reportStr, point[0], point[1], currentSection.getSeсtionID());
                    //System.out.println(output);
                    sectionIds.add(currentSection.getSeсtionID());
                }
            }
        }
        return sectionIds;
    }

    public GeoObjectCollection getCollection(List<Section> sections) {
        GeoObjectCollection collection = new GeoObjectCollection();
        Iterator<Section> iterator = sections.iterator();
        Feature feature;
        Geometry geometry;
        Section current;
        int counter = 0;
        Random rnd = new Random();
        String colorPattern = "rgba(%d,%d,%d,1)";
        String rgbaColor;
        while (iterator.hasNext()) {
            current = iterator.next();
            feature = new Feature();
            feature.setId(counter++);
            geometry = new Geometry();
            geometry.setType(lineType);
            geometry.getCoordinates().add(new Double[]{current.getLat1(), current.getLon1()});
            geometry.getCoordinates().add(new Double[]{current.getLat2(), current.getLon2()});
            geometry.getCoordinates().add(new Double[]{current.getLat3(), current.getLon3()});
            geometry.getCoordinates().add(new Double[]{current.getLat4(), current.getLon4()});
            feature.setGeometry(geometry);
            feature.getProperties().put("sectionId", current.getSeсtionID());
            feature.getOptions().put("strokeWidth", 5);
            rgbaColor = String.format(colorPattern, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
            feature.getOptions().put("strokeColor", rgbaColor);
            collection.getFeatures().add(feature);

            feature = new Feature();
            feature.setId(counter++);
            geometry = new Geometry();
            geometry.setType(circleType);
            geometry.getCoordinates().add(new Double[]{current.getLat1(), current.getLon1()});
            feature.setGeometry(geometry);
            feature.getOptions().put("strokeWidth", 4);
            feature.getOptions().put("strokeColor", "#000000");
            feature.getOptions().put("fillColor", "#ffffff");
            collection.getFeatures().add(feature);
        }
        return collection;
    }
}
