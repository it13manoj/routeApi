package routers.com.model;

import java.util.ArrayList;
import java.util.List;

public class Root {
    private Long endHorizon;
    private String depotId;
    private String depotName;
    private Double depotLatitude;
    private Double depotLongitude;
    private List<Customer> customers = new ArrayList<Customer>();
    private Integer truckCapacity;
    private Double truckSpeed;
    private List<Truck> trucks = new ArrayList<Truck>();
    public Long getEndHorizon() {
        return endHorizon;
    }
    public void setEndHorizon(Long endHorizon) {
        this.endHorizon = endHorizon;
    }
    public String getDepotId() {
        return depotId;
    }
    public void setDepotId(String depotId) {
        this.depotId = depotId;
    }
    public String getDepotName() {
        return depotName;
    }
    public void setDepotName(String depotName) {
        this.depotName = depotName;
    }
    public Double getDepotLatitude() {
        return depotLatitude;
    }
    public void setDepotLatitude(Double depotLatitude) {
        this.depotLatitude = depotLatitude;
    }
    public Double getDepotLongitude() {
        return depotLongitude;
    }
    public void setDepotLongitude(Double depotLongitude) {
        this.depotLongitude = depotLongitude;
    }
    public List<Customer> getCustomers() {
        return customers;
    }
    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }
    public Integer getTruckCapacity() {
        return truckCapacity;
    }
    public void setTruckCapacity(Integer truckCapacity) {
        this.truckCapacity = truckCapacity;
    }
    public Double getTruckSpeed() {
        return truckSpeed;
    }
    public void setTruckSpeed(Double truckSpeed) {
        this.truckSpeed = truckSpeed;
    }
    public List<Truck> getTrucks() {
        return trucks;
    }
    public void setTrucks(List<Truck> trucks) {
        this.trucks = trucks;
    }
}
