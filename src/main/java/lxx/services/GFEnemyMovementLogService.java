package lxx.services;

import ags.utils.KdTree;
import lxx.model.BattleState;
import lxx.model.LxxRobot;
import lxx.model.LxxWave;
import lxx.utils.BearingOffsetDanger;
import lxx.utils.GuessFactor;
import lxx.utils.LxxConstants;
import lxx.utils.LxxUtils;
import robocode.Rules;
import robocode.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.Math.max;
import static java.lang.Math.sqrt;

public class GFEnemyMovementLogService implements DataService {

    private final WavesService wavesService = new WavesService();
    private final KdTree<GuessFactor> tree;

    public GFEnemyMovementLogService(Map<String, Object> staticData) {
        final String key = getClass() + ".simple";
        if (!staticData.containsKey(key)) {
            staticData.put(key, new KdTree.SqrEuclid(6, Integer.MAX_VALUE));
        }
        tree = (KdTree<GuessFactor>) staticData.get(key);
    }

    @Override
    public void updateData(BattleState state) {

        final ArrayList<WavesService.WaveHitInterval> waveHitIntervals = wavesService.updateData(state);

        for (WavesService.WaveHitInterval waveHitInterval : waveHitIntervals) {
            final LxxWave w = waveHitInterval.wave;
            tree.addPoint(getLocation(w.launcher, w.victim), new GuessFactor(waveHitInterval.hitInterval.center(), LxxUtils.getMaxEscapeAngle(w.speed), LxxUtils.lateralDirection(w, w.victim)));
        }

        if (state.myFiredBullet != null) {
            wavesService.registerWave(state.myFiredBullet);
        }
    }

    public List<BearingOffsetDanger> getVisits(BattleState state, double bulletSpeed) {
        final List<BearingOffsetDanger> visits = new ArrayList<BearingOffsetDanger>();

        final double[] currentLoc = getLocation(state.me, state.enemy);
        final List<KdTree.Entry<GuessFactor>> entries = tree.nearestNeighbor(currentLoc, (int) max(10, sqrt(tree.size())), true);
        if (entries.size() == 0) {
            return visits;
        }
        final double maxDist = entries.get(0).distance;

        for (KdTree.Entry<GuessFactor> entry : entries) {
            visits.add(new BearingOffsetDanger(entry.value.getBearingOffset(LxxUtils.getMaxEscapeAngle(bulletSpeed),
                    LxxUtils.lateralDirection(state.me, state.enemy)),
                    entry.distance / maxDist));
        }

        return visits;
    }

    private static double[] getLocation(LxxRobot me, LxxRobot enemy) {
        return new double[]{
                me.distance(enemy) / me.rules.field.fieldDiagonal,
                LxxUtils.lateralVelocity(me, enemy) / Rules.MAX_VELOCITY,
                LxxUtils.advancingVelocity(me, enemy) / Rules.MAX_VELOCITY,
                enemy.acceleration / 3,
                (Double.isNaN(enemy.movementDirection)
                        ? enemy.position.distanceToWall(enemy.rules.field, enemy.heading)
                        : enemy.position.distanceToWall(enemy.rules.field, enemy.movementDirection)) / me.rules.field.fieldDiagonal,
                (Double.isNaN(enemy.movementDirection)
                        ? enemy.position.distanceToWall(enemy.rules.field, Utils.normalAbsoluteAngle(enemy.heading + LxxConstants.RADIANS_180))
                        : enemy.position.distanceToWall(enemy.rules.field, Utils.normalAbsoluteAngle(enemy.movementDirection + LxxConstants.RADIANS_180))) /
                        me.rules.field.fieldDiagonal
        };
    }

}