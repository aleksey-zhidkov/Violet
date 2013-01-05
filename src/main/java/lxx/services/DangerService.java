package lxx.services;

import lxx.utils.BearingOffsetDanger;
import lxx.model.LxxWave;
import lxx.utils.*;
import lxx.utils.func.F1;
import robocode.util.Utils;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.random;

public class DangerService {

    private final LxxHashMap<LxxWave, List<BearingOffsetDanger>> waveDangers = new LxxHashMap<LxxWave, List<BearingOffsetDanger>>(
            new F1<LxxWave, List<BearingOffsetDanger>>() {
                public List<BearingOffsetDanger> f(LxxWave lxxWave) {
                    final List<BearingOffsetDanger> res = new ArrayList<BearingOffsetDanger>();

                    for (int i = 0; i < 100; i++) {
                        res.add(new BearingOffsetDanger(random() * LxxConstants.RADIANS_90 - LxxConstants.RADIANS_45, random()));
                    }

                    return res;
                }
            }
    );

    public WaveDangerInfo getWaveDangerInfo(final LxxWave wave) {

        if (wave.time > wave.launcher.time) {
            return new WaveDangerInfo() {
                @Override
                public double getPointDanger(APoint pnt) {
                    return LxxUtils.getRobotWidthInRadians(wave.launcher, pnt);
                }
            };
        }

        return new WaveDangerInfo() {
            public double getPointDanger(final APoint pnt) {
                return DangerService.this.getPointDanger(wave, pnt, waveDangers.get(wave));
            }
        };
    }

    private double getPointDanger(LxxWave wave, APoint pnt, List<BearingOffsetDanger> predictedBearingOffsets) {
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
        for (BearingOffsetDanger bo : predictedBearingOffsets) {
            if (bo.bearingOffset < bearingOffset - lowEffectDist) {
                continue;
            } else if (bo.bearingOffset > bearingOffset + lowEffectDist) {
                break;
            }
            final double dist = abs(bearingOffset - bo.bearingOffset);
            if (dist < hiEffectDist) {
                bulletsDanger += (2 - (dist / hiEffectDist)) * bo.danger;
            } else if (dist < lowEffectDist) {
                bulletsDanger += (1 - (dist / lowEffectDist)) * bo.danger;
            }
        }

        return bulletsDanger;
    }

}
