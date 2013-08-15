package lxx.services;

import lxx.model.LxxRobot;
import lxx.model.LxxWave;

import java.util.*;

public final class MonitoringService {

    public static final String FOUR_DIGITS_DOUBLE_FORMAT = "%4.4f";

    private static final Map<String, LxxRobot> robots = new TreeMap<String, LxxRobot>();
    private static final Map<String, String> robotHitRates = new HashMap<String, String>();

    private static int robotHits;
    private static int wallHits;
    private static WaveDangerInfo surfingDangerInfo;
    private static int undetectedBullets;

    private MonitoringService() {
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

    public static void setSurfingDangerInfo(WaveDangerInfo surfingDangerInfo) {
        MonitoringService.surfingDangerInfo = surfingDangerInfo;
    }

    public static void waveForBulletNotFound() {
        undetectedBullets++;
    }

    public static String formatData() {
        final StringBuilder builder = new StringBuilder();

        builder.append("Wall hits: ").append(wallHits).append('\n');
        builder.append("Robot hits: ").append(robotHits).append('\n');
        builder.append("Undetected bullets: ").append(undetectedBullets).append('\n');

        builder.append("\n\nSurfing wave dangerInfo: ").append(surfingDangerInfo).append("\n");

        builder.append('\n');
        for (LxxRobot robot : robots.values()) {
            builder.append(formatRobot(robot)).append('\n');
        }

        return builder.toString();
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
        return "  Launch time: " + wave.time + '\n' +
                "  Fire pos: " + wave.launcher.position + '\n';
    }

}
