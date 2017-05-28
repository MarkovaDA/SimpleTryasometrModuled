package su.vistar.tryasometr.service;


import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import su.vistar.commons.db.TryasometrWebMapper;
import su.vistar.commons.model.Path;
import su.vistar.commons.model.Rectangle;
import su.vistar.commons.model.Section;
import su.vistar.commons.model.Segment;
import su.vistar.tryasometr.model.objectmanager.Feature;
import su.vistar.tryasometr.model.objectmanager.GeoObjectCollection;
import su.vistar.tryasometr.model.objectmanager.Geometry;

@Service
public class PathService {

    @Autowired
    TryasometrWebMapper tryasometrWebMapper;
    //сбоку еще добавить менюшку с цветами
    private final String lineType = "LineString";
    private final String circleType = "Circle";
    private final String rectType = "Rectangle";  
    private final double MAX_DISTANCE = 0.001;
    
    //зеленый, желтый, красный, бордовый
    private final String[] fixedColorPatterns = 
    {   "rgba(32,232,14,1)",
        "rgba(254,254,34,1)", 
        "rgba(255,0,0,1)", 
        "rgba(165,32,25,1)"
    };
    //построение коллекции базовых точек
    public GeoObjectCollection getBasePointsCollection(List<Path> approximatePaths){
        GeoObjectCollection collection = new GeoObjectCollection();
        Random rnd = new Random();
        String colorPattern = "rgba(%d,%d,%d,1)";
        Iterator<Path> pathIterator = approximatePaths.iterator();
        Path currentPath;
        int segmentIndex;
        Feature feature;
        Geometry geometry;
        String rgbaColor;
        while (pathIterator.hasNext()) {
            currentPath = pathIterator.next();
            Iterator<Segment> segmentIterator = currentPath.getSegments().iterator();
            Segment nextSegment;
            segmentIndex = 0;            
            while (segmentIterator.hasNext()) {
                nextSegment = segmentIterator.next();
                //цвет подбирать в зависимости от сегмента
                rgbaColor = String.format(colorPattern, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));                        
                for(int j=0; j < nextSegment.getPoints().size(); j++){
                    feature = new Feature();
                    feature.setId(segmentIndex + j);
                    geometry = new Geometry();
                    geometry.setType(circleType);
                    geometry.getCoordinates().add(new Double[]{nextSegment.getPoints().get(j)[0],
                        nextSegment.getPoints().get(j)[1]
                    });
                    feature.setGeometry(geometry);
                    feature.getOptions().put("strokeWidth", 2);
                    feature.getOptions().put("strokeColor", "#000000");
                    feature.getOptions().put("fillColor", rgbaColor);
                    collection.getFeatures().add(feature);
                }
                ++segmentIndex;
            }
        }
        return collection;
    }
    
    //построение коллекции секций
    public GeoObjectCollection getSectionCollection(List<Section> sections) {
        GeoObjectCollection collection = new GeoObjectCollection();
        Iterator<Section> iterator = sections.iterator();
        Feature feature;
        Geometry geometry;
        Section current;
        int counter = 0;
        Random rnd = new Random();
        float valueSection;       
        String colorPattern = "rgba(%d,%d,%d,1)";
        //String rgbaColor;
        while (iterator.hasNext()) {
            valueSection = rnd.nextFloat() * 4;//оценка по 4 балльной шкале
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
            //моделируем оценку секции - случайной число от 1 до 4 по степени убывания качества
            feature.getProperties().put("sectionValue", valueSection);
            feature.getOptions().put("strokeWidth", 5);
            //рандомной сгенерированный цвет
            //rgbaColor = String.format(colorPattern, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
            feature.getOptions().put("strokeColor", fixedColorPatterns[(int)(valueSection)]);
            collection.getFeatures().add(feature);
            //кругляк обрамляющий
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
        
    public GeoObjectCollection getAllSectionsCollection(){
        return getSectionCollection(tryasometrWebMapper.selectAllSections());
    }
    //вычисление азимута между двумя точками
    public int evaluateAzimuth(double lat1, double lat2, double lon1, double lon2){
        double phi1 = lat1 * Math.PI/180;
        double phi2 = lat2 * Math.PI /180;
        double delta = (lon2 - lon1) * Math.PI/180;
        double y = Math.sin(delta) * Math.cos(phi2);
        double x = Math.cos(phi1) * Math.sin(phi2) 
                - Math.sin(phi1) * Math.cos(phi2) * Math.cos(delta);
        double sigma = Math.atan2(y, x);
        return (int)((sigma * 180/Math.PI + 360) % 360);
    }
    
    //построение коллекции обрамляющих прямоугольников
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
            feature.getProperties().put("rectLength", 
            Math.sqrt((current.getBottomPoint()[0] - current.getTopPoint()[0]) * 
                       (current.getBottomPoint()[0] - current.getTopPoint()[0]) + 
                       (current.getBottomPoint()[1] - current.getTopPoint()[1])*
                       (current.getBottomPoint()[1] - current.getTopPoint()[1]))                    
            );
            feature.getOptions().put("strokeWidth", 2);
            feature.getOptions().put("strokeColor", "#000000");
            feature.getOptions().put("opacity", "0.5");
            collection.getFeatures().add(feature);
            counter++;
        }
        return collection;
    }

    //рассчет евклидова расстояния
    public double euclideanDistance(Double[] point1, Double[] point2){
        return Math.sqrt((point1[0] - point2[0])*(point1[0] - point2[0]) 
               +  (point1[1] - point2[1])*(point1[1] - point2[1]));
    }
    
    //получение прямоугольника по опорным точек диагонали
    public Rectangle getRectangleByPoints(Double[] prevPoint, Double[] nextPoint){
        Double[] bottomPoint = new Double[2];
        Double[] topPoint = new Double[2];
        bottomPoint[0] = Math.min(prevPoint[0], nextPoint[0]) - MAX_DISTANCE;
        bottomPoint[1] = Math.min(prevPoint[1], nextPoint[1]) - MAX_DISTANCE;
        topPoint[0] = Math.max(prevPoint[0], nextPoint[0]) + MAX_DISTANCE;
        topPoint[1] = Math.max(prevPoint[1], nextPoint[1]) + MAX_DISTANCE;
        return new Rectangle(bottomPoint, topPoint);
    }
    
    public List<Section> getSectionsIntoRectangle(Double[] prevPoint, Double[] nextPoint){
        Double[] bottomPoint = new Double[2];
        Double[] topPoint = new Double[2];
        bottomPoint[0] = Math.min(prevPoint[0], nextPoint[0]) - MAX_DISTANCE;
        bottomPoint[1] = Math.min(prevPoint[1], nextPoint[1]) - MAX_DISTANCE;
        topPoint[0] = Math.max(prevPoint[0], nextPoint[0]) + MAX_DISTANCE;
        topPoint[1] = Math.max(prevPoint[1], nextPoint[1]) + MAX_DISTANCE;
        return tryasometrWebMapper.selectSectionsByBounds(bottomPoint[0], bottomPoint[1], topPoint[0], topPoint[1]);
    }
}
