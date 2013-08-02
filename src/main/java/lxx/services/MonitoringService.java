package lxx.services;

import lxx.model.LxxRobot;
import lxx.movement.orbital.OrbitDirection;

import java.util.*;

public class MonitoringService {

	public static final String FOUR_DIGITS_DOUBLE_FORMAT = "%4.4f";

	private static final String orbitDirKey = "orbitDir";

    private static final Properties props = new Properties();

	private static final Map<String, LxxRobot> robots = new TreeMap<String, LxxRobot>();

	public static void setOrbitDirection(OrbitDirection orbitDirection) {
        props.put(orbitDirKey, orbitDirection);
    }

    public static void setDangerComponents(OrbitDirection dir, double minDist, double pointDanger, double flightTime, double totalDanger) {
        props.put(dir + ".distDanger", String.format(FOUR_DIGITS_DOUBLE_FORMAT, minDist));
        props.put(dir + ".pointDanger", String.format(FOUR_DIGITS_DOUBLE_FORMAT, pointDanger));
        props.put(dir + ".flightTime", String.format(FOUR_DIGITS_DOUBLE_FORMAT, flightTime));
        props.put(dir + ".totalDanger", String.format(FOUR_DIGITS_DOUBLE_FORMAT, totalDanger));
    }

	public static void setRobot(LxxRobot robot) {
		robots.put(robot.name, robot);
	}

    public static String formatData() {
        final StringBuilder builder = new StringBuilder();

        builder.append("\n\nOrbit direction: ").append(props.get(orbitDirKey)).append('\n');
        builder.append(formatOdDanger(OrbitDirection.CLOCKWISE));
        builder.append(formatOdDanger(OrbitDirection.STOP));
        builder.append(formatOdDanger(OrbitDirection.COUNTER_CLOCKWISE));

		builder.append('\n');
		for (LxxRobot robot : robots.values()) {
			builder.append(formatRobot(robot));
		}

        return builder.toString();
    }

    private static String formatOdDanger(OrbitDirection dir) {
        final StringBuilder res = new StringBuilder();

        res.append(dir).append('\n');
        res.append("  distDanger = ").append(props.get(dir + ".distDanger")).append('\n');
        res.append("  pointDanger = ").append(props.get(dir + ".pointDanger")).append('\n');
        res.append("  flightTime = ").append(props.get(dir + ".flightTime")).append('\n');
        res.append("  totalDanger = ").append(props.get(dir + ".totalDanger")).append('\n');

        return res.toString();
    }

	private static String formatRobot(LxxRobot robot) {
		final StringBuilder res = new StringBuilder();

		res.append(robot.name).append(":\n");
		res.append("Energy: ").append(String.format(FOUR_DIGITS_DOUBLE_FORMAT, robot.energy)).append('\n');
		res.append("Gun heat: ").append(String.format(FOUR_DIGITS_DOUBLE_FORMAT, robot.gunHeat)).append('\n');

		return res.toString();
	}

}
