package su.vistar.tryasometr.model;


public class MapBounds {
    private double[] bottomCorner;
    private double[] topCorner;

    public MapBounds() {
        this.bottomCorner = new double[2];
        this.topCorner = new double[2];
    }

    public double[] getBottomCorner() {
        return bottomCorner;
    }

    public void setBottomCorner(double[] bottomCorner) {
        this.bottomCorner = bottomCorner;
    }

    public double[] getTopCorner() {
        return topCorner;
    }

    public void setTopCorner(double[] topCorner) {
        this.topCorner = topCorner;
    }
}
