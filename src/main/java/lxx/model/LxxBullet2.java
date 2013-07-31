package lxx.model;

import robocode.Bullet;

public class LxxBullet2 {

    public final LxxWave2 wave;
    public final Bullet bullet;

    public final double x;
    public final double y;
    public final double speed;
    public final double heading;

    public LxxBullet2(LxxWave2 wave, Bullet bullet) {
        this.wave = wave;
        this.bullet = bullet;

        x = bullet.getX();
        y = bullet.getY();
        speed = bullet.getVelocity();
        heading = bullet.getHeadingRadians();
    }

    public double getX() {
        return bullet.getX();
    }

    public double getY() {
        return bullet.getY();
    }
}
