
package anqi.chen.plugins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import anqi.chen.framework.DataPlugin;
import anqi.chen.framework.Experience;
import anqi.chen.framework.ResumeFramework;
import anqi.chen.framework.Project;

import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class PDFPlugin implements DataPlugin {
    private String[] splitDocumentText;
    private final int num3 = 3;

    @Override
    public void parseDataResource(String dataPath) throws IOException {
        // Load the data resource
        try (PDDocument document = PDDocument.load(new File(dataPath));) {
            if (!document.isEncrypted()) {
                PDFTextStripper stripper = new PDFTextStripper();
                String documentText = stripper.getText(document);
                splitDocumentText = documentText.split("\\R");

                document.close();
            }

        } catch (IOException e) {
            throw new IOException("Error loading and processing PDF document: " + e.getMessage(), e);
        }
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
        String[] splitInfo = info.split(" +\\| +");
        return splitInfo[1];
    }

    @Override
    public String getPhoneNumber() {
        String info = splitDocumentText[1];
        String[] splitInfo = info.split(" +\\| +");
        return splitInfo[0];
    }

    @Override
    public String getURL() {
        String info = splitDocumentText[1];
        String[] splitInfo = info.split(" +\\| +");
        return splitInfo[2];
    }

    @Override
    public List<String> getSkills() {
        int startIdx = -1;
        List<String> skills = new ArrayList<>();
        for (int i = 0; i < splitDocumentText.length; i++) {
            if (splitDocumentText[i].trim().equals("SKILLS") ||
                    splitDocumentText[i].trim().equals("TECHNICAL SKILLS")) {
                startIdx = i;
                break;
            }
        }
        if (startIdx == -1) {
            return null;
        }
        startIdx += 1;

        while (splitDocumentText[startIdx].trim() != "") {
            String skillLine = splitDocumentText[startIdx];
            String[] splitSkillLine = skillLine.split("[:,]");
            for (int j = 1; j < splitSkillLine.length; j++) {
                skills.add(splitSkillLine[j].trim());
            }
            startIdx++;
        }

        return skills;
    }

    @Override
    public List<Experience> getExperience() {
        List<Integer> startIdxs = new ArrayList<>();
        List<Experience> experiences = new ArrayList<>();

        for (int i = 0; i < splitDocumentText.length; i++) {
            if (splitDocumentText[i].trim().equals("EDUCATION") ||
                    splitDocumentText[i].trim().equals("EXPERIENCE") ||
                    splitDocumentText[i].trim().equals("")) {
                if (splitDocumentText[i + 1].split(" ").length > 1) {
                    startIdxs.add(i + 1);
                }
            }
        }

        for (int idx : startIdxs) {
            experiences.add(createExperience(idx));
        }

        return experiences;
    }

    @Override
    public List<Project> getProject() {
        List<Integer> startIdxs = new ArrayList<>();
        List<Project> projects = new ArrayList<>();

        for (int i = 0; i < splitDocumentText.length; i++) {
            if (splitDocumentText[i].trim().equals("PROJECT") ||
                    splitDocumentText[i].trim().equals("NOTABLE PROJECT")) {
                if (splitDocumentText[i + 1].split(" ").length > 1) {
                    startIdxs.add(i + 1);
                }
            }
        }

        for (int idx : startIdxs) {
            projects.add(createProject(idx));
        }
        return projects;
    }

    private Experience createExperience(int startIdx) {
        String[] splitTitle = splitDocumentText[startIdx].split(" ");
        String[] splitDate = splitDocumentText[startIdx + 2].split(" - ");
        String title = String.join(" ", Arrays.copyOfRange(splitTitle, 0, splitTitle.length - 2)).trim();
        String location = String.join(" ", Arrays.copyOfRange(splitTitle, splitTitle.length - 2, splitTitle.length))
                .trim();
        String position = splitDocumentText[startIdx + 1].trim();
        String startDate = splitDate[0].trim();
        String endDate = splitDate[splitDate.length - 1].trim();

        List<String> descriptions = new ArrayList<>();
        startIdx = startIdx + num3;
        while (startIdx < splitDocumentText.length && splitDocumentText[startIdx].trim() != "") {
            String cleanText = splitDocumentText[startIdx].trim();
            descriptions.add(cleanText);
            startIdx++;
        }

        return new Experience(title, position, location, startDate, endDate, descriptions);
    }

    private Project createProject(int startIdx) {
        String[] splitTitle = splitDocumentText[startIdx].split("\\|");
        String title = splitTitle[0].trim();
        String[] skills = splitTitle[1].split(",");
        for (int i = 0; i < skills.length; i++) {
            skills[i] = skills[i].trim(); // Trimming each skill
        }

        String[] splitDate = splitTitle[2].split(" - ");
        String startDate = splitDate[0].trim();
        String endDate = splitDate[1].trim();

        // Extracting Summary
        String summary = splitDocumentText[startIdx + 1].trim();

        List<String> descriptions = new ArrayList<>();
        startIdx += 2;
        while (startIdx < splitDocumentText.length && !splitDocumentText[startIdx].trim().isEmpty()) {
            descriptions.add(splitDocumentText[startIdx].trim());
            startIdx++;
        }
        return new Project(title, Arrays.asList(skills), summary, startDate, endDate, descriptions);
    }
}
