package lxx.services;

import ags.utils.KdTree;
import lxx.events.BulletFiredEventListener;
import lxx.events.TickEventListener;
import lxx.logs.KdTreeMovementLog;
import lxx.logs.MovementLog;
import lxx.logs.SimpleLocationFactory;
import lxx.model.BattleState;
import lxx.model.LxxBullet;
import lxx.model.LxxRobot;
import lxx.model.LxxWave;
import lxx.utils.GuessFactor;
import lxx.utils.LxxUtils;
import lxx.utils.ScoredBearingOffset;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.sqrt;

public class GFMovementLogServiceImpl implements TickEventListener, GFMovementLogService, BulletFiredEventListener {

    private final WavesService wavesService = new WavesService();
    private final MovementLog<GuessFactor> log;
    private final String observer;
    private final String observable;

    public GFMovementLogServiceImpl(StaticDataStorage dataStorage, String observerName, String observable) {
        final String id = "Enemy movement kdTree";
        final SimpleLocationFactory locationFactory = new SimpleLocationFactory();
        if (!dataStorage.containsData(id)) {
            dataStorage.saveData(id, new KdTree.SqrEuclid<GuessFactor>(locationFactory.getDimensionCount(), Integer.MAX_VALUE));
        }
        this.log = new KdTreeMovementLog<GuessFactor>(dataStorage.<KdTree<GuessFactor>>getData(id), locationFactory);
        this.observer = observerName;
        this.observable = observable;
    }

    @Override
    public void onTick(BattleState state) {

        final List<WavesService.WaveHitInterval> waveHitIntervals = wavesService.updateData(state);

        final LxxRobot launcher = state.getRobot(observer);
        final LxxRobot victim = state.getRobot(observable);
        for (WavesService.WaveHitInterval waveHitInterval : waveHitIntervals) {
            final LxxWave w = waveHitInterval.wave;
            log.addEntry(launcher, victim, new GuessFactor(waveHitInterval.hitInterval.center(), LxxUtils.getMaxEscapeAngle(w.speed), LxxUtils.lateralDirection(w, w.victim), launcher, victim));
        }
    }

    @Override
    public void onBulletFired(LxxBullet bullet) {
        if (bullet.wave.victim.alive) {
            wavesService.registerWave(bullet.wave);
        }
    }

    public List<ScoredBearingOffset> getVisits(BattleState state, double bulletSpeed) {
        final List<ScoredBearingOffset> visits = new ArrayList<ScoredBearingOffset>();

        final List<KdTree.Entry<GuessFactor>> entries = log.getEntries(state.getRobot(observer), state.getRobot(observable), (int) max(10, sqrt(log.size())));
        if (entries.size() == 0) {
            return visits;
        }
        final double maxDist = entries.get(0).distance + 0.00001;

        for (KdTree.Entry<GuessFactor> entry : entries) {
            final double score = 1 - entry.distance / maxDist;
            assert score > 0 && score <= 1;
            visits.add(new ScoredBearingOffset(entry.value.getBearingOffset(LxxUtils.getMaxEscapeAngle(bulletSpeed),
                    LxxUtils.lateralDirection(state.getRobot(observer), state.getRobot(observable))), score));
        }

        return visits;
    }

}
