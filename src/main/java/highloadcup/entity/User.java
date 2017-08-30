package highloadcup.entity;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by d.asadullin on 17.08.2017.
 */
public class User {
    private int id;
    private String email;
    private String first_name;
    private String last_name;
    private Boolean gender;
    private Long birth_date;
    private byte[] source;
    private List<Visit> lst = new ArrayList<>();
    public static Instant now = Instant.now();
    // private Integer age;


    public User(int id, String email, String first_name, String last_name, Boolean gender, Long birth_date) {
        this.id = id;
        this.email = email;
        this.first_name = first_name;
        this.last_name = last_name;
        this.birth_date = birth_date;
        this.gender = gender;
    }

    public User() {
    }

    public void fromUser(User user) {
        if (user.getEmail() != null)
            this.email = user.getEmail();
        if (user.getFirst_name() != null)
            this.first_name = user.getFirst_name();
        if (user.getLast_name() != null)
            this.last_name = user.getLast_name();
        if (user.getGender() != null)
            this.gender = user.getGender();
        if (user.getBirth_date() != null) {
            this.birth_date = user.getBirth_date();
        }
    }

    public List<Visit> getLst() {
        return lst;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public Boolean getGender() {
        return gender;
    }

    public void setGender(Boolean gender) {
        this.gender = gender;
    }

    public Long getBirth_date() {
        return birth_date;
    }

    public void setBirth_date(Long birth_date) {
        this.birth_date = birth_date;
    }

//    public boolean after(Integer year) {
//        return age<year;
//    }

    public boolean after(Integer year) {
        //   return age>year;
        long time = ZonedDateTime.ofInstant(now, ZoneId.systemDefault()).minusYears(year).toEpochSecond();
        return getBirth_date() > time;
    }

    public byte[] getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source.getBytes(StandardCharsets.UTF_8);
    }

    public byte[] toJson() {
        return source;
    }

    public void refreshSource() {
        source = toString().getBytes(StandardCharsets.UTF_8);
        ;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(getId()).append(",");
        sb.append("\"email\":\"").append(getEmail()).append("\",");
        sb.append("\"first_name\":\"").append(getFirst_name()).append("\",");
        sb.append("\"last_name\":\"").append(getLast_name()).append("\",");
        sb.append("\"gender\":\"").append(getGender() ? "m" : "f").append("\",");
        sb.append("\"birth_date\":").append(getBirth_date());
        sb.append("}");
        return sb.toString();
    }
}
