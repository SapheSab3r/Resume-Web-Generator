package anqi.chen.framework;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

// builder pattern
public class Resume {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String url;
    private List<String> skills;
    private List<Education> educations;
    private List<Experience> experiences;
    private List<Project> projects;

    /**
     * Set first name.
     * 
     * @param firstName the first name to set to.
     * @return this object.
     */
    public Resume setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    /**
     * Set last name.
     * 
     * @param lastName the last name to set to.
     * @return this object.
     */
    public Resume setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    /**
     * Set Email.
     * 
     * @param email the email address to set to.
     * @return this object.
     */
    public Resume setEmail(String email) {
        this.email = email;
        return this;
    }

    /**
     * Set phone number.
     * 
     * @param phoneNumber the phone number to set to.
     * @return this object.
     */
    public Resume setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    /**
     * Set url.
     * 
     * @param url the url to set to.
     * @return this object.
     */
    public Resume setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * Set skills.
     * 
     * @param skills the skills to set to.
     * @return this object.
     */
    public Resume setSkills(List<String> skills) {
        this.skills = new ArrayList<>(skills);
        return this;
    }

    /**
     * Set educations.
     * 
     * @param list the educations to set to.
     * @return this object.
     */

    public Resume setEduction(List<Education> educations) {
        this.educations = new ArrayList<>(educations);
        Collections.sort(this.educations);
        return this;
    }

    /**
     * Set experiences.
     * 
     * @param experiences The experience to set to.
     * @return this object.
     */
    public Resume setExperiences(List<Experience> experiences) {
        this.experiences = new ArrayList<>(experiences);
        Collections.sort(this.experiences);
        return this;
    }

    /**
     * Set projects.
     * 
     * @param projects The project to set to.
     * @return this object.
     */
    public Resume setProjects(List<Project> projects) {
        this.projects = new ArrayList<>(projects);
        Collections.sort(this.projects);
        return this;
    }

    /**
     * Return a json string containing all fields in this object.
     * 
     * @return a json string containing all fields in this object.
     */
    public String buildJsonString() {
        return buildJsonObject().toString();
    }

    @Override
    public String toString() {
        return buildJsonObject().toString();
    }

    public JSONObject buildJsonObject() {
        JSONObject json = new JSONObject();
        json.put("firstName", this.firstName);
        json.put("lastName", this.lastName);
        json.put("email", this.email);
        json.put("phoneNumber", this.phoneNumber);
        json.put("url", this.url);
        json.put("educations", this.educations);
        json.put("skills", this.skills);
        json.put("experiences", this.experiences);
        json.put("projects", this.projects);
        return json;
    }

}
