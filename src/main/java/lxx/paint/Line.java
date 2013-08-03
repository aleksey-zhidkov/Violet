package lxx.paint;

import lxx.utils.APoint;

public class Line implements Drawable {

    private final APoint from;
    private final double alpha;
    private final double length;

    public Line(APoint from, double alpha, double length) {
        this.from = from;
        this.alpha = alpha;
        this.length = length;
    }

    @Override
    public void draw(LxxGraphics g) {
        g.drawLine(from, alpha, length);
    }
}
