package lxx.strategy;

public class TurnDecision {

    public final double desiredVelocity;
    public final double turnRate;

    public final Double gunTurnRate;
    public final Double firePower;

    public final Double radarTurnRate;

    public TurnDecision(double desiredVelocity, double turnRate, Double gunTurnRate, Double firePower, double radarTurnRate) {
        this.desiredVelocity = desiredVelocity;
        this.turnRate = turnRate;
        this.gunTurnRate = gunTurnRate;
        this.firePower = firePower;
        this.radarTurnRate = radarTurnRate;
    }
}
