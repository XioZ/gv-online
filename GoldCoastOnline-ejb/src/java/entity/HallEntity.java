/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Chuck
 */
@XmlRootElement // @XmlRootElement is for Jersey to serialize entity to JSON
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class HallEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int number; // hall no. indexed from 1 onwards per cinema upon creation
    // assuming seating layout is 2D rectangular grid 
    private Integer numOfRows = 0; // range from A to Z
    private Integer numOfColumns = 0; // range from 1 to N
    // stores status of each seat: 'NORMAL', 'EMPTY' or 'HANDICAPPED'
    // row[0] specifies screen position (FROM .. TO ..)
    // row[1] onwards: [1][0] corresponds to seat A1
    private String[][] seatingPlan;

    @XmlTransient
    @ManyToOne(cascade = CascadeType.DETACH)
    private CinemaEntity cinema;

    @OneToMany(cascade = CascadeType.DETACH, mappedBy = "hall")
    private List<ScheduleEntity> schedules = new ArrayList<>();

    public HallEntity() {
    }

    public HallEntity(int number, String[][] seatingPlan) {
        this.number = number;
        if (seatingPlan != null) {
            this.seatingPlan = seatingPlan;
            this.numOfRows = seatingPlan.length;
            this.numOfColumns = seatingPlan.length > 0 ? seatingPlan[0].length : 0;
        }
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String[][] getSeatingPlan() {
        return seatingPlan;
    }

    public void setSeatingPlan(String[][] seatingPlan) {
        if (seatingPlan != null) {
            this.seatingPlan = seatingPlan;
            this.numOfRows = this.seatingPlan.length;
            this.numOfColumns = seatingPlan.length > 0 ? seatingPlan[0].length : 0;
        }
    }

    public CinemaEntity getCinema() {
        return cinema;
    }

    public void setCinema(CinemaEntity cinema) {
        this.cinema = cinema;
    }

    public List<ScheduleEntity> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<ScheduleEntity> schedules) {
        this.schedules = schedules;
    }

    public Integer getNumOfRows() {
        return numOfRows;
    }

    public void setNumOfRows(Integer numOfRows) {
        this.numOfRows = numOfRows;
    }

    public Integer getNumOfColumns() {
        return numOfColumns;
    }

    public void setNumOfColumns(Integer numOfColumns) {
        this.numOfColumns = numOfColumns;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof HallEntity)) {
            return false;
        }
        HallEntity other = (HallEntity) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.HallEntity[ id=" + id + " ]";
    }

}
