package routers.com.model;

public class Customer {
    private String id;
    private String name;
    private Double latitude;
    private Double longitude;
    private Long serviceTime;
    private Long timeWindowStart;
    private Long timeWindowEnd;
    private Long demand;
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Double getLatitude() {
        return latitude;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    public Double getLongitude() {
        return longitude;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    public Long getServiceTime() {
        return serviceTime;
    }
    public void setServiceTime(Long serviceTime) {
        this.serviceTime = serviceTime;
    }
    public Long getTimeWindowStart() {
        return timeWindowStart;
    }
    public void setTimeWindowStart(Long timeWindowStart) {
        this.timeWindowStart = timeWindowStart;
    }
    public Long getTimeWindowEnd() {
        return timeWindowEnd;
    }
    public void setTimeWindowEnd(Long timeWindowEnd) {
        this.timeWindowEnd = timeWindowEnd;
    }
    public Long getDemand() {
        return demand;
    }
    public void setDemand(Long demand) {
        this.demand = demand;
    }
}
