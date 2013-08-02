package lxx.utils

import robocode.Rules
import robocode.util.Utils
import spock.lang.Specification

class GuessFactorSpec extends Specification {

    def "Guess factor should be calculated correctly"(double loggedBearingOffset, double loggedLateralDirection,
                                                      double currentLateralDirection, double expectedBearingOffset) {

        final double mae = LxxUtils.getMaxEscapeAngle(Rules.getBulletSpeed(3))
        when:
        final GuessFactor gf = new GuessFactor(loggedBearingOffset, mae, loggedLateralDirection)

        then:
        Utils.isNear(gf.getBearingOffset(mae, currentLateralDirection), expectedBearingOffset)

        where:
        loggedBearingOffset | loggedLateralDirection | currentLateralDirection | expectedBearingOffset
        LxxConstants.RADIANS_45 | 1 | 1 | LxxConstants.RADIANS_45
        -LxxConstants.RADIANS_45 | 1 | 1 | -LxxConstants.RADIANS_45

        LxxConstants.RADIANS_45 | -1 | 1 | -LxxConstants.RADIANS_45
        LxxConstants.RADIANS_45 | 1 | 1 | LxxConstants.RADIANS_45

        LxxConstants.RADIANS_45 | 1 | 1 | LxxConstants.RADIANS_45
        LxxConstants.RADIANS_45 | 1 | -1 | -LxxConstants.RADIANS_45

        LxxConstants.RADIANS_45 | -1 | -1 | LxxConstants.RADIANS_45
        -LxxConstants.RADIANS_45 | -1 | -1 | -LxxConstants.RADIANS_45

    }

}
