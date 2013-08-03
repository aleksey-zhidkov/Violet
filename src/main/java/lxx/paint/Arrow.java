package lxx.paint;

import lxx.utils.APoint;

public class Arrow implements Drawable {

    private final APoint from;
    private final APoint to;
    private final double pikeWidth;

    public Arrow(APoint from, double alpha, double length, double pikeWidth) {
        this.from = from;
        this.to = from.project(alpha, length);
        this.pikeWidth = pikeWidth;
    }

    @Override
    public void draw(LxxGraphics g) {
        final double angle = from.angleTo(to);
        final double arrowLength = from.distance(to);
        final APoint peakBase = from.project(angle, arrowLength - pikeWidth);

        final APoint empennageBase = from.project(angle, pikeWidth);

        g.drawLine(from, peakBase);
        g.drawLine(empennageBase, angle + Math.PI / 2, pikeWidth);
        g.drawLine(peakBase, angle + Math.PI / 2, pikeWidth);

        final APoint peakPnt1 = peakBase.project(robocode.util.Utils.normalAbsoluteAngle(angle + Math.PI / 2), pikeWidth / 2);
        final APoint peakPnt2 = peakBase.project(robocode.util.Utils.normalAbsoluteAngle(angle - Math.PI / 2), pikeWidth / 2);

        g.drawLine(peakPnt1, to);
        g.drawLine(peakPnt2, to);
    }
}
