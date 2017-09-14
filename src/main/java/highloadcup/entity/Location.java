package highloadcup.entity;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by d.asadullin on 17.08.2017.
 */
public class Location {
    private int id;
    private String place;
    private String country;
    private String city;
    private int distance=-1;
    private byte[] source;
    private List<Visit> lst=new ArrayList<>();

    public Location(int id, String place, String country, String city, int distance) {
        this.id = id;
        this.place = place;
        this.country = country;
        this.city = city;
        this.distance = distance;
    }

    public Location() {
    }

    public void fromLocation(Location location) {
        if (location.getPlace() != null)
            this.place = location.getPlace();
        if (location.getCountry() != null)
            this.country = location.getCountry();
        if (location.getCity() != null)
            this.city = location.getCity();
        if (location.getDistance() !=-1)
            this.distance = location.getDistance();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public byte[] getSource() {
        return source;
    }

    public void setSource(byte[] source) {
        this.source = source;
    }

    public List<Visit> getLst() {
        return lst;
    }

    public byte[] toJson(){
        return source;
    }

    public void refreshSource(){
        source=toString().getBytes(StandardCharsets.UTF_8);
    }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(getId()).append(",");
        sb.append("\"place\":\"").append( getPlace()).append("\",");
        sb.append("\"country\":\"").append(getCountry()).append("\",");
        sb.append("\"city\":\"").append(getCity()).append("\",");
        sb.append("\"distance\":").append(getDistance());
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Location location = (Location) o;

        return id == location.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
