
package su.vistar.taskrunner.model;


public class Location {
    private Long id;
    private Double lon;
    private Double lat;
    private String deviceImei;
    private String dateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public String getDeviceImei() {
        return deviceImei;
    }

    public void setDeviceImei(String deviceImei) {
        this.deviceImei = deviceImei;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDataTime(String dateTime) {
        this.dateTime = dateTime;
    }
    
}
