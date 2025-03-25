package BankTask;

import java.util.Scanner;

public class BankTest {
    public static final int NACCOUNTS = 10;
    public static final int INITIAL_BALANCE = 100000;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nВиберіть метод переказу:");
            System.out.println("1 - Без синхронізації");
            System.out.println("2 - Синхронізований метод");
            System.out.println("3 - Блокування за допомогою Lock");
            System.out.println("4 - Синхронізований блок");
            System.out.println("5 - Використання атомарних змінних");
            System.out.println("0 - Вихід");

            int method = scanner.nextInt();

            if (method == 0) {
                System.out.println("Вихід з програми...");
                break;
            }

            if (method < 1 || method > 5) {
                System.out.println("Невірний вибір, спробуйте ще раз.");
                continue;
            }

            Bank b = new Bank(NACCOUNTS, INITIAL_BALANCE);
            for (int i = 0; i < NACCOUNTS; i++) {
                TransferThread t = new TransferThread(b, i, INITIAL_BALANCE, method);
                t.start();
            }
        }

        scanner.close();
    }
}
