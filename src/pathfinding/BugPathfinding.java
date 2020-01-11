package pathfinding;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

import static pathfinding.RobotPlayer.rc;
import static pathfinding.SimplePathfinding.badPathFindTo;

public final class BugPathfinding {

    private static MapLocation targetLocation = null;

    public static void setTargetLocation(final MapLocation location) {
        targetLocation = location;
    }

    public static BehaviorResult pathfind() throws GameActionException {
        rc.setIndicatorLine(rc.getLocation(), targetLocation, 255, 0, 0);
        return badPathFindTo(targetLocation);
    }
}
