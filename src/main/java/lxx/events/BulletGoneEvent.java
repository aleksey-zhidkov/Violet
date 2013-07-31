package lxx.events;

import lxx.model.LxxWave;

public class BulletGoneEvent implements LxxEvent {

    public final LxxWave wave;

    public BulletGoneEvent(LxxWave wave) {
        this.wave = wave;
    }
}
