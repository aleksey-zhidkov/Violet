package lxx.model;

import lxx.utils.LxxPoint;
import robocode.Bullet;

import java.util.LinkedList;
import java.util.List;

public class LxxRobotInfo {

    public final List<Bullet> hitBullets = new LinkedList<Bullet>();
    public final List<Bullet> interceptedBullets = new LinkedList<Bullet>();

    public LxxPoint position;
    public double velocity;
    public double heading;
    public double energy;
    public long time;
    public int round;
    public Double radarHeading;
    public Double gunHeading;
    public boolean alive;
    public Double wallDmg;
    public double receivedDmg;
    public double returnedEnergy;
    public boolean hitRobot;

    public String name;
}
