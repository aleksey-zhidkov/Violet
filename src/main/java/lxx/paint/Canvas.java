package lxx.paint;

import java.awt.*;
import java.util.LinkedList;

public enum Canvas {

    BATTLE_STATE(true),
    WS(true);

    private LinkedList<DrawCommand> drawables = new LinkedList<DrawCommand>();

    private final boolean autoReset;

    private boolean enabled = false;

    Canvas(boolean autoReset) {
        this.autoReset = autoReset;
    }

    public void switchEnabled() {
        enabled = !enabled;
    }

    public boolean enabled() {
        return enabled;
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

    private class DrawCommand {

        private final Drawable d;
        private final Color c;

        private DrawCommand(Drawable d, Color c) {
            this.d = d;
            this.c = c;
        }
    }

}
