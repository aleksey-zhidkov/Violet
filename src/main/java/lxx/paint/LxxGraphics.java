package lxx.paint;

import lxx.utils.APoint;

import java.awt.*;

import static java.lang.Math.round;

public class LxxGraphics {

    private final Graphics2D g;

    private Color color;

    public LxxGraphics(Graphics2D g) {
        this.g = g;
    }

    public void drawLine(APoint from, APoint to) {
        g.drawLine((int) round(from.x()), (int) round(from.y()), (int) round(to.x()), (int) round(to.y()));
    }

    public void drawLine(APoint center, double angle, double length) {
        drawLine(center.project(angle, length / 2), center.project(robocode.util.Utils.normalAbsoluteAngle(angle - Math.PI), length / 2));
    }

    public void setColor(Color c) {
        if (c != null && !c.equals(color)) {
            g.setColor(c);
            color = c;
        }
    }

    public void drawCircle(APoint center, double radius) {
        g.drawOval((int) round(center.x() - radius), (int) round(center.y() - radius), (int) round(radius * 2), (int) round(radius * 2));
    }

    public void fillCircle(APoint center, double radius) {
        g.fillOval((int) round(center.x() - radius), (int) round(center.y() - radius), (int) round(radius * 2), (int) round(radius * 2));
    }

    public void drawSquare(APoint center, double width) {
        g.drawRect((int) (center.x() - width / 2), (int) (center.y() - width / 2), (int) width, (int) width);
    }

    public void drawText(String text, double x, double y) {
        g.drawString(text, (float) x, (float) y);
    }

    public Graphics2D getGraphics() {
        return g;
    }
}
