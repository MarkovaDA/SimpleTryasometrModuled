package su.vistar.tryasometr.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import su.vistar.commons.db.TryasometrWebMapper;
import su.vistar.commons.model.Path;
import su.vistar.commons.model.Rectangle;
import su.vistar.commons.model.Section;
import su.vistar.commons.model.Segment;

@Service
public class RouteService {
    
    @Autowired
    TryasometrWebMapper tryasometrWebMapper;
    
    private final double MAX_DISTANCE = 0.001;
    
    public List<Section> approimateRouteByBasePoints(List<Path> paths){
        Iterator<Path> pathIterator = paths.iterator();
        Iterator<Segment> segmentIterator;
        List<Double[]> allPoints = new ArrayList<>();
        Path currentPath;
        Segment nextSegment;
        List<Section> sections = new ArrayList<>();//итоговый набор найденных секций
        List<Section> searchedSectionsForPart;//набор секций, найденных для одного сегмента
        List<Section> filteredSectionsForPart = new ArrayList<>();
        //собрали все точки, формирующие прямоугольник
        while (pathIterator.hasNext()) {
            currentPath = pathIterator.next();
            segmentIterator = currentPath.getSegments().iterator();
            while (segmentIterator.hasNext()) {
                nextSegment = segmentIterator.next();
                allPoints.addAll(nextSegment.getPoints());
            }
        }
        //сама оценка по прямоугольникам
        Double[] prevPoint;
        Double[] nextPoint = null;
        double distanceDiag;
        prevPoint = allPoints.get(0);
        int i = 0;
        System.out.println("кол-во точек:" + allPoints.size());
        while (i < allPoints.size() - 1) {
            int j = i + 1;
            do {
                if (j == allPoints.size()) break;               
                nextPoint = allPoints.get(j);
                distanceDiag = euclideanDistance(prevPoint, nextPoint);
                j++;
            } 
            while (distanceDiag < 0.002); //еще ограничение поставить        
            int azimuth = evaluateAzimuth(prevPoint[0], nextPoint[0], prevPoint[1], nextPoint[1]); //азимут отрезка
            System.out.println("азимут кусочка " + azimuth);
            searchedSectionsForPart = getSectionsIntoRectangle(prevPoint, nextPoint);
            System.out.println("число секций до фильтрации" + searchedSectionsForPart.size());     
            searchedSectionsForPart.forEach(item -> {
                System.out.println(item);
            });
            searchedSectionsForPart = searchedSectionsForPart.stream()
                    .filter(p -> Math.abs(p.getAzimuth1() - azimuth) <= 30
                            || Math.abs(p.getAzimuth2() - azimuth) <= 30
                            || Math.abs(p.getAzimuth3() - azimuth) <= 30
                    )
                    .collect(Collectors.toList());           
            sections.addAll(searchedSectionsForPart);
            System.out.println("число секций после фильтрации" + filteredSectionsForPart.size());
            filteredSectionsForPart.forEach(item -> {
                System.out.println(item);
            });
            prevPoint = nextPoint;
            i = j;
        }
        return sections;
    }
    
    public List<Rectangle> getWrappedRectangles(List<Path> paths){
        Iterator<Path> pathIterator = paths.iterator();
        List<Rectangle> rectangles = new ArrayList<>();
        Path currentPath;
        List<Double[]> allPoints = new ArrayList<>();       
        Double[] nextPoint = null;
        Double[] prevPoint;
        while (pathIterator.hasNext()) {
            currentPath = pathIterator.next();
            Iterator<Segment> segmentIterator = currentPath.getSegments().iterator();
            Segment nextSegment;
            while (segmentIterator.hasNext()) {
                nextSegment = segmentIterator.next();
                allPoints.addAll(nextSegment.getPoints());
            }
        }
        
        double distanceDiag;
        prevPoint = allPoints.get(0);
        int i = 0;
        System.out.println("кол-во точек:" + allPoints.size());
        while (i < allPoints.size() - 1) {
            int j = i + 1;
            do {
                if (j == allPoints.size()) break;               
                nextPoint = allPoints.get(j);
                distanceDiag = euclideanDistance(prevPoint, nextPoint);
                j++;
            } 
            while (distanceDiag < 0.002); //еще ограничение поставить        
            rectangles.add(getRectangleByPoints(prevPoint, nextPoint));
            prevPoint = nextPoint;
            System.out.println("i=" + i + ",j="+j);
            i = j;
        }
        return rectangles;
    }
    //вычисление азимута
    int evaluateAzimuth(double lat1, double lat2, double lon1, double lon2){
        double phi1 = lat1 * Math.PI/180;
        double phi2 = lat2 * Math.PI /180;
        double delta = (lon2 - lon1) * Math.PI/180;
        double y = Math.sin(delta) * Math.cos(phi2);
        double x = Math.cos(phi1) * Math.sin(phi2) 
                - Math.sin(phi1) * Math.cos(phi2) * Math.cos(delta);
        double sigma = Math.atan2(y, x);
        return (int)((sigma * 180/Math.PI + 360) % 360);
    }
    
    List<Section> getSectionsIntoRectangle(Double[] prevPoint, Double[] nextPoint){
        Double[] bottomPoint = new Double[2];
        Double[] topPoint = new Double[2];
        bottomPoint[0] = Math.min(prevPoint[0], nextPoint[0]) - MAX_DISTANCE;
        bottomPoint[1] = Math.min(prevPoint[1], nextPoint[1]) - MAX_DISTANCE;
        topPoint[0] = Math.max(prevPoint[0], nextPoint[0]) + MAX_DISTANCE;
        topPoint[1] = Math.max(prevPoint[1], nextPoint[1]) + MAX_DISTANCE;
        return tryasometrWebMapper.selectSectionsByBounds(bottomPoint[0], bottomPoint[1], topPoint[0], topPoint[1]);
    }
     //рассчет евклидова расстояния
    double euclideanDistance(Double[] point1, Double[] point2){
        return Math.sqrt((point1[0] - point2[0])*(point1[0] - point2[0]) 
               +  (point1[1] - point2[1])*(point1[1] - point2[1]));
    }
    
    //получение прямоугольника по опорным точкам диагонали
    Rectangle getRectangleByPoints(Double[] prevPoint, Double[] nextPoint){
        Double[] bottomPoint = new Double[2];
        Double[] topPoint = new Double[2];
        bottomPoint[0] = Math.min(prevPoint[0], nextPoint[0]) - MAX_DISTANCE;
        bottomPoint[1] = Math.min(prevPoint[1], nextPoint[1]) - MAX_DISTANCE;
        topPoint[0] = Math.max(prevPoint[0], nextPoint[0]) + MAX_DISTANCE;
        topPoint[1] = Math.max(prevPoint[1], nextPoint[1]) + MAX_DISTANCE;
        return new Rectangle(bottomPoint, topPoint);
    }   
}
