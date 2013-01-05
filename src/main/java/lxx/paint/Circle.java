package lxx.paint;

import lxx.utils.APoint;

public class Circle implements Drawable {

    private final APoint center;
    private final double radius;
    private final boolean fill;

    public Circle(APoint center, double radius, boolean fill) {
        this.center = center;
        this.radius = radius;
        this.fill = fill;
    }

    public Circle(APoint center, double radius) {
        this(center, radius, false);
    }

    @Override
    public void draw(LxxGraphics g) {
        if (fill) {
            g.fillCircle(center, radius);
        } else {
            g.drawCircle(center, radius);
        }
    }
}
