package su.vistar.commons.model;
/**
 * усредненное ускорение по отрезку
 */
public class AverageAcceleration {

    private float accelX;
    private float accelY;
    private float accelZ;
    private String deviceImei;
    private Location locStart;
    private Location locEnd;

    public String getDeviceImei() {
        return deviceImei;
    }

    public void setDeviceImei(String deviceImei) {
        this.deviceImei = deviceImei;
    }

    public float getAccelX() {
        return accelX;
    }

    public void setAccelX(float accelX) {
        this.accelX = accelX;
    }

    public float getAccelY() {
        return accelY;
    }

    public void setAccelY(float accelY) {
        this.accelY = accelY;
    }

    public float getAccelZ() {
        return accelZ;
    }

    public void setAccelZ(float accelZ) {
        this.accelZ = accelZ;
    }

    public Location getLocStart() {
        return locStart;
    }

    public void setLocStart(Location locStart) {
        this.locStart = locStart;
    }

    public Location getLocEnd() {
        return locEnd;
    }

    public void setLocEnd(Location locEnd) {
        this.locEnd = locEnd;
    }
}
