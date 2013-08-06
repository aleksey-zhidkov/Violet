package lxx.services;

import lxx.model.LxxRobot;
import lxx.model.LxxWave;
import lxx.movement.orbital.OrbitDirection;

import java.util.*;

public final class MonitoringService {

    public static final String FOUR_DIGITS_DOUBLE_FORMAT = "%4.4f";

    private static final String DIST_DANGER_KEY = ".distDanger";
    private static final String POINT_DANGER_KEY = ".pointDanger";
    private static final String FLIGHT_TIME_KEY = ".flightTime";
    private static final String TOTAL_DANGER_KEY = ".totalDanger";

    private static final String ORBIT_DIR_KEY = "orbitDir";

    private static final Properties props = new Properties();
    private static final Map<String, LxxRobot> robots = new TreeMap<String, LxxRobot>();
    private static final Map<String, String> robotHitRates = new HashMap<String, String>();

    private static int robotHits;
    private static int wallHits;

    private MonitoringService() {
    }

    public static void setOrbitDirection(OrbitDirection orbitDirection) {
        props.put(ORBIT_DIR_KEY, orbitDirection);
    }

    public static void setDangerComponents(OrbitDirection dir, double minDist, double pointDanger, double flightTime, double totalDanger) {
        props.put(dir + DIST_DANGER_KEY, String.format(FOUR_DIGITS_DOUBLE_FORMAT, minDist));
        props.put(dir + POINT_DANGER_KEY, String.format(FOUR_DIGITS_DOUBLE_FORMAT, pointDanger));
        props.put(dir + FLIGHT_TIME_KEY, String.format(FOUR_DIGITS_DOUBLE_FORMAT, flightTime));
        props.put(dir + TOTAL_DANGER_KEY, String.format(FOUR_DIGITS_DOUBLE_FORMAT, totalDanger));
    }

    public static void setRobot(LxxRobot robot) {
        robots.put(robot.name, robot);
    }

    public static void robotHitted() {
        robotHits++;
    }

    public static void wallHitted() {
        wallHits++;
    }

    public static void setRobotHitRate(String robotName, String hitRate) {
        robotHitRates.put(robotName, hitRate);
    }

    public static String formatData() {
        final StringBuilder builder = new StringBuilder();

        builder.append("Wall hits: ").append(wallHits).append('\n');
        builder.append("Robot hits: ").append(robotHits).append('\n');


        builder.append("\n\nOrbit direction: ").append(props.get(ORBIT_DIR_KEY)).append('\n');
        builder.append(formatOdDanger(OrbitDirection.CLOCKWISE));
        builder.append(formatOdDanger(OrbitDirection.STOP));
        builder.append(formatOdDanger(OrbitDirection.COUNTER_CLOCKWISE));

        builder.append('\n');
        for (LxxRobot robot : robots.values()) {
            builder.append(formatRobot(robot)).append('\n');
        }

        return builder.toString();
    }

    private static String formatOdDanger(OrbitDirection dir) {
        final StringBuilder res = new StringBuilder();

        res.append(dir).append('\n');
        res.append("  distDanger = ").append(props.get(dir + DIST_DANGER_KEY)).append('\n');
        res.append("  pointDanger = ").append(props.get(dir + POINT_DANGER_KEY)).append('\n');
        res.append("  flightTime = ").append(props.get(dir + FLIGHT_TIME_KEY)).append('\n');
        res.append("  totalDanger = ").append(props.get(dir + TOTAL_DANGER_KEY)).append('\n');

        return res.toString();
    }

    private static String formatRobot(LxxRobot robot) {
        final StringBuilder res = new StringBuilder();

        res.append(robot.name).append(":\n");
        res.append("Hit rate: ").append(robotHitRates.get(robot.name)).append('\n');
        res.append("Energy: ").append(String.format(FOUR_DIGITS_DOUBLE_FORMAT, robot.energy)).append('\n');
        res.append("Gun heat: ").append(String.format(FOUR_DIGITS_DOUBLE_FORMAT, robot.gunHeat)).append('\n');

        for (LxxWave wave : robot.bulletsInAir) {
            res.append(formatWave(wave)).append('\n');
        }
        // placeholders for bullets in air
        for (int i = 0; i < 3 - robot.bulletsInAir.size(); i++) {
            res.append("\n\n");
        }

        return res.toString();
    }

    private static String formatWave(LxxWave wave) {
        final StringBuilder res = new StringBuilder();

        res.append("  Launch time: ").append(wave.time).append('\n');
        res.append("  Fire pos: ").append(wave.launcher.position).append('\n');

        return res.toString();
    }


}
