package lxx.services;

import lxx.model.BattleState;
import lxx.model.LxxRobot;
import lxx.model.LxxWave;
import lxx.utils.APoint;
import lxx.utils.IntervalDouble;
import lxx.utils.LxxUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class WavesService {

    private final Map<LxxWave, IntervalDouble> waves = new HashMap<LxxWave, IntervalDouble>();

    public void registerWave(LxxWave wave) {
        assert !waves.containsKey(wave);
        waves.put(wave, new IntervalDouble());
    }

    public ArrayList<WaveHitInterval> updateData(BattleState state) {
        final ArrayList<WaveHitInterval> passedWaves = new ArrayList<WaveHitInterval>();
        for (Iterator<LxxWave> wavesIter = waves.keySet().iterator(); wavesIter.hasNext();) {
            final LxxWave w = wavesIter.next();
            final LxxRobot victimCurrentState = state.getRobot(w.victim.name);
            final APoint bulletPos = w.launcher.project(w.launcher.angleTo(victimCurrentState), w.getTraveledDistance(state.time));
            if (LxxUtils.getBoundingRectangleAt(victimCurrentState).contains(bulletPos.x(), bulletPos.y())) {
                final IntervalDouble hitInterval = waves.get(w);
                final double halfRobotWidthInRadians = LxxUtils.getRobotWidthInRadians(w.launcher, victimCurrentState) / 2;
                final double bo = w.getBearingOffset(victimCurrentState);
                waves.put(w, hitInterval.merge(bo - halfRobotWidthInRadians, bo + halfRobotWidthInRadians));
            } else if (w.isPassed(victimCurrentState)) {
                final IntervalDouble hitInterval = waves.get(w);
                if (hitInterval.b == Long.MAX_VALUE) {
                    assert hitInterval.b != Long.MAX_VALUE;
                }
                wavesIter.remove();
                passedWaves.add(new WaveHitInterval(w, hitInterval));
            }
        }

        return passedWaves;
    }

    public class WaveHitInterval {

        public final LxxWave wave;
        public final IntervalDouble hitInterval;

        public WaveHitInterval(LxxWave wave, IntervalDouble hitInterval) {
            this.wave = wave;
            this.hitInterval = hitInterval;
        }
    }

}
