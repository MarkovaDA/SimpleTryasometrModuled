
package su.vistar.commons.model;


public class Rectangle {
    private Double[] bottomPoint;
    private Double[] topPoint;

    public Rectangle(Double[] point1, Double[] point2) {
        this.bottomPoint = point1;
        this.topPoint = point2;
    }
    

    public Double[] getBottomPoint() {
        return bottomPoint;
    }

    public void setBottomPoint(Double[] bottomPoint) {
        this.bottomPoint = bottomPoint;
    }

    public Double[] getTopPoint() {
        return topPoint;
    }

    public void setTopPoint(Double[] topPoint) {
        this.topPoint = topPoint;
    }
    
}
