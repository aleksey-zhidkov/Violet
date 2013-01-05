package lxx.paint;

import lxx.utils.APoint;

public class Square implements Drawable {

    private final APoint center;
    private final int width;

    public Square(APoint center, double width) {
        this.center = center;
        this.width = (int) width;
    }

    @Override
    public void draw(LxxGraphics g) {
        g.drawSquare(center, width);
    }
}
