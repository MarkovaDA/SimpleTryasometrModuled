
package su.vistar.commons.model;

/**
 * информация об ускорениях (по осям) в рамках одной секции
 */
public class DataBySection {
    Integer id;
    Integer sectionId;
    Double valueX;
    Double valueY;
    Double valueZ;

    public Double getValueX() {
        return valueX;
    }

    public void setValueX(Double valueX) {
        this.valueX = valueX;
    }

    public Double getValueY() {
        return valueY;
    }

    public void setValueY(Double valueY) {
        this.valueY = valueY;
    }

    public Double getValueZ() {
        return valueZ;
    }

    public void setValueZ(Double valueZ) {
        this.valueZ = valueZ;
    }
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSectionId() {
        return sectionId;
    }

    public void setSectionId(Integer sectionId) {
        this.sectionId = sectionId;
    }
}
