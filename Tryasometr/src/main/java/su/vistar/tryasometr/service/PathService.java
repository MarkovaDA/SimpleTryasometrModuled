package su.vistar.tryasometr.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import su.vistar.tryasometr.mapper.SensorDataMapper;
import su.vistar.tryasometr.model.Rectangle;
import su.vistar.tryasometr.model.Section;
import su.vistar.tryasometr.model.objectmanager.Feature;
import su.vistar.tryasometr.model.objectmanager.GeoObjectCollection;
import su.vistar.tryasometr.model.objectmanager.Geometry;

@Service
public class PathService {

    @Autowired
    SensorDataMapper sensorDataMapper;

    private final int count = 50;
    private final String lineType = "LineString";
    private final String circleType = "Circle";
    private final String rectType = "Rectangle";
    
    public final double MAX_DISTANCE = 0.000025;
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
                        return currentSection.getSectionID();
                    }
                }
            }
            from += count;
        } while (!portionSection.isEmpty());
        return null;
    }

    public List<Section> findSectionsWhichPointBelongs(Double[] point) {        
        List<Section> filteredSections = sensorDataMapper.selectSectionsByBounds(point[0] - MAX_DISTANCE, point[1] - MAX_DISTANCE,
                point[0] + MAX_DISTANCE, point[1] + MAX_DISTANCE);
        Iterator<Section> iterator = filteredSections.iterator();
        Section currentSection;
        List<Section> sectionIds = new ArrayList<>();
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
                    sectionIds.add(currentSection);
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
            feature.getProperties().put("sectionId", current.getSectionID());
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
    
    public int  evaluateAzimuth(double lat1, double lat2, double lon1, double lon2){
        double phi1 = lat1 * Math.PI/180;
        double phi2 = lat2 * Math.PI /180;
        double delta = (lon2 - lon1) * Math.PI/180;
        double y = Math.sin(delta) * Math.cos(phi2);
        double x = Math.cos(phi1) * Math.sin(phi2) 
                - Math.sin(phi1) * Math.cos(phi2) * Math.cos(delta);
        double sigma = Math.atan2(y, x);
        return (int)((sigma * 180/Math.PI + 360) % 360);
    }
    
    public GeoObjectCollection getRectangleCollection(List<Rectangle> rectangles){
        //координаты углов прямоугольника
        GeoObjectCollection collection = new GeoObjectCollection();
        Iterator<Rectangle> iterator = rectangles.iterator();
        Feature feature;
        Geometry geometry;
        int counter = 1000;
        Rectangle current;
        while (iterator.hasNext()) {
            current = iterator.next();
            feature = new Feature();
            feature.setId(counter++);
            geometry = new Geometry();
            geometry.setType(rectType);
            geometry.getCoordinates().add(current.getBottomPoint());
            geometry.getCoordinates().add(current.getTopPoint());
            feature.setGeometry(geometry);
            feature.getOptions().put("strokeWidth", 2);
            feature.getOptions().put("strokeColor", "#000000");
            feature.getOptions().put("opacity", "0.5");
            System.out.println("feature coordinates");
            System.out.println(current.getBottomPoint()[0].toString() + " " +  current.getBottomPoint()[1].toString());
            System.out.println(current.getTopPoint()[0].toString() + " " + current.getTopPoint()[1].toString());
            collection.getFeatures().add(feature);
            counter++;
        }
        return collection;
    }
}
