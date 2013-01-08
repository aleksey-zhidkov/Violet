package lxx.gun;

import lxx.model.BattleState;

public interface Gun {

    Double getGunTurnAngle(BattleState state, double bulletSpeed);

}
