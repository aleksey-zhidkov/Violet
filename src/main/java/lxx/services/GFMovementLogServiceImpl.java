package lxx.services;

import ags.utils.KdTree;
import lxx.logs.MovementLog;
import lxx.model.BattleState2;
import lxx.model.LxxWave2;
import lxx.utils.GuessFactor;
import lxx.utils.LxxUtils;
import lxx.utils.ScoredBearingOffset;
import lxx.utils.func.Option;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.sqrt;

public class GFMovementLogServiceImpl implements DataService, GFMovementLogService {

    private final WavesService wavesService = new WavesService();
    private final MovementLog<GuessFactor> log;
    private final String observer;
    private final String observable;

    public GFMovementLogServiceImpl(MovementLog<GuessFactor> log, String observerName, String observable) {
        this.log = log;
        this.observer = observerName;
        this.observable = observable;
    }

    @Override
    public void updateData(BattleState2 state) {

        final ArrayList<WavesService.WaveHitInterval> waveHitIntervals = wavesService.updateData(state);

        for (WavesService.WaveHitInterval waveHitInterval : waveHitIntervals) {
            final LxxWave2 w = waveHitInterval.wave;
            log.addEntry(state.getRobot(observer), state.getRobot(observable), new GuessFactor(waveHitInterval.hitInterval.center(), LxxUtils.getMaxEscapeAngle(w.speed), LxxUtils.lateralDirection(w, w.victim)));
        }

        final Option<LxxWave2> firedBullet = state.getRobotFiredBullet(observer);
        if (firedBullet.defined()) {
            wavesService.registerWave(firedBullet.get());
        }
    }

    public List<ScoredBearingOffset> getVisits(BattleState2 state, double bulletSpeed) {
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