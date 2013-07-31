package lxx.services;

import lxx.model.BattleState2;
import lxx.model.LxxRobot2;
import lxx.model.LxxWave2;
import lxx.utils.APoint;
import lxx.utils.IntervalDouble;
import lxx.utils.LxxUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class WavesService {

    private final Map<LxxWave2, IntervalDouble> waves = new HashMap<LxxWave2, IntervalDouble>();

    public void registerWave(LxxWave2 wave) {
        assert !waves.containsKey(wave);
        waves.put(wave, new IntervalDouble());
    }

    public ArrayList<WaveHitInterval> updateData(BattleState2 state) {
        final ArrayList<WaveHitInterval> passedWaves = new ArrayList<WaveHitInterval>();
        for (Iterator<LxxWave2> wavesIter = waves.keySet().iterator(); wavesIter.hasNext();) {
            final LxxWave2 w = wavesIter.next();
            final LxxRobot2 victimCurrentState = state.getRobot(w.victim.name);
            final APoint bulletPos = w.launcher.project(w.launcher.angleTo(victimCurrentState), w.getTraveledDistance(state.time));
            if (LxxUtils.getBoundingRectangleAt(victimCurrentState).contains(bulletPos.x(), bulletPos.y())) {
                final IntervalDouble hitInterval = waves.get(w);
                final double halfRobotWidthInRadians = LxxUtils.getRobotWidthInRadians(w.launcher, victimCurrentState) / 2;
                final double bo = w.getBearingOffset(victimCurrentState);
                waves.put(w, hitInterval.merge(bo - halfRobotWidthInRadians, bo + halfRobotWidthInRadians));
            } else if (w.isPassed(victimCurrentState)) {
                final IntervalDouble hitInterval = waves.get(w);
                assert hitInterval.a != Long.MAX_VALUE && hitInterval.b != Long.MIN_VALUE;
                wavesIter.remove();
                passedWaves.add(new WaveHitInterval(w, hitInterval));
            }
        }

        return passedWaves;
    }

    public class WaveHitInterval {

        public final LxxWave2 wave;
        public final IntervalDouble hitInterval;

        public WaveHitInterval(LxxWave2 wave, IntervalDouble hitInterval) {
            this.wave = wave;
            this.hitInterval = hitInterval;
        }
    }

}
