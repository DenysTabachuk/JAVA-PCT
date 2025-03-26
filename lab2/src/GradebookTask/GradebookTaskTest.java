package GradebookTask;

import java.util.List;
import java.util.Scanner;

public class GradebookTaskTest {
    public static void main(String[] args) {
        Gradebook gradebook = new Gradebook(3);
        Subject subjectMath = new Subject("Math");

        Teacher lectorAdd = new Teacher("Lector Denis Denisovich", subjectMath, "lector", 12, Teacher.Mode.ADD);
        Teacher lectorUpdate = new Teacher("Cool lector", subjectMath, "lector", 12, Teacher.Mode.UPDATE);
        Teacher assistant1Add = new Teacher("Assistant1", subjectMath, "assistant", 12, Teacher.Mode.ADD);
        Teacher assistant2Add = new Teacher("Assistant2", subjectMath, "assistant", 12, Teacher.Mode.ADD);
        Teacher assistant3Add = new Teacher("Assistant3", subjectMath, "assistant", 12, Teacher.Mode.ADD);
        Teacher assistant4Delete = new Teacher("Crazy assistant", subjectMath, "assistant", 12, Teacher.Mode.DELETE);

        List<Group> groups = gradebook.getGroups();
        for (int g = 0; g < groups.size(); g++) {
            Group group = groups.get(g);
            for (int i = 0; i < 30; i++) {
                group.addStudent(new Student("Student" + (i + 1)));
            }
            group.setName("IP2" + (g + 1));
            lectorAdd.addGroup(group);
        }

        assistant1Add.addGroup(groups.get(0));
        assistant2Add.addGroup(groups.get(1));
        assistant3Add.addGroup(groups.get(2));
        assistant4Delete.addGroup(groups.get(0));
        lectorUpdate.addGroup(groups.get(0));

        Thread lectorAddThread = new Thread(lectorAdd);
        Thread assistant1AddThread = new Thread(assistant1Add);
        Thread assistant2AddThread = new Thread(assistant2Add);
        Thread assistant3AddThread = new Thread(assistant3Add);

        lectorAddThread.start();
        assistant1AddThread.start();
        assistant2AddThread.start();
        assistant3AddThread.start();

        try {
            lectorAddThread.join();
            assistant2AddThread.join();
            assistant1AddThread.join();
            assistant3AddThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (Group group : groups) {
            for (Student student : group.getStudents()) {
                List<Integer> grades = student.getGrades(subjectMath);
                System.out.println(group.getName() + " " + student.getName() +
                        " Оцінки: " + grades +
                        " Кількість оцінок: " + grades.size());
            }
        }


        System.out.println("\nНатисніть Enter, щоб продовжити до оновлення та видалення оцінок...");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();


        Thread lectorUpdateThread = new Thread(lectorUpdate);
        Thread assistantDeleteThread = new Thread(assistant4Delete);
        lectorUpdateThread.start();
        assistantDeleteThread.start();

        try {
            lectorUpdateThread.join();
            assistantDeleteThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (Student student : groups.get(0).getStudents()) {
            List<Integer> grades = student.getGrades(subjectMath);
            System.out.println(groups.get(0).getName() + " " + student.getName() +
                    " Оцінки: " + grades +
                    " Кількість оцінок: " + grades.size());
        }
    }
}
