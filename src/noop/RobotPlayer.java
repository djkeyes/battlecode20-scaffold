package noop;

import battlecode.common.Clock;
import battlecode.common.RobotController;

public strictfp class RobotPlayer {

    @SuppressWarnings("unused")
    public static void run(RobotController rc) {
        //noinspection InfiniteLoopStatement
        while (true) Clock.yield();
    }

}
