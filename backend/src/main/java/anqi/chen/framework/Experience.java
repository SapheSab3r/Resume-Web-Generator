package anqi.chen.framework;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.json.JSONObject;

public class Experience implements Comparable {
    private String title;
    private List<String> description;
    private String startDate;
    private String endDate;
    private String location;
    private String position;

    public Experience(String title, String position, String location, String startDate, String endDate,
            List<String> description) {
        if (title.isEmpty() || position.isEmpty() || location.isEmpty() ||
                startDate.isEmpty() || endDate.isEmpty() || description.isEmpty()) {
            throw new IllegalArgumentException("Empty parameter!");
        }

        this.title = title;
        this.position = position;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;

    }

    public String getTitle() {
        return title;
    }

    public String getPosition() {
        return position;
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
        json.put("Title:", this.title);
        json.put("Position:", this.position);
        json.put("Location:", this.location);
        json.put("StartTime:", this.startDate);
        json.put("EndTime:", this.endDate);
        json.put("Description:", this.description);
        return json.toString();
    }

    /**
     * @return Return a json string represent this object.
     */
    public String buildJsoString() {
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
        if (!(o instanceof Experience)) {
            throw new IllegalArgumentException("Object is not an instance of Experience");
        }

        Experience e = (Experience) o;
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
