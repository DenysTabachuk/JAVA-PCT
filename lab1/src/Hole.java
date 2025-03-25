import java.awt.*;
import java.awt.geom.Ellipse2D;


public class Hole {
    private int x ;
    private int y;
    public static final int DIAMETER = 100;


    public Hole(int x, int y){
        this.x = x;
        this.y = y;
    }

    public void draw(Graphics2D g2){
        g2.setColor(Color.GREEN);
        g2.fill(new Ellipse2D.Double(x, y, DIAMETER, DIAMETER));
    }

    public boolean contains(Ball b) {
        int centerX = x + DIAMETER / 2;
        int centerY = y + DIAMETER / 2;
        int ballCenterX = b.getX() + b.getDiameter() / 2;
        int ballCenterY = b.getY() + b.getDiameter() / 2;

        double distance = Math.sqrt(Math.pow(centerX - ballCenterX, 2) + Math.pow(centerY - ballCenterY, 2));
        return distance <= DIAMETER / 2;
    }
}
