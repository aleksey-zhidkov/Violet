package lxx.events;

import lxx.model.LxxBullet;

public class BulletDetectedEvent implements LxxEvent {

    public final LxxBullet bullet;

    public BulletDetectedEvent(LxxBullet bullet) {
        this.bullet = bullet;
    }
}
