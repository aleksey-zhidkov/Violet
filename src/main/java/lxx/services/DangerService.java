package lxx.services;

import ags.utils.KdTree;
import lxx.logs.MovementLog;
import lxx.model.BattleState;
import lxx.model.LxxBullet;
import lxx.paint.Canvas;
import lxx.paint.Circle;
import lxx.utils.ScoredBearingOffset;
import lxx.model.LxxWave;
import lxx.utils.*;
import lxx.utils.func.F1;
import robocode.util.Utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.Math.abs;

public class DangerService implements DataService {

    private final Map<LxxWave, WaveDangerInfoImpl> waveDangerInfos = new LxxHashMap<LxxWave, WaveDangerInfoImpl>(new createWaveDangerInfo());

    private final MovementLog<GuessFactor> simpleHitsLog;

    public DangerService(MovementLog<GuessFactor> simpleHitsLog) {
        this.simpleHitsLog = simpleHitsLog;
    }

    @Override
    public void updateData(BattleState state) {
        for (LxxBullet bullet : state.getEnemyHitBullets()) {
            simpleHitsLog.addEntry(state.enemy, state.me, new GuessFactor(Utils.normalRelativeAngle(bullet.heading - bullet.wave.noBearingOffset), LxxUtils.getMaxEscapeAngle(bullet.speed),
                    LxxUtils.lateralDirection(bullet.wave, bullet.wave.victim)));
        }

        for (LxxWave goneWave : state.getEnemyGoneWaves()) {
            waveDangerInfos.remove(goneWave);
        }
    }

    public WaveDangerInfo getWaveDangerInfo(final LxxWave wave) {

        if (wave.time > wave.launcher.time) {
            return new WaveDangerInfo() {
                @Override
                public double getPointDanger(APoint pnt) {
                    return LxxUtils.getRobotWidthInRadians(wave.launcher, pnt);
                }

                @Override
                public void draw(Canvas c, long time) {
                }
            };
        }

        return waveDangerInfos.get(wave);
    }

    private double getPointDanger(LxxWave wave, APoint pnt, List<ScoredBearingOffset> predictedBearingOffsets) {
        if (predictedBearingOffsets.size() == 0) {
            return 0;
        }
        final LxxPoint firePos = wave.launcher.position;
        final double alpha = LxxUtils.angle(firePos.x, firePos.y, pnt.x(), pnt.y());
        final double bearingOffset = Utils.normalRelativeAngle(alpha - wave.noBearingOffset);
        final double robotWidthInRadians = LxxUtils.getRobotWidthInRadians(alpha, firePos.aDistance(pnt));

        double bulletsDanger = 0;
        final double hiEffectDist = robotWidthInRadians * 0.75;
        final double lowEffectDist = robotWidthInRadians * 2.55;
        for (ScoredBearingOffset bo : predictedBearingOffsets) {
            if (bo.bearingOffset < bearingOffset - lowEffectDist) {
                continue;
            } else if (bo.bearingOffset > bearingOffset + lowEffectDist) {
                break;
            }
            final double dist = abs(bearingOffset - bo.bearingOffset);
            if (dist < hiEffectDist) {
                bulletsDanger += (2 - (dist / hiEffectDist)) * bo.score;
            } else if (dist < lowEffectDist) {
                bulletsDanger += (1 - (dist / lowEffectDist)) * bo.score;
            }
        }

        assert bulletsDanger >= 0;
        return bulletsDanger;
    }

    private class createWaveDangerInfo implements F1<LxxWave, WaveDangerInfoImpl> {
        @Override
        public WaveDangerInfoImpl f(LxxWave lxxWave) {
            return new WaveDangerInfoImpl(lxxWave,
                    visits(lxxWave, simpleHitsLog.getEntries(lxxWave.launcher, lxxWave.victim, simpleHitsLog.size())));
        }

        private List<ScoredBearingOffset> visits(LxxWave wave, List<KdTree.Entry<GuessFactor>> entries) {
            final ArrayList<ScoredBearingOffset> visits = new ArrayList<ScoredBearingOffset>();

            if (entries.size() == 0) {
                return visits;
            }

            double maxDist = entries.get(0).distance + 0.00001;
            for (KdTree.Entry<GuessFactor> entry : entries) {
                final double score = 1 - entry.distance / maxDist;
                assert score > 0 && score <= 1;
                final double bearingOffset = entry.value.getBearingOffset(LxxUtils.getMaxEscapeAngle(wave.speed), LxxUtils.lateralDirection(wave, wave.victim));
                assert bearingOffset >= -LxxConstants.RADIANS_60 &&
                        bearingOffset <= LxxConstants.RADIANS_60;
                visits.add(new ScoredBearingOffset(bearingOffset, score));
            }

            return visits;
        }
    }

    private class WaveDangerInfoImpl implements WaveDangerInfo {

        private final LxxWave wave;
        private final List<ScoredBearingOffset> bos;

        private WaveDangerInfoImpl(LxxWave wave, List<ScoredBearingOffset> bos) {
            this.wave = wave;
            this.bos = bos;
        }

        @Override
        public double getPointDanger(APoint pnt) {
            return DangerService.this.getPointDanger(wave, pnt, bos);
        }

        @Override
        public void draw(Canvas c, long time) {
            if (!c.enabled()) {
                return;
            }

            final double travelledDist = wave.getTraveledDistance(time);

            for (ScoredBearingOffset bo : bos) {
                c.draw(new Circle(wave.launcher, travelledDist), new Color(255, 255, 255, 155));
                c.draw(new Circle(wave.launcher.project(wave.noBearingOffset + bo.bearingOffset, travelledDist - wave.speed), 2, true),
                        new Color(255, 255, 255, (int) (255 * bo.score)));
            }
        }
    }

}
