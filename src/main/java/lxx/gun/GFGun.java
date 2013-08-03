package lxx.gun;

import lxx.model.BattleState;
import lxx.services.GFMovementLogService;
import lxx.utils.ScoredBearingOffset;
import robocode.util.Utils;

import java.util.List;

import static java.lang.Math.*;

public class GFGun implements Gun {

    private final GFMovementLogService logService;
    private Double bearingOffset;

    public GFGun(GFMovementLogService logService) {
        this.logService = logService;
    }

    @Override
    public Double getGunTurnAngle(BattleState state, double bulletSpeed) {
        final double targetAngle;
        if (state.me.getTurnsToGunCool() > 2 || state.opponent.energy == 0) {
            bearingOffset = null;
            targetAngle = state.me.angleTo(state.opponent);
        } else {
            if (bearingOffset == null) {
                bearingOffset = getBearingOffset(state, bulletSpeed);
            }
            targetAngle = state.me.angleTo(state.opponent) + bearingOffset;
        }

        return Utils.normalRelativeAngle(targetAngle - state.me.gunHeading);
    }

    private Double getBearingOffset(BattleState state, double bulletSpeed) {
        final List<ScoredBearingOffset> visits = logService.getVisits(state, bulletSpeed);
        if (visits.isEmpty()) {
            return 0d;
        }

        final double[] boScores = new double[205];
        int bestBo = 104;
        for (ScoredBearingOffset bo : visits) {
            assert bo.score <= 1;
            final int centerIdx = (int) (toDegrees(bo.bearingOffset * 2) + 104);
            final int fromIdx = max(0, centerIdx - 5);
            final int toIdx = min(boScores.length - 1, centerIdx + 5);

            for (int i = fromIdx; i <= toIdx; i++) {
                boScores[i] += bo.score / (abs(i - centerIdx) + 1);
                if (boScores[i] > boScores[bestBo]) {
                    bestBo = i;
                }
            }
        }

        return toRadians(bestBo - 104) / 2;
    }
}
