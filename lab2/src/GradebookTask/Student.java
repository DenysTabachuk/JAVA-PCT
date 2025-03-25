package GradebookTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Student extends Person {
    private final Map<Subject, List<Integer>> grades;

    public Student(String name) {
        super(name);
        this.grades = new ConcurrentHashMap<>();
    }

    public void addGrade(Subject subject, int grade) {
        grades.putIfAbsent(subject, new CopyOnWriteArrayList<>());
        grades.get(subject).add(grade);
    }

    public List<Integer> getGrades(Subject subject) {
        return new ArrayList<>(grades.getOrDefault(subject, new CopyOnWriteArrayList<>()));
    }

    public boolean removeGrade(Subject subject, int grade) {
        List<Integer> subjectGrades = grades.get(subject);
        if (subjectGrades != null && subjectGrades.contains(grade)) {
            subjectGrades.remove(Integer.valueOf(grade));
            return true;
        }
        return false;
    }

    public boolean updateGrade(Subject subject, int index, int newGrade) {
        List<Integer> subjectGrades = grades.get(subject);

        if (subjectGrades == null || subjectGrades.isEmpty()) {
            return false;
        }

        if (index >= 0 && index < subjectGrades.size()) {
            subjectGrades.set(index, newGrade);
            return true;
        }

        return false;
    }


    public void clearGrades(Subject subject) {
        grades.put(subject, new CopyOnWriteArrayList<>());
    }
}
