package GradebookTask;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Teacher extends Person implements Runnable {
    public enum Mode { ADD, DELETE, UPDATE }

    protected List<Group> groups;
    private Subject subject;
    private String position;
    private int numWeeks;
    private Mode mode;
    private Random random;

    public Teacher(String name, Subject subject, String position, int numWeeks, Mode mode) {
        super(name);
        this.groups = new ArrayList<>();
        this.subject = subject;
        this.position = position;
        this.numWeeks = numWeeks;
        this.mode = mode;
        this.random = new Random();
    }

    public void addGroup(Group group) {
        this.groups.add(group);
    }

    public String getPosition() {
        return position;
    }

    @Override
    public void run() {
        for (Group group : groups) {
            System.out.println(group.getName());
            for (Student student : group.getStudents()) {
                switch (mode) {
                    case ADD:
                        addGrades(student);
                        break;
                    case DELETE:
                        deleteGrades(student);
                        break;
                    case UPDATE:
                        updateGrades(student);
                        break;
                }
            }
        }
    }

    private void addGrades(Student student) {
        for (int i = 0; i < numWeeks; i++) {
            int grade = random.nextInt(101);
            student.addGrade(subject, grade);
            System.out.println(getName() + " додав оцінку " + grade + " для " + student.getName());
        }
    }

    private void deleteGrades(Student student) {
        student.clearGrades(subject);
        System.out.println(getName() + " видалив всі оцінки для " + student.getName());
    }

    private void updateGrades(Student student) {
        List<Integer> grades = student.getGrades(subject);
        if (!grades.isEmpty()) {
            for (int i = 0; i < grades.size(); i++) {
                student.updateGrade(subject, i, 100);
            }
            System.out.println(getName() + " змінив всі оцінки для " + student.getName());
        } else {
            System.out.println(getName() + " не зміг оновити оцінки, бо список оцінок порожній у " + student.getName());
        }
    }
}
