package anqi.chen.framework;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.json.JSONObject;

public class Project implements Comparable {
    private String title;
    private List<String> skills;
    private String startDate;
    private String endDate;
    private List<String> description;

    public Project(String title, List<String> skills, String startDate, String endDate,
            List<String> description) {
        if (title.isEmpty() || skills.isEmpty() ||
                startDate.isEmpty() || endDate.isEmpty() || description.isEmpty()) {
            throw new IllegalArgumentException("Empty parameter!");
        }

        this.title = title;
        this.skills = skills;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getSkills() {
        return skills;
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
        json.put("title", this.title);
        json.put("skills", this.skills);
        json.put("startDate", this.startDate);
        json.put("endDate", this.endDate);
        json.put("description", this.description);
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
        if (!(o instanceof Project)) {
            throw new IllegalArgumentException("Object is not an instance of Project");
        }

        Project p = (Project) o;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);
        LocalDate d1 = parseDate(this.startDate, formatter);
        LocalDate d2 = parseDate(p.startDate, formatter);

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
