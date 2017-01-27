
package su.vistar.taskrunner.model;


public class DataBySection {
    Integer id;
    Integer section_id;
    Double value;
    Double value_x;
    Double value_y;
    Double value_z;

    public Double getValue_x() {
        return value_x;
    }

    public void setValue_x(Double value_x) {
        this.value_x = value_x;
    }

    public Double getValue_y() {
        return value_y;
    }

    public void setValue_y(Double value_y) {
        this.value_y = value_y;
    }

    public Double getValue_z() {
        return value_z;
    }

    public void setValue_z(Double value_z) {
        this.value_z = value_z;
    }
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSection_id() {
        return section_id;
    }

    public void setSection_id(Integer section_id) {
        this.section_id = section_id;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
    
}
