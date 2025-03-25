import javax.swing.*;
import java.util.ArrayList;

public class BallAndHolesThread extends Thread {
    private Ball b;
    private ArrayList<Hole> holes;
    private BallAndHolesCanvas canvas;
    private BounceFrame frame;
    private boolean running = true;

    public BallAndHolesThread(Ball ball, ArrayList<Hole> holes, BallAndHolesCanvas canvas, int priority, BounceFrame frame) {
        this.b = ball;
        this.holes = holes;
        this.canvas = canvas;
        this.setPriority(priority);
        this.frame = frame;
    }

    @Override
    public void run() {
        try {
            while (!b.isInHole() && running) {
                System.out.println("Thread name = " + Thread.currentThread().getName() + ", Priority = " + Thread.currentThread().getPriority());
                synchronized (frame) {
                    while (frame.isPaused()) {
                        frame.wait();
                    }
                }

                b.move();
                for (Hole hole : holes) {
                    if (hole.contains(b)) {
                        b.setInHole(true);
                        SwingUtilities.invokeLater(() -> frame.incrementBallsInHoles());
                        break;
                    }
                }
                canvas.repaint();
                Thread.sleep(5);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        // Видаляємо м'яч з канваса та оновлюємо кількість
        SwingUtilities.invokeLater(() -> {
            canvas.removeBall(b);
            frame.updateBallCount();
        });
    }

    public void stopThread() {
        running = false;
    }
}
