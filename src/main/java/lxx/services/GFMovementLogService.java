package lxx.services;

import lxx.model.BattleState2;
import lxx.utils.ScoredBearingOffset;

import java.util.List;

public interface GFMovementLogService {

    List<ScoredBearingOffset> getVisits(BattleState2 state, double bulletSpeed);

}
