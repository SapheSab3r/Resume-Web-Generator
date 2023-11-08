
package anqi.chen.plugins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import anqi.chen.framework.DataPlugin;
import anqi.chen.framework.Experience;
import anqi.chen.framework.Education;
import anqi.chen.framework.ResumeFramework;
import anqi.chen.framework.Project;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import javax.swing.plaf.synth.SynthEditorPaneUI;

import java.util.regex.Matcher;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class PDFPlugin implements DataPlugin {
    private final int num3 = 3;
    private String[] splitDocumentText;

    @Override
    public void parseDataResource(String dataPath) throws IOException {
        PDDocument document = PDDocument.load(new File(dataPath));
        if (!document.isEncrypted()) {
            PDFTextStripper stripper = new PDFTextStripper();
            String documentText = stripper.getText(document);
            splitDocumentText = documentText.split("\\R");
        }
        document.close();
    }

    @Override
    public void onRegister(ResumeFramework framework) {

    }

    @Override
    public String getPluginName() {
        return "PDF";
    }

    @Override
    public String getFirstName() {
        String name = splitDocumentText[0];
        String[] splitName = name.split(" ");
        return splitName[0];
    }

    @Override
    public String getLastName() {
        String name = splitDocumentText[0];
        String[] splitName = name.split(" ");
        return splitName[1];
    }

    @Override
    public String getEmail() {
        String info = splitDocumentText[1];
        Pattern pattern = Pattern.compile("\\S+@\\S+");
        Matcher matcher = pattern.matcher(info);
        if (matcher.find()) {
            return matcher.group();
        }
        return null; // or return an empty string
    }

    @Override
    public String getPhoneNumber() {
        String info = splitDocumentText[1];
        Pattern pattern = Pattern.compile("\\d{3}-\\d{3}-\\d{4}");
        Matcher matcher = pattern.matcher(info);

        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    @Override
    public String getURL() {
        String info = splitDocumentText[1];
        Pattern pattern = Pattern.compile("linkedin\\.com/in/\\S+");
        Matcher matcher = pattern.matcher(info);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    @Override
    public List<String> getSkills() {
        int startIdx = -1;
        List<String> skills = new ArrayList<>();
        for (int i = 0; i < splitDocumentText.length; i++) {
            if (splitDocumentText[i].trim().equals("TECHNICAL SKILLS")) {
                startIdx = i;
                break;
            }
        }
        if (startIdx == -1) {
            return null;
        }

        startIdx += 1;

        // Process lines until "EXPERIENCE" is reached or an empty line is encountered
        while (startIdx < splitDocumentText.length &&
                !splitDocumentText[startIdx].trim().equalsIgnoreCase("EXPERIENCE") &&
                !splitDocumentText[startIdx].trim().isEmpty()) {
            String skillLine = splitDocumentText[startIdx];

            // Split the line using a comma and/or colon as separators
            String[] splitSkills = skillLine.split("[,:]");

            for (String skill : splitSkills) {
                skills.add(skill.trim());
            }

            startIdx++;
        }

        return skills;
    }

    @Override
    public List<Education> getEducation() {
        List<Integer> idxStart = new ArrayList<>();
        List<Education> educations = new ArrayList<>();
        for (int i = 0; i < splitDocumentText.length; i++) {
            if (splitDocumentText[i].trim().equals("EDUCATION")) {
                i++;
                while (i < splitDocumentText.length && !splitDocumentText[i].trim().isEmpty()) {
                    idxStart.add(i);
                    while (i < splitDocumentText.length && !splitDocumentText[i].trim().isEmpty()) {
                        i++; // skip till next empty line
                    }
                }
            }
        }

        for (int idx : idxStart) {
            educations.add(createEducation(idx));
        }

        return educations;

    }

    private Education createEducation(int startIdx) {
        String datePatternString = "(Jan\\.|Feb\\.|Mar\\.|Apr\\.|May|Jun\\.|Jul\\.|Aug\\.|Sep\\.|Oct\\.|Nov\\.|Dec\\.)\\s\\d{4}\\s*[-–—\\?]\\s*(Jan\\.|Feb\\.|Mar\\.|Apr\\.|May|Jun\\.|Jul\\.|Aug\\.|Sep\\.|Oct\\.|Nov\\.|Dec\\.)\\s\\d{4}";

        Pattern datePattern = Pattern.compile(datePatternString);
        String schoolLine = splitDocumentText[startIdx].trim();
        Matcher dateMatcher = datePattern.matcher(schoolLine);

        String school = null;
        String startDate = null;
        String endDate = null;
        String major = null;
        String location = null;

        if (dateMatcher.find()) {
            // Extract the part of the line before the date as the school name
            school = schoolLine.substring(0, dateMatcher.start()).trim();

            String datePortion = schoolLine.substring(dateMatcher.start(), dateMatcher.end());
            String[] dates = datePortion.split("\\s*[-–—\\?]\\s*");
            startDate = dates[0].trim();
            endDate = dates[1].trim();

            // The major and location are assumed to be on the same line
            if (splitDocumentText.length > startIdx + 1) {
                String majorAndLocationLine = splitDocumentText[startIdx + 1].trim();

                // Find the last occurrence of the comma and space as the delimiter
                int lastDelimiterIndex = majorAndLocationLine.lastIndexOf(", ");
                if (lastDelimiterIndex != -1) {
                    major = majorAndLocationLine.substring(0, lastDelimiterIndex).trim();
                    location = majorAndLocationLine.substring(lastDelimiterIndex + 2).trim();
                } else {
                    throw new IllegalArgumentException("Unable to extract major and location.");
                }
            } else {
                throw new IllegalArgumentException("Expected line with major and location not found.");
            }
        } else {
            throw new IllegalArgumentException("No date pattern found in the line: " + schoolLine);
        }

        // Extract other details like GPA and coursework
        List<String> otherDetails = new ArrayList<>();
        int detailsIdx = startIdx + 2; // Start from the line after the major

        while (detailsIdx < splitDocumentText.length
                && !splitDocumentText[detailsIdx].trim().isEmpty()
                && !splitDocumentText[detailsIdx].trim().equalsIgnoreCase("TECHNICAL SKILLS")) {
            String detailLine = splitDocumentText[detailsIdx].trim();

            // Check if the detailLine ends with a comma and the next line is not empty
            if (detailLine.endsWith(",") && detailsIdx + 1 < splitDocumentText.length
                    && !splitDocumentText[detailsIdx + 1].trim().isEmpty()) {
                // Append the next line to the current detailLine and increment the detailsIdx
                detailLine += " " + splitDocumentText[detailsIdx + 1].trim();
                detailsIdx++;
            }

            otherDetails.add(detailLine.replaceAll("•", "\n"));
            detailsIdx++;
        }

        // Validation
        if (school == null || major == null || startDate == null || endDate == null || location == null) {
            throw new IllegalArgumentException("One of the parameters for Education constructor is null.");
        }
        return new Education(school, major, location, startDate, endDate, otherDetails);
    }

    @Override
    public List<Experience> getExperience() {
        List<Integer> idxStart = new ArrayList<>();
        List<Experience> experiences = new ArrayList<>();
        String datePatternString = "(Jan\\.|Feb\\.|Mar\\.|Apr\\.|May|Jun\\.|Jul\\.|Aug\\.|Sep\\.|Oct\\.|Nov\\.|Dec\\.)\\s\\d{4}\\s*[–—-]?\\s*(Jan\\.|Feb\\.|Mar\\.|Apr\\.|May|Jun\\.|Jul\\.|Aug\\.|Sep\\.|Oct\\.|Nov\\.|Dec\\.|Present)\\s?(?:\\d{4})?";

        Pattern datePattern = Pattern.compile(datePatternString);

        for (int i = 0; i < splitDocumentText.length; i++) {

            if (splitDocumentText[i].trim().equals("EXPERIENCE")) {

                i++; // Move to the next line
                while (i < splitDocumentText.length) {
                    Matcher dateMatcher = datePattern.matcher(splitDocumentText[i].trim());

                    if (dateMatcher.find()) {
                        idxStart.add(i);
                    }
                    if (splitDocumentText[i].trim().equalsIgnoreCase("NOTABLE PROJECTS")) {
                        break; // Stop the loop when "NOTABLE PROJECTS" is found
                    }
                    i++;
                }

            }
        }

        for (int idx : idxStart) {
            System.out.print(idxStart);
            experiences.add(createExperience(idx));
        }

        return experiences;
    }

    private Experience createExperience(int startIdx) {
        System.out.println(startIdx);
        String title = null;
        String startDate = null;
        String endDate = null;
        String position = null;
        String location = null;
        List<String> descriptions = new ArrayList<>();

        String datePatternString = "(Jan\\.|Feb\\.|Mar\\.|Apr\\.|May|Jun\\.|Jul\\.|Aug\\.|Sep\\.|Oct\\.|Nov\\.|Dec\\.)\\s\\d{4}\\s*[–—-]?\\s*(Jan\\.|Feb\\.|Mar\\.|Apr\\.|May\\.|Jun\\.|Jul\\.|Aug\\.|Sep\\.|Oct\\.|Nov\\.|Dec\\.|Present)\\s?(?:\\d{4})?";

        Pattern datePattern = Pattern.compile(datePatternString);
        String titleLine = splitDocumentText[startIdx].trim();
        System.out.println(titleLine);
        Matcher dateMatcher = datePattern.matcher(titleLine);

        if (dateMatcher.find()) {
            // Extract the part of the line before the date as the school name
            title = titleLine.substring(0, dateMatcher.start()).trim();
            String datePortion = titleLine.substring(dateMatcher.start(), dateMatcher.end());
            String[] dates = datePortion.split("\\s*[-–—\\?]\\s*");
            if (dates.length >= 2) {
                startDate = dates[0].trim();
                endDate = dates[1].trim();
            } else {
                startDate = endDate = dates[0].trim(); // Set both to the same value if only one date is present
            }

            // The major and location are assumed to be on the same line
            if (splitDocumentText.length > startIdx + 1) {
                String positionAndLocationLine = splitDocumentText[startIdx + 1].trim();

                // Find the last occurrence of the comma and space as the delimiter
                int lastDelimiterIndex = positionAndLocationLine.lastIndexOf(", ");
                if (lastDelimiterIndex != -1) {
                    position = positionAndLocationLine.substring(0, lastDelimiterIndex).trim();
                    location = positionAndLocationLine.substring(lastDelimiterIndex + 2).trim();
                } else {
                    throw new IllegalArgumentException("Unable to extract major and location.");
                }
            } else {
                throw new IllegalArgumentException("Expected line with major and location not found.");
            }
        } else {
            throw new IllegalArgumentException("No date pattern found in the line: " + titleLine);
        }

        int detailsIdx = startIdx + 2; // Start from the line after the position
        while (detailsIdx < splitDocumentText.length
                && !splitDocumentText[detailsIdx].trim().isEmpty()
                && !splitDocumentText[detailsIdx].trim().equalsIgnoreCase("NOTABLE PROJECTS")
                && !splitDocumentText[detailsIdx].trim().equalsIgnoreCase("PROJECTS")) {

            String detailLine = splitDocumentText[detailsIdx];

            /// Check if the line starts with a date pattern
            Matcher dateMatcher2 = datePattern.matcher(detailLine);
            if (dateMatcher2.find()) {
                // If a date pattern is found, it's the start of a new experience section
                break; // Exit the loop as a new experience section has started
            }

            // Check if the line starts with a bullet point (•)
            if (detailLine.startsWith("•")) {
                // Remove the bullet point and trim again to ensure there are no extra spaces
                detailLine = detailLine.substring(1).trim();

                // Check if the detailLine ends with a comma and the next line is not empty
                while (detailLine.endsWith(",")
                        && detailsIdx + 1 < splitDocumentText.length
                        && !splitDocumentText[detailsIdx + 1].trim().isEmpty()
                        && !splitDocumentText[detailsIdx + 1].trim().equalsIgnoreCase("NOTABLE PROJECTS")) {
                    // Append the next line to the current detailLine and increment the detailsIdx
                    detailsIdx++;
                    detailLine += " " + splitDocumentText[detailsIdx].trim();
                }
            }
            descriptions.add(detailLine);
            detailsIdx++;
        }

        // System.out.println("Title: " + title);
        // System.out.println("Position: " + position);
        // System.out.println("Location: " + location);
        // System.out.println("StartDate: " + startDate);
        // System.out.println("EndDate: " + endDate);
        // System.out.println("Descriptions: " + descriptions);

        // Validation
        if (title == null || position == null || startDate == null || endDate == null || location == null) {
            throw new IllegalArgumentException("One of the parameters for Experience constructor is null.");
        }

        return new Experience(title, position, location, startDate, endDate, descriptions);
    }

    @Override
    public List<Project> getProject() {
        List<Integer> startIdxs = new ArrayList<>();
        List<Project> projects = new ArrayList<>();

        String datePatternString = "(Jan\\.|Feb\\.|Mar\\.|Apr\\.|May|Jun\\.|Jul\\.|Aug\\.|Sep\\.|Oct\\.|Nov\\.|Dec\\.)\\s\\d{4}\\s*[–—-]?\\s*(Jan\\.|Feb\\.|Mar\\.|Apr\\.|May|Jun\\.|Jul\\.|Aug\\.|Sep\\.|Oct\\.|Nov\\.|Dec\\.|Present)\\s?(?:\\d{4})?";

        Pattern datePattern = Pattern.compile(datePatternString);

        for (int i = 0; i < splitDocumentText.length; i++) {

            if (splitDocumentText[i].trim().equals("NOTABLE PROJECTS")) {

                i++; // Move to the next line
                while (i < splitDocumentText.length) {
                    Matcher dateMatcher = datePattern.matcher(splitDocumentText[i].trim());

                    if (dateMatcher.find()) {
                        startIdxs.add(i);
                    }

                    i++;
                }

            }
        }

        for (int idx : startIdxs) {
            projects.add(createProject(idx));
        }
        return projects;
    }

    private Project createProject(int startIdx) {
        String title = null;
        List<String> skills = new ArrayList<>();
        String startDate = null;
        String endDate = null;
        List<String> descriptions = new ArrayList<>();

        String datePatternString = "(Jan\\.|Feb\\.|Mar\\.|Apr\\.|May|Jun\\.|Jul\\.|Aug\\.|Sep\\.|Oct\\.|Nov\\.|Dec\\.)\\s\\d{4}\\s*[–—-]?\\s*(Jan\\.|Feb\\.|Mar\\.|Apr\\.|May|Jun\\.|Jul\\.|Aug\\.|Sep\\.|Oct\\.|Nov\\.|Dec\\.|Present)\\s?(?:\\d{4})?";

        Pattern datePattern = Pattern.compile(datePatternString);
        String projectLine = splitDocumentText[startIdx].trim();

        Matcher dateMatcher = datePattern.matcher(projectLine);
        if (dateMatcher.find()) {
            // Extract the date portion as the project title
            title = projectLine.substring(0, dateMatcher.start()).trim();
            String datePortion = projectLine.substring(dateMatcher.start(), dateMatcher.end());
            String[] dates = datePortion.split("\\s*[-–—\\?]\\s*");
            if (dates.length >= 2) {
                startDate = dates[0].trim();
                endDate = dates[1].trim();
            } else {
                startDate = endDate = dates[0].trim(); // Set both to the same value if only one date is present
            }
        } else {
            throw new IllegalArgumentException("No date pattern found in the line: " + projectLine);
        }

        int detailsIdx = startIdx + 1; // Start from the line after the title and date
        while (detailsIdx < splitDocumentText.length
                && !splitDocumentText[detailsIdx].trim().isEmpty()) {

            String detailLine = splitDocumentText[detailsIdx];
            System.out.println("there is line:  " + detailLine);
            // Check if the detailLine contains a pipe symbol (|) to split tech stack and
            // summary
            if (detailLine.contains("|")) {
                String[] parts = detailLine.split("\\|");
                if (parts.length == 2) {
                    // Extract skills and summary
                    String skillsPart = parts[0].trim();
                    String[] skillArray = skillsPart.split(",");
                    skills.addAll(Arrays.asList(skillArray));
                }
            } else {
                // If no pipe symbol is found, treat the line as a description
                descriptions.add(detailLine.trim());
            }

            detailsIdx++;
        }

        // Debugging output
        System.out.println("Title: " + title);
        System.out.println("Skills: " + skills);
        System.out.println("StartDate: " + startDate);
        System.out.println("EndDate: " + endDate);
        System.out.println("Descriptions: " + descriptions);

        // Validation
        if (title == null || startDate == null || endDate == null) {
            throw new IllegalArgumentException("One of the parameters for Project constructor is null.");
        }

        return new Project(title, skills, startDate, endDate, descriptions);
    }

    public static void main(String[] args) {
        // Get the logger for "org.apache.pdfbox" and set its level to SEVERE to
        // suppress WARN messages
        Logger.getLogger("org.apache.pdfbox").setLevel(Level.SEVERE);
        PDFPlugin plugin = new PDFPlugin();

        // Replace this path with the path to your sample PDF
        String pdfPath = "D:\\Destop\\ResumeProject\\backend\\src\\test\\resources\\Anqi_Chen_Resume.pdf";

        try {
            plugin.parseDataResource(pdfPath);

            System.out.println("First Name: " + plugin.getFirstName());
            System.out.println("Last Name: " + plugin.getLastName());
            System.out.println("Email: " + plugin.getEmail());
            System.out.println("Phone Number: " + plugin.getPhoneNumber());
            System.out.println("URL: " + plugin.getURL());

            // Iterate through and print education objects
            System.out.println("\nEDUCATION: ");
            for (Education education : plugin.getEducation()) {
                System.out.println("School: " + education.getSchool());
                System.out.println("Major: " + education.getMajor());
                System.out.println("StartDate: " + education.getStartDate());
                System.out.println("EndDate: " + education.getEndDate());
                System.out.println("Description: " + education.getDescription());
                System.out.println("-----");
            }
            System.out.println("\nSkills:");
            for (String skill : plugin.getSkills()) {
                System.out.println(skill);
            }

            System.out.println("\nEXPERIENCE:");
            for (Experience exp : plugin.getExperience()) {
                System.out.println("Title: " + exp.getTitle());
                System.out.println("Position: " + exp.getPosition());
                System.out.println("Location: " + exp.getLocation());
                System.out.println("StartDate: " + exp.getStartDate());
                System.out.println("EndDate: " + exp.getEndDate());
                System.out.println("Description: " + exp.getDescription());
                System.out.println("-----");
            }

            System.out.println("\nPROJECTS:");
            for (Project project : plugin.getProject()) {
                // Assuming Project class has a suitable toString() method
                System.out.println(project);
                System.out.println("-----");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
