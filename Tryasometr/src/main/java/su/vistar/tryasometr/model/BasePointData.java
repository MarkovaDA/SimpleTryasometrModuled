package su.vistar.tryasometr.model;


public class BasePointData {
    private int pathNumber; //какому пути
    private int segmentNumber; //какому сегменту принадлежит
    private boolean endOfSegment = false;

    public boolean isEndOfSegment() {
        return endOfSegment;
    }

    public void setEndOfSegment(boolean endOfSegment) {
        this.endOfSegment = endOfSegment;
    }
    
    public BasePointData(int pathNumber, int segmentNumber) {
        this.pathNumber = pathNumber;
        this.segmentNumber = segmentNumber;
    }   
    public int getPathNumber() {
        return pathNumber;
    }

    public void setPathNumber(int pathNumber) {
        this.pathNumber = pathNumber;
    }

    public int getSegmentNumber() {
        return segmentNumber;
    }

    public void setSegmentNumber(int segmentNumber) {
        this.segmentNumber = segmentNumber;
    }
    
    
}
