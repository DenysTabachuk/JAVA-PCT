import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.Random;

class Ball {
    private Component canvas;
    private static final int DIAMETER = 20;
    private int x;
    private int y;
    private int dx = 2;
    private int dy = 2;
    private boolean inHole = false;
    private Color color;

    public Ball(Component c, Color color, int x, int y) {
        this.canvas = c;
        this.color = color;
        this.x = x;
        this.y = y;
    }

    public void draw(Graphics2D g2) {
        g2.setColor(color);
        g2.fill(new Ellipse2D.Double(x, y, DIAMETER, DIAMETER));
    }

    public void move() {
        x += dx;
        y += dy;
        if (x < 0) {
            x = 0;
            dx = -dx;
        }
        if (x + DIAMETER >= this.canvas.getWidth()) {
            x = this.canvas.getWidth() - DIAMETER;
            dx = -dx;
        }
        if (y < 0) {
            y = 0;
            dy = -dy;
        }
        if (y + DIAMETER >= this.canvas.getHeight()) {
            y = this.canvas.getHeight() - DIAMETER;
            dy = -dy;
        }
        this.canvas.repaint();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getDiameter() {
        return DIAMETER;
    }

    public boolean isInHole() {
        return inHole;
    }

    public Color getColor() {
        return color;
    }

    public void setInHole(boolean inHole) {
        this.inHole = inHole;
    }
}
