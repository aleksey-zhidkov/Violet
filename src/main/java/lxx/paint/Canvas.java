package lxx.paint;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public enum Canvas {

    BATTLE_STATE(true),
    WS(true);

    private static boolean paintEnabled;

    private final List<DrawCommand> drawables = new LinkedList<DrawCommand>();

    private final boolean autoReset;

    private boolean enabled;

    Canvas(boolean autoReset) {
        this.autoReset = autoReset;
    }

    public void switchEnabled() {
        enabled = !enabled;
    }

    public static void setPaintEnabled(boolean paintEnabled) {
        Canvas.paintEnabled = paintEnabled;
    }

    public boolean enabled() {
        return enabled & paintEnabled;
    }

    public void reset() {
        drawables.clear();
    }

    public void draw(Drawable d, Color c) {
        drawables.add(new DrawCommand(d, c));
    }

    public void exec(LxxGraphics g) {
        if (drawables.size() == 0) {
            return;
        }

        try {
            for (DrawCommand dc : drawables) {
                g.setColor(dc.c);
                dc.d.draw(g);
            }
        } finally {
            if (autoReset) {
                reset();
            }
        }
    }

    private static final class DrawCommand {

        public final Drawable d;
        public final Color c;

        private DrawCommand(Drawable d, Color c) {
            this.d = d;
            this.c = c;
        }
    }

}
