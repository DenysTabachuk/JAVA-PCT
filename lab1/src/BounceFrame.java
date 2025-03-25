import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;

public class BounceFrame extends JFrame {
    private BallAndHolesCanvas canvas;
    private final int WIDTH = 1240;
    private final int HEIGHT = 720;
    private final ArrayList<Hole> HOLES = new ArrayList<>() {{
        add(new Hole(0 - Hole.DIAMETER / 2, 0 - Hole.DIAMETER / 2));
        add(new Hole((int) (WIDTH - Hole.DIAMETER / 2 - 10), 0 - Hole.DIAMETER / 2));
        add(new Hole((int) (WIDTH / 2 - Hole.DIAMETER / 2 - 10), 0 - Hole.DIAMETER / 2));
        add(new Hole((int) (WIDTH / 2 - Hole.DIAMETER / 2 - 10), HEIGHT - (int) (Hole.DIAMETER / 0.75)));
        add(new Hole(0 - Hole.DIAMETER / 2 + 10, HEIGHT - (int) (Hole.DIAMETER / 0.75)));
        add(new Hole((int) (WIDTH - Hole.DIAMETER / 2 - 10), HEIGHT - (int) (Hole.DIAMETER / 0.75)));
    }};
    private final ArrayList<BallAndHolesThread> threads = new ArrayList<>();
    private int ballsInHoles = 0;
    JLabel ballCountLabel;
    private boolean isPaused = true;
    private JCheckBox randomSpawnCheckbox;
    private final Random random = new Random();

    public synchronized void pauseThreads() {
        isPaused = true;
    }

    public synchronized void resumeThreads() {
        isPaused = false;
        notifyAll();
    }

    public synchronized boolean isPaused() {
        return isPaused;
    }

    public BounceFrame() {
        this.setSize(WIDTH, HEIGHT);
        this.setTitle("Bounce program");
        this.canvas = new BallAndHolesCanvas(HOLES);

        ballCountLabel = new JLabel("Number of Balls: 0  Balls in Holes: " + ballsInHoles);
        ballCountLabel.setForeground(Color.WHITE);
        ballCountLabel.setBackground(Color.BLACK);
        ballCountLabel.setOpaque(true);

        System.out.println("In Frame Thread name = " + Thread.currentThread().getName());
        Container content = this.getContentPane();
        content.add(this.canvas, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.lightGray);
        JButton buttonRed = new JButton("Red Ball");
        JButton buttonBlue = new JButton("Blue Ball");
        JButton buttonBlack = new JButton("Black Ball");
        JButton buttonStop = new JButton("Start");
        JButton buttonDouble = new JButton("Double Balls");
        JButton buttonJoinExample = new JButton("Join Example");
        JButton buttonClear = new JButton("Clear");

        randomSpawnCheckbox = new JCheckBox("Random Spawn");
        randomSpawnCheckbox.setBackground(Color.lightGray);

        buttonRed.addActionListener(e -> spawnBall(Color.RED, Thread.MAX_PRIORITY));
        buttonBlue.addActionListener(e -> spawnBall(Color.BLUE, Thread.NORM_PRIORITY));
        buttonBlack.addActionListener(e -> spawnBall(Color.BLACK, Thread.MIN_PRIORITY));

        buttonStop.addActionListener(e -> {
            synchronized (BounceFrame.this) {
                if (isPaused) {
                    resumeThreads();
                    buttonStop.setText("Pause");
                } else {
                    pauseThreads();
                    buttonStop.setText("Resume");
                }
            }
        });

        buttonDouble.addActionListener(e -> {
            java.util.List<Ball> existingBalls = new ArrayList<>(canvas.getBalls());
            for (Ball b : existingBalls) {
                spawnBall(b.getColor(), getPriorityByColor(b.getColor()));
            }
        });

        buttonClear.addActionListener(e -> clearBalls());

        buttonJoinExample.addActionListener(e -> {
            Ball b = new Ball(canvas, Color.RED, 150, 200);
            Ball b2 = new Ball(canvas, Color.BLACK, 200, 200);
            Ball b3 = new Ball(canvas, Color.BLUE, 300, 300);
            canvas.addBall(b);
            canvas.addBall(b2);
            canvas.addBall(b3);

            canvas.repaint();
            updateBallCount();

            BallAndHolesThread thread1 = new BallAndHolesThread(b, HOLES, canvas, Thread.NORM_PRIORITY, BounceFrame.this);
            BallAndHolesThread thread2 = new BallAndHolesThread(b2, HOLES, canvas, Thread.NORM_PRIORITY, BounceFrame.this);
            BallAndHolesThread thread3 = new BallAndHolesThread(b3, HOLES, canvas, Thread.NORM_PRIORITY, BounceFrame.this);

            // Створюємо новий потік для запуску і синхронізації потоків
            new Thread(() -> {
                try {
                    // Запускаємо перший потік
                    thread1.start();
                    // Чекаємо на завершення першого потоку
                    thread1.join();

                    // Запускаємо другий потік
                    thread2.start();
                    // Чекаємо на завершення другого потоку
                    thread2.join();

                    // Запускаємо третій потік
                    thread3.start();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
        });


        buttonPanel.add(randomSpawnCheckbox);
        buttonPanel.add(buttonRed);
        buttonPanel.add(buttonBlue);
        buttonPanel.add(buttonBlack);
        buttonPanel.add(buttonDouble);
        buttonPanel.add(buttonStop);
        buttonPanel.add(buttonClear);
        buttonPanel.add(buttonJoinExample);

        content.add(ballCountLabel, BorderLayout.NORTH);
        content.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void spawnBall(Color color, int priority) {
        int x, y;
        if (randomSpawnCheckbox.isSelected()) {
            x = random.nextInt(WIDTH - 40) + 20;
            y = random.nextInt(HEIGHT - 40) + 20;
        } else {
            x = WIDTH / 2;
            y = HEIGHT / 2;
        }

        Ball b = new Ball(canvas, color, x, y);
        canvas.addBall(b);
        canvas.repaint();
        updateBallCount();

        BallAndHolesThread thread = new BallAndHolesThread(b, HOLES, canvas, priority, BounceFrame.this);
        threads.add(thread);
        thread.start();
    }

    private int getPriorityByColor(Color color) {
        if (Color.RED.equals(color)) {
            return Thread.MAX_PRIORITY;
        } else if (Color.BLUE.equals(color)) {
            return Thread.NORM_PRIORITY;
        } else {
            return Thread.MIN_PRIORITY;
        }
    }

    public void incrementBallsInHoles() {
        ballsInHoles++;
        updateBallCount();
    }

    public void updateBallCount() {
        ballCountLabel.setText("Number of Balls: " + canvas.getBalls().size() + "  Balls in Holes: " + ballsInHoles);
    }

    private void clearBalls() {
        for (BallAndHolesThread thread : threads ) {
            thread.stopThread();
        }

        canvas.clearBalls();
        ballsInHoles = 0;
        updateBallCount();
    }
}
