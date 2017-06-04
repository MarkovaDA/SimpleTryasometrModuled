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
public class GeoObjectService {

    @Autowired
    TryasometrWebMapper tryasometrWebMapper;

    private final String lineType = "LineString";
    private final String circleType = "Circle";
    private final String rectType = "Rectangle";  
   
    
   /*enum COLOR{
        GREEN("rgba(32,232,14,1)"),
        YELLOW("rgba(254,254,34,1)"),
        RED("rgba(255,0,0,1)"),
        PURPLE("rgba(165,32,25,1)");
        private String colorPattern;       
        private COLOR(String pattern) {
            this.colorPattern = pattern;
        }
        public String getPattern() {
            return colorPattern;
        }
    }*/
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

}
