package lxx.paint;

import java.util.List;

public class Text implements Drawable {

    private final List<String> text;
    private final double x;
    private final double y;

    public Text(List<String> text, double x, double y) {
        this.text = text;
        this.x = x;
        this.y = y;
    }

    @Override
    public void draw(LxxGraphics g) {
        final int lineHeight = g.getGraphics().getFontMetrics().getHeight() + 2;
        double y1 = y;
        for (String t : text) {
            g.drawText(t, x, y1);
            y1 -= lineHeight;
        }
    }

}
