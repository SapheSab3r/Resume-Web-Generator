package anqi.chen.plugins;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
// Importing List class from java.util package
import java.util.List;

import anqi.chen.framework.DataPlugin;
import anqi.chen.framework.Education;
import anqi.chen.framework.Experience;
import anqi.chen.framework.ResumeFramework;
import anqi.chen.framework.Project;

public class TextPlugin implements DataPlugin {

    private List<String> splitDocumentText;

    @Override
    public void onRegister(ResumeFramework framework) {

    }

    @Override
    public void parseDataResource(String path) throws IOException {

        Path temp_path = Paths.get(path);

        // To read file to byte array
        byte[] bytes = Files.readAllBytes(temp_path);

        // Display message only
        System.out.println(
                "Read text file using Files class");

        // Reading the file to String List

        // Creating a List class object of string type
        // as data in file to be read is words
        this.splitDocumentText = Files.readAllLines(
                temp_path, StandardCharsets.UTF_8);
        System.out.println(this.splitDocumentText);
    }

    @Override
    public String getPluginName() {
        return "TXT";
    }

    @Override
    public String getFirstName() {
        String info = splitDocumentText.get(0);
        String[] splitName = info.split(" ");
        ;
        return splitName[0];
    }

    @Override
    public String getLastName() {
        String info = splitDocumentText.get(0);
        String[] splitName = info.split(" ");
        ;
        return splitName[1];
    }

    @Override
    public String getEmail() {
        String info = splitDocumentText.get(1);
        String[] splitInfo = info.split(" +\\| +");
        return splitInfo[1];
    }

    @Override
    public String getPhoneNumber() {
        String info = splitDocumentText.get(1);
        String[] splitInfo = info.split(" +\\| +");
        return splitInfo[0];
    }

    @Override
    public String getURL() {
        String info = splitDocumentText.get(1);
        String[] splitInfo = info.split(" +\\| +");
        return splitInfo[2];
    }

    @Override
    public List<String> getSkills() {
        int startIdx = -1;
        List<String> skills = new ArrayList<>();
        for (int i = 0; i < splitDocumentText.size(); i++) {
            if (splitDocumentText.get(i).trim().equals("SKILLS")) {
                startIdx = i;
                break;
            }
        }
        if (startIdx == -1) {
            return null;
        }
        startIdx += 1;
        while (startIdx < splitDocumentText.size() && splitDocumentText.get(startIdx).trim() != "") {
            String skillLine = splitDocumentText.get(startIdx);
            String[] splitSkillLine = skillLine.split("[:,]");
            for (int j = 1; j < splitSkillLine.length; j++) {
                skills.add(splitSkillLine[j].trim());
            }
            startIdx++;
        }
        return skills;
    }

    private Experience createExperience(int startIdx) {
        String[] splitTitle = splitDocumentText.get(startIdx).split(" ");
        System.out.println();
        String[] splitDate = splitDocumentText.get(startIdx + 2).split(" - ");
        String title = String.join(" ", Arrays.copyOfRange(splitTitle, 0, splitTitle.length - 2)).trim();
        String location = String.join(" ", Arrays.copyOfRange(splitTitle, splitTitle.length - 2, splitTitle.length))
                .trim();
        String position = splitDocumentText.get(startIdx + 1).trim();
        String startDate = splitDate[0].trim();
        String endDate = splitDate[splitDate.length - 1].trim();

        List<String> descriptions = new ArrayList<>();
        startIdx = startIdx + 3;
        // System.out.println(title);
        // System.out.println(position);
        // System.out.println(location);
        // System.out.println(startDate);
        // System.out.println(endDate);
        while (startIdx < splitDocumentText.size() && splitDocumentText.get(startIdx).trim() != "") {
            if (splitDocumentText.get(startIdx).trim().equals("")) {
                break;
            }
            String cleanText = splitDocumentText.get(startIdx).replace("\u2022 ", "");
            descriptions.add(cleanText.trim());
            startIdx++;
        }
        // System.out.println(descriptions);
        return new Experience(title, position, location, startDate, endDate, descriptions);
    }

