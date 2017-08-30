package highloadcup.entity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Year;
import java.util.Calendar;
import java.util.List;

/**
 * Created by d.asadullin on 17.08.2017.
 * {"user": 84, "location": 54, "visited_at": 957879823, "id": 1, "mark": 2}
 */
public class Visit {
    private int id;
    private int location=-1;
    private int user=-1;
    private Long visited_at;
    private byte mark=-1;

    private User userEntry;
    private Location locationEntry;

    public Visit(int id, int location, int user, Long visitedAt, byte mark) {
        this.id = id;
        this.location = location;
        this.user = user;
        this.visited_at = visitedAt;
        this.mark = mark;
    }

    public void fromVisit(Visit visit) {
        if (visit.getLocation() !=-1)
            this.location = visit.getLocation();
        if (visit.getUser() !=-1)
            this.user = visit.getUser();
        if (visit.getVisited_at() !=null)
            setVisited_at(visit.getVisited_at());
        if (visit.getMark() !=-1)
            this.mark = visit.getMark();
    }

    public Visit() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }

    public Long getVisited_at() {
        return visited_at;
    }

    public void setVisited_at(Long visited_at) {
        this.visited_at = visited_at;
    }

    public byte getMark() {
        return mark;
    }

    public void setMark(byte mark) {
        this.mark = mark;
    }

    public User getUserEntry() {
        return userEntry;
    }

    public void setUserEntry(User userEntry) {
        this.userEntry = userEntry;
    }

    public Location getLocationEntry() {
        return locationEntry;
    }

    public void setLocationEntry(Location locationEntry) {
        this.locationEntry = locationEntry;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb);
        return sb.toString();
    }
    public String toFullString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"mark\":" + getMark() + ",");
        sb.append("\"visited_at\":").append(getVisited_at()).append(",");
        sb.append("\"user\":").append(getUser()).append(",");
        sb.append("\"id\":").append(getId()).append("," );
        sb.append("\"location\":").append(getLocation());
        sb.append("}");
        return sb.toString();
    }


    public String toDebugString() {
        Calendar calendar=Calendar.getInstance();
        calendar.setTimeInMillis( getUserEntry().getBirth_date());
        int age= new Integer( Year.now().getValue()-calendar.get(Calendar.YEAR)).intValue();
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"mark\":" + getMark() + ",");
        sb.append("\"visited_at\":" + getVisited_at() + ",");
        sb.append("\"user\":" + getUser() + ",");
        sb.append("\"id\":" + getId() + "," );
        sb.append("\"location\":" + getLocation()+ "," );
        sb.append("\"distance\":" + getLocationEntry().getDistance()+ "," );
        sb.append("\"age\":" + age);
        sb.append("}");
        return sb.toString();
    }


    public void toString(StringBuilder sb) {
        sb.append("{");
        sb.append("\"mark\":").append(getMark()).append(",");
        sb.append("\"visited_at\":").append(getVisited_at()).append(",");
        sb.append("\"place\":\"").append(getLocationEntry().getPlace()).append("\"");
        sb.append("}");
    }

    public static String toString(List<Visit> visits) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"visits\": [ ");
        if (visits != null) {
            for (Visit visit : visits) {
                visit.toString(sb);
                sb.append(",");
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("]}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Visit visit = (Visit) o;
        return id==visit.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
