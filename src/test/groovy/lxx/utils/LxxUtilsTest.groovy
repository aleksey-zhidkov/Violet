package lxx.utils

import org.junit.Test
import robocode.Rules
import robocode.util.Utils

import static java.lang.Math.abs
import static java.lang.Math.pow
import static java.lang.Math.sqrt
import static java.lang.Math.toRadians
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue;

class LxxUtilsTest {

    @Test
    def void testLatAdvVelocities() {
        final Values latVelValues = new Values(361 * 361 * 17)
        final Values advVelValues = new Values(361 * 361 * 17)
        for (angle in 0..360) {
            for (velocity in -8..8) {
                for (heading in 0..360) {
                    final LxxPoint center = new LxxPoint(400, 300)
                    final LxxPoint pnt = center.project(toRadians(angle), 300)
                    final headingRadians = toRadians(heading)

                    final lateralVelocity = LxxUtils.lateralVelocity(center, pnt, velocity, headingRadians)
                    final advancingVelocity = LxxUtils.advancingVelocity(center, pnt, velocity, headingRadians)

                    latVelValues.addValue(lateralVelocity)
                    advVelValues.addValue(advancingVelocity)

                    assertTrue(lateralVelocity >= -Rules.MAX_VELOCITY)
                    assertTrue(lateralVelocity <= Rules.MAX_VELOCITY)

                    assertTrue(advancingVelocity >= -Rules.MAX_VELOCITY)
                    assertTrue(advancingVelocity <= Rules.MAX_VELOCITY)

                    assertEquals(abs(velocity), sqrt(pow(lateralVelocity, 2) + pow(advancingVelocity, 2)), 2)
                }
            }
        }

        assertEquals(latVelValues.minValue, -Rules.MAX_VELOCITY, Utils.NEAR_DELTA)
        assertEquals(latVelValues.maxValue, Rules.MAX_VELOCITY, Utils.NEAR_DELTA)
        assertEquals(latVelValues.avgValue.currentValue, 0, Utils.NEAR_DELTA)

        assertEquals(advVelValues.minValue, -Rules.MAX_VELOCITY, Utils.NEAR_DELTA)
        assertEquals(advVelValues.maxValue, Rules.MAX_VELOCITY, Utils.NEAR_DELTA)
        assertEquals(advVelValues.avgValue.currentValue, 0, Utils.NEAR_DELTA)
    }

}