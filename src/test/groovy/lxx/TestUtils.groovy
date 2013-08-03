package lxx

import lxx.model.LxxRobot
import lxx.utils.BattleRules
import lxx.utils.LxxPoint
import lxx.utils.func.Option

final class TestUtils {

    private TestUtils() {
    }

    static LxxRobot createRobot(Map params) {
        return new LxxRobot(
                Option.of(params.get('prevState')) as Option<LxxRobot>,
                params.get('rules', null) as BattleRules,
                params.get('name', null) as String,
                params.get('position', null) as LxxPoint,
                params.get('velocity',  Double.NaN) as double,
                params.get('heading',  Double.NaN) as double,
                params.get('energy',  Double.NaN) as double,
                params.get('lastScanTime', -1) as long,
                params.get('time', -1) as long,
                params.get('round', -1) as int,
                params.get('radarHeading', null) as Double,
                params.get('gunHeading', null) as Double,
                params.get('alive', false) as boolean,
                params.get('firePower',  Double.NaN) as double,
                params.get('gunHeat',  Double.NaN) as double,
                params.get('speed',  Double.NaN) as double,
                params.get('acceleration',  Double.NaN)as double,
                params.get('movementDirection', Double.NaN) as double,
                params.get('bulletsInAir', new ArrayList()) as List
        )
    }

}
