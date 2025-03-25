import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class BallAndHolesCanvas extends JPanel {
    private ArrayList<Ball> balls = new ArrayList<>();
    private ArrayList<Hole> holes = new ArrayList<>();

    public BallAndHolesCanvas(ArrayList<Hole> holeList){
        this.holes = holeList;
    }

    public void addBall(Ball b){
        this.balls.add(b);
    }

    public void removeBall(Ball b) {
        balls.remove(b);
    }

    public ArrayList<Ball> getBalls() {
        return balls;
    }

    public void clearBalls() {
        balls.clear();
        repaint();
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g); // Очистка панелі перед малюванням
        Graphics2D g2 = (Graphics2D)g;
        for (Ball b : balls) {
            b.draw(g2);
        }

        for (Hole h : holes) {
            h.draw(g2);
        }
    }
}