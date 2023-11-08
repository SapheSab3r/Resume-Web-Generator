package anqi.chen.framework;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.json.JSONObject;

public class Education implements Comparable {
    private String schoolName;
    private String major;
    private String startDate;
    private String endDate;
    private String location;
    private List<String> description;

    public Education(String schoolName, String major, String location, String startDate, String endDate,
            List<String> description) {
        if (schoolName.isEmpty() || major.isEmpty() || location.isEmpty() ||
                startDate.isEmpty() || endDate.isEmpty() || description.isEmpty()) {
            throw new IllegalArgumentException("Empty parameter!");
        }

        this.schoolName = schoolName;
        this.major = major;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;

    }

    public String getSchool() {
        return schoolName;
    }

    public String getMajor() {
        return major;
    }

    public String getLocation() {
        return location;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public List<String> getDescription() {
        return description;
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        json.put("School", this.schoolName);
        json.put("Major", this.major);
        json.put("Location", this.location);
        json.put("startTime", this.startDate);
        json.put("endTime", this.endDate);
        return json.toString();
    }

    /**
     * @return Return a json string represent this object.
     */
    public String buildJsonString() {
        return this.toString();
    }

    private LocalDate parseDate(String dateStr, DateTimeFormatter formatter) {
        try {
            return LocalDate.parse(dateStr, formatter);
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public int compareTo(Object o) {
        if (!(o instanceof Education)) {
            throw new IllegalArgumentException("Object is not an instance of Education");
        }

        Education e = (Education) o;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);
        LocalDate d1 = parseDate(this.startDate, formatter);
        LocalDate d2 = parseDate(e.startDate, formatter);

        if (d1 == null && d2 == null) {
            return 0; // Both dates are null, so they are considered equal
        } else if (d1 == null) {
            return -1; // Current object's date is null, so it comes before
        } else if (d2 == null) {
            return 1; // Other object's date is null, so current object comes after
        }

        return d1.compareTo(d2);
    }

}
