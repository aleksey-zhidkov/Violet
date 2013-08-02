package lxx.utils

import org.junit.Test
import robocode.util.Utils

import static java.lang.Math.abs
import static java.lang.Math.pow
import static java.lang.Math.sqrt
import static java.lang.Math.toRadians
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue
import static robocode.Rules.MAX_VELOCITY;

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

                    assertTrue(lateralVelocity >= -MAX_VELOCITY)
                    assertTrue(lateralVelocity <= MAX_VELOCITY)

                    assertTrue(advancingVelocity >= -MAX_VELOCITY)
                    assertTrue(advancingVelocity <= MAX_VELOCITY)

                    assertEquals(abs(velocity), sqrt(pow(lateralVelocity, 2) + pow(advancingVelocity, 2)), 2)
                }
            }
        }

        assertEquals(latVelValues.minValue, (double) -MAX_VELOCITY, Utils.NEAR_DELTA)
        assertEquals(latVelValues.maxValue, MAX_VELOCITY, Utils.NEAR_DELTA)
        assertEquals(latVelValues.avgValue.currentValue, 0, Utils.NEAR_DELTA)

        assertEquals(advVelValues.minValue, (double) -MAX_VELOCITY, Utils.NEAR_DELTA)
        assertEquals(advVelValues.maxValue, MAX_VELOCITY, Utils.NEAR_DELTA)
        assertEquals(advVelValues.avgValue.currentValue, 0, Utils.NEAR_DELTA)
    }

}