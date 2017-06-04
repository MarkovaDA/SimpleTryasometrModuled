
package su.vistar.commons.model;

/*
*  Модель данных - секция
*/
public class Section {
    private Integer sectionID;
    private Double lat1;
    private Double lon1;
    private Double lat2;
    private Double lon2;
    private Double lat3;
    private Double lon3;
    private Double lat4;
    private Double lon4;
    private Integer azimuth1;
    private Integer azimuth2;

    public Integer getAzimuth2() {
        return azimuth2;
    }

    public void setAzimuth2(Integer azimuth2) {
        this.azimuth2 = azimuth2;
    }

    public Integer getAzimuth3() {
        return azimuth3;
    }

    public void setAzimuth3(Integer azimuth3) {
        this.azimuth3 = azimuth3;
    }
    private Integer azimuth3;

    public Integer getSectionID() {
        return sectionID;
    }

    public void setSectionID(Integer sectionID) {
        this.sectionID = sectionID;
    }

    public Integer getAzimuth1() {
        return azimuth1;
    }

    public void setAzimuth1(Integer azimuth1) {
        this.azimuth1 = azimuth1;
    }
    public Double getLat1() {
        return lat1;
    }

    public void setLat1(Double lat1) {
        this.lat1 = lat1;
    }

    public Double getLon1() {
        return lon1;
    }

    public void setLon1(Double lon1) {
        this.lon1 = lon1;
    }

    public Double getLat2() {
        return lat2;
    }

    public void setLat2(Double lat2) {
        this.lat2 = lat2;
    }

    public Double getLon2() {
        return lon2;
    }

    public void setLon2(Double lon2) {
        this.lon2 = lon2;
    }

    public Double getLat3() {
        return lat3;
    }

    public void setLat3(Double lat3) {
        this.lat3 = lat3;
    }

    public Double getLon3() {
        return lon3;
    }

    public void setLon3(Double lon3) {
        this.lon3 = lon3;
    }

    public Double getLat4() {
        return lat4;
    }

    public void setLat4(Double lat4) {
        this.lat4 = lat4;
    }

    public Double getLon4() {
        return lon4;
    }

    public void setLon4(Double lon4) {
        this.lon4 = lon4;
    }

    @Override
    public String toString() {
        return "Section{" + "sectionID=" + sectionID + ", azimuth1=" + azimuth1 + ", azimuth2=" + azimuth2 + ", azimuth3=" + azimuth3 + '}';
    }
     
}
