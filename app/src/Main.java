import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main {

    static class School {
        public String name;
        public List<Integer> location;
        public int maxAllocation;
        public int currentAllocation = 0;
        public List<Integer> allocatedStudents = new ArrayList<>();
    }

    // Structure for Student
    static class Student {
        public int id;
        public List<Integer> homeLocation;
        public String alumni;
        public String volunteer;
        public Map<String, Double> scores = new HashMap<>();
    }

    public static void main(String[] args) {

        String inputFilePath = "input.json";
        String outputFilePath = "output.json";

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Map<String, Object> data = objectMapper.readValue(new File(inputFilePath), new TypeReference<>() {});

            List<School> schools = objectMapper.convertValue(data.get("schools"), new TypeReference<>() {});
            List<Student> students = objectMapper.convertValue(data.get("students"), new TypeReference<>() {});

            for (Student student : students) {
                for (School school : schools) {
                    double distanceScore = calculateDistanceScore(student.homeLocation, school.location);
                    double alumniScore = student.alumni != null && student.alumni.equals(school.name) ? 30 : 0;
                    double volunteerScore = student.volunteer != null && student.volunteer.equals(school.name) ? 20 : 0;
                    double totalScore = 50 * distanceScore + alumniScore + volunteerScore;
                    student.scores.put(school.name, totalScore);
                }
            }

            for (School school : schools) {
                students.sort((s1, s2) -> {
                    double score1 = s1.scores.getOrDefault(school.name, 0.0);
                    double score2 = s2.scores.getOrDefault(school.name, 0.0);
                    if (score1 != score2) {
                        return Double.compare(score2, score1);
                    }
                    return Integer.compare(s1.id, s2.id);
                });

                for (Student student : students) {
                    if (school.currentAllocation < school.maxAllocation && !school.allocatedStudents.contains(student.id) && student.scores.containsKey(school.name)) {
                        school.allocatedStudents.add(student.id);
                        school.currentAllocation++;
                    }
                }
            }

            List<Map<String, List<Integer>>> output = new ArrayList<>();
            for (School school : schools) {
                Map<String, List<Integer>> schoolAllocation = new HashMap<>();
                schoolAllocation.put(school.name, school.allocatedStudents);
                output.add(schoolAllocation);
            }


            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputFilePath), output);

            System.out.println("Allocation completed. Output saved to " + outputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("An error occurred while processing the JSON file.");
        }
    }

    private static double calculateDistanceScore(List<Integer> home, List<Integer> school) {
        double distance = Math.sqrt(Math.pow(home.get(0) - school.get(0), 2) + Math.pow(home.get(1) - school.get(1), 2));
        return 1 / (1 + distance);
    }

}
