package lxx.events;

import lxx.model.LxxBullet;

public class BulletFiredEvent implements LxxEvent {

    public final LxxBullet bullet;

    public BulletFiredEvent(LxxBullet bullet) {
        this.bullet = bullet;
    }
}
