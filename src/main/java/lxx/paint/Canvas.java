package lxx.paint;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public enum Canvas {

    BATTLE_STATE(true, 'b'),
    WS_MOVEMENT(false, 'w'),
    WS_WAVES(true, 'w');

    private static boolean paintEnabled;
    private final List<DrawCommand> drawables = new LinkedList<DrawCommand>();
    private final boolean autoReset;
    private final char enableSwitchKey;
    private boolean enabled;

    Canvas(boolean autoReset, char enableSwitchKey) {
        this.autoReset = autoReset;
        this.enableSwitchKey = enableSwitchKey;
    }

    public static void setPaintEnabled(boolean paintEnabled) {
        Canvas.paintEnabled = paintEnabled;
    }

    public void switchEnabled() {
        enabled = !enabled;
    }

    public boolean enabled() {
        return enabled & paintEnabled;
    }

    public char getEnableSwitchKey() {
        return enableSwitchKey;
    }

    public void reset() {
        drawables.clear();
    }

    public void draw(Drawable d, Color c) {
        if (enabled) {
            drawables.add(new DrawCommand(d, c));
        }
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