    @Override
    public List<Experience> getExperience() {
        // System.out.println("--------");
        List<Integer> startIdxs = new ArrayList<>();
        List<Experience> experiences = new ArrayList<>();

        for (int i = 0; i < splitDocumentText.size(); i++) {
            if (splitDocumentText.get(i).trim().equals("EDUCATION") ||
                    splitDocumentText.get(i).trim().equals("EXPERIENCE") ||
                    splitDocumentText.get(i).trim().equals("")) {
                if (splitDocumentText.get(i + 1).split(" ").length > 1) {
                    startIdxs.add(i + 1);
                }
            }
        }
        // System.out.println("********");
        for (int idx : startIdxs) {
            experiences.add(createExperience(idx));
        }
        // System.out.println("&&&&&&&&");
        return experiences;
    }

    @Override
    public List<Project> getProject() {
        List<Integer> startIdxs = new ArrayList<>();
        List<Project> projects = new ArrayList<>();

        for (int i = 0; i < splitDocumentText.size(); i++) {
            if (splitDocumentText.get(i).trim().startsWith("PROJECT")) {
                startIdxs.add(i);
            }
        }

        for (int idx : startIdxs) {
            projects.add(createProject(idx));
        }

        return projects;
    }

    private Project createProject(int startIdx) {
        String projectInfo = splitDocumentText.get(startIdx + 1); // Line with project title and skills
        String[] titleAndSkills = projectInfo.split("\\|");
        String title = titleAndSkills[0].trim();
        String[] skillsArray = titleAndSkills[1].split(",");
        List<String> skills = Arrays.asList(skillsArray);

        String position = splitDocumentText.get(startIdx + 2).trim(); // Line with position

        String dateRange = splitDocumentText.get(startIdx + 3).trim(); // Line with date range
        String[] dateRangeParts = dateRange.split(" - ");
        String startDate = dateRangeParts[0].trim();
        String endDate = dateRangeParts[1].trim();

        List<String> descriptions = new ArrayList<>();
        int currentIndex = startIdx + 4;

        while (currentIndex < splitDocumentText.size() && !splitDocumentText.get(currentIndex).trim().isEmpty()) {
            String description = splitDocumentText.get(currentIndex).trim();
            descriptions.add(description);
            currentIndex++;
        }

        return new Project(title, skills, startDate, endDate, descriptions);
    }

    public static void main(String args[]) throws IOException {
        // Assuming the file is located at
        // D:\Destop\ResumeProject\backend\src\test\resources\test.txt
        Path temp_path = Paths.get("D:/Destop/ResumeProject/backend/src/test/resources/test.txt");

        // To read file to byte array
        byte[] bytes = Files.readAllBytes(temp_path);

        // Display message only
        System.out.println(
                "Read text file using Files class");

        // Reading the file to String List
        @SuppressWarnings("unused")

        // Creating a List class object of string type
        // as data in file to be read is words
        List<String> allLines = Files.readAllLines(
                temp_path, StandardCharsets.UTF_8);
        List<Integer> startIdxs = new ArrayList<>();
        List<Experience> experiences = new ArrayList<>();

        for (int i = 0; i < allLines.size(); i++) {
            if (allLines.get(i).trim().equals("EDUCATION") ||
                    allLines.get(i).trim().equals("EXPERIENCE") ||
                    allLines.get(i).trim().equals("")) {
                if (allLines.get(i + 1).split(" ").length > 1) {
                    startIdxs.add(i + 1);
                }
            }
        }
        System.out.println(startIdxs);
        for (int idx : startIdxs) {
            String[] splitTitle = allLines.get(idx).split(" ");
            System.out.println();
            String[] splitDate = allLines.get(idx + 2).split(" - ");
            String title = String.join(" ", Arrays.copyOfRange(splitTitle, 0, splitTitle.length - 2)).trim();
            String location = String.join(" ", Arrays.copyOfRange(splitTitle, splitTitle.length - 2, splitTitle.length))
                    .trim();
            String position = allLines.get(idx + 1).trim();
            String startDate = splitDate[0].trim();
            String endDate = splitDate[splitDate.length - 1].trim();

            List<String> descriptions = new ArrayList<>();
            idx = idx + 3;
            while (idx < allLines.size() && allLines.get(idx).trim() != "") {
                // System.out.println(allLines.get(idx).trim());
                if (allLines.get(idx).trim().equals("")) {
                    break;
                }
                String cleanText = allLines.get(idx).replace("\u2022 ", "");
                descriptions.add(cleanText.trim());
                idx++;
            }
            System.out.println("---------------");
            System.out.println(title);
            System.out.println(position);
            System.out.println(location);
            System.out.println(startDate);
            System.out.println(endDate);
            System.out.println(descriptions);
        }

    }

    @Override
    public List<Education> getEducation() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getEducation'");
    }

}