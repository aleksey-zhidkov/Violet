package lxx.events;

import lxx.model.BattleState;
import lxx.model.LxxBullet;
import lxx.model.LxxWave;

import java.util.ArrayList;
import java.util.List;

public class EventsChannel {

    private final List<BulletDetectedEventListener> bulletDetectedEventListeners = new ArrayList<BulletDetectedEventListener>();
    private final List<BulletFiredEventListener> bulletFiredEventListeners = new ArrayList<BulletFiredEventListener>();
    private final List<WaveGoneEventListener> waveGoneEventListeners = new ArrayList<WaveGoneEventListener>();
    private final List<TickEventListener> tickEventListeners = new ArrayList<TickEventListener>();

    public void fireEvent(LxxEvent event) {
        if (event instanceof BulletDetectedEvent) {
            final LxxBullet bullet = ((BulletDetectedEvent) event).bullet;
            for (BulletDetectedEventListener listener: bulletDetectedEventListeners) {
                listener.onBulletDetected(bullet);
            }
        } else if (event instanceof BulletFiredEvent) {
            final LxxBullet bullet = ((BulletFiredEvent) event).bullet;
            for (BulletFiredEventListener listener : bulletFiredEventListeners) {
                listener.onBulletFired(bullet);
            }
        } else if (event instanceof BulletGoneEvent) {
            final LxxWave wave = ((BulletGoneEvent) event).wave;
            for (WaveGoneEventListener listener : waveGoneEventListeners) {
                listener.onWaveGone(wave);
            }
        } else if (event instanceof TickEvent) {
            final BattleState state = ((TickEvent) event).battleState;
            for (TickEventListener listener : tickEventListeners) {
                listener.onTick(state);
            }
        } else {
            throw new IllegalArgumentException("Unsupported event type: " + event.getClass());
        }
    }

    public void addBulletDetectedEventListener(BulletDetectedEventListener listener) {
        bulletDetectedEventListeners.add(listener);
    }

    public void addWaveGoneEventListener(WaveGoneEventListener listener) {
        waveGoneEventListeners.add(listener);
    }

    public void addBulletFiredListener(BulletFiredEventListener listener) {
        bulletFiredEventListeners.add(listener);
    }

    public void addTickEventsListener(TickEventListener listener) {
        tickEventListeners.add(listener);
    }
}
