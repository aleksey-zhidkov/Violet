package lxx.services;

import lxx.movement.orbital.OrbitDirection;

import java.util.Properties;

public class MonitoringService {

    private static final String orbitDirKey = "orbitDir";

    private static final Properties props = new Properties();

    public static void setOrbitDirection(OrbitDirection orbitDirection) {
        props.put(orbitDirKey, orbitDirection);
    }

    public static void setDangerComponents(OrbitDirection dir, double minDist, double pointDanger, double flightTime, double totalDanger) {
        props.put(dir + ".distDanger", String.format("%4.4f", minDist));
        props.put(dir + ".pointDanger", String.format("%4.4f", pointDanger));
        props.put(dir + ".flightTime", String.format("%4.4f", flightTime));
        props.put(dir + ".totalDanger", String.format("%4.4f", totalDanger));
    }

    public static String formatData() {
        final StringBuilder builder = new StringBuilder();

        builder.append("\n\nOrbit direction: ").append(props.get(orbitDirKey)).append('\n');
        builder.append(formatOdDanger(OrbitDirection.CLOCKWISE));
        builder.append(formatOdDanger(OrbitDirection.STOP));
        builder.append(formatOdDanger(OrbitDirection.COUNTER_CLOCKWISE));

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

}
