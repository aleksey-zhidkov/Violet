package lxx.model;

import robocode.Bullet;

public class LxxBullet {

    public final LxxWave wave;
    public final Bullet bullet;
    public final LxxBulletState state;

    public final double x;
    public final double y;
    public final double speed;
    public final double heading;

    public LxxBullet(LxxWave wave, Bullet bullet, LxxBulletState state) {
        this.wave = wave;
        this.bullet = bullet;
        this.state = state;

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
