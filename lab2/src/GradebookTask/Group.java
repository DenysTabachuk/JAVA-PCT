package GradebookTask;

import java.util.ArrayList;
import java.util.List;

class Group {
    private String name;
    private List<Student> students;

    public Group(String name) {
        this.students = new ArrayList<>();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public synchronized void addStudent(Student student) {
        students.add(student);
    }

    public synchronized Student getStudent(String name) {
        for (Student student : students) {
            if (student.getName().equals(name)) {
                return student;
            }
        }
        return null;
    }

    public synchronized List<Student> getStudents() {
        return new ArrayList<>(students);
    }
}
