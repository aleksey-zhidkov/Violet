package lxx.services;

import lxx.paint.Canvas;
import lxx.utils.APoint;

public interface WaveDangerInfo {

    double getPointDanger(APoint pnt);

    void draw(Canvas c, long time);

}
