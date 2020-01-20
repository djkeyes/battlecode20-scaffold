package landscaperwaller;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

import static landscaperwaller.RobotPlayer.rc;

public final class SimplePathfinding {

    static boolean canMoveWithFlooding(final Direction dir, final int sensorRadiusSq) throws GameActionException {
        if (rc.getType().canFly()) {
            return rc.canMove(dir);
        }
        final MapLocation cur = rc.getLocation();
        final MapLocation next = cur.add(dir);
        if (next.distanceSquaredTo(cur) > sensorRadiusSq) {
            // cautious estimate. may want to override this in some situations.
            return false;
        }
        if (!rc.onTheMap(next)) {
            return false;
        }
        if (rc.senseFlooding(next)) {
            return false;
        }
        return rc.canMove(dir);
    }

    public static BehaviorResult badPathFindTo(final MapLocation target) throws GameActionException {
        if (target.equals(rc.getLocation())) {
            return BehaviorResult.FAIL;
        }

        final Direction targetDir = rc.getLocation().directionTo(target);
        final int sensorRadius = rc.getCurrentSensorRadiusSquared();
        for (int offset = 0; offset <= 4; ++offset) {
            final Direction dir = Direction.values()[(targetDir.ordinal() + offset) % 8];
            if (canMoveWithFlooding(dir, sensorRadius)) {
                rc.move(dir);
                return BehaviorResult.SUCCESS;
            }
            if ((8 - offset) % 8 != offset) {
                final Direction otherDir = Direction.values()[(targetDir.ordinal() + 8 - offset) % 8];
                if (canMoveWithFlooding(otherDir, sensorRadius)) {
                    rc.move(otherDir);
                    return BehaviorResult.SUCCESS;
                }
            }
        }
        return BehaviorResult.FAIL;
    }
}
