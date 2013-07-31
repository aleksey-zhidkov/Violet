package lxx.gun;

import lxx.model.BattleState2;

public interface Gun {

    Double getGunTurnAngle(BattleState2 state, double bulletSpeed);

}
