package lxx.services;

import lxx.model.BattleState;
import lxx.utils.ScoredBearingOffset;

import java.util.List;

public interface GFMovementLogService {

    List<ScoredBearingOffset> getVisits(BattleState state, double bulletSpeed);

}
