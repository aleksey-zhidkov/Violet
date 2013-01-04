package lxx.tests

import robocode.RobotStatus

public class RobotStatusFactory {

    static RobotStatus createRobotStatus(Map<String, Object> params) {
        new RobotStatus(/*double energy*/ params.get('energy', 0d)
                /*double x*/, 0D
                /*double y*/, 0D
                /*double bodyHeading*/,  0D
                /*double gunHeading*/, 0D
                /*double radarHeading*/, 0D
                /*double velocity*/, 0D
                /*double bodyTurnRemaining*/, 0D
                /*double radarTurnRemaining*/, 0D
                /*double gunTurnRemaining*/, 0D
                /*double distanceRemaining*/, 0D
                /*double gunHeat*/, 0D
                /*int others*/, 0
                /*int roundNum*/, 0
                /*int numRounds*/, 0
                /*long time*/, params.get('time', 0L))
    }

}