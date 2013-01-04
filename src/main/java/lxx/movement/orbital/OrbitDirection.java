package lxx.movement.orbital;

public enum OrbitDirection {

    CLOCKWISE(1, 8),
    COUNTER_CLOCKWISE(-1, 8),
    STOP(0, 0);

    public final double direction;
    public final double speed;

    private OrbitDirection(double direction, double speed) {
        this.direction = direction;
        this.speed = speed;
    }
}
