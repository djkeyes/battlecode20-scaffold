package landscaperwaller;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

import static landscaperwaller.RobotPlayer.onTheMap;
import static landscaperwaller.RobotPlayer.rc;

public final class SimplePathfinding {

    public static BehaviorResult badPathFindTo(final MapLocation target) throws GameActionException {
        if (target.equals(rc.getLocation())) {
            return BehaviorResult.FAIL;
        }

        final Direction targetDir = rc.getLocation().directionTo(target);
        for (int offset = 0; offset <= 4; ++offset) {
            final Direction dir = Direction.values()[(targetDir.ordinal() + offset) % 8];
            // FIXME: canMove is supposed to check for flooded tiles, but it doesn't for some reason
            if (!rc.getType().canFly()) {
                final MapLocation next = rc.getLocation().add(dir);
                if (onTheMap(rc, next) && rc.senseFlooding(next)) {
                    continue;
                }
            }
            if (rc.canMove(dir)) {
                rc.move(dir);
                return BehaviorResult.SUCCESS;
            }
            if ((8 - offset) % 8 != offset) {
                final Direction otherDir = Direction.values()[(targetDir.ordinal() + 8 - offset) % 8];
                // FIXME: canMove is supposed to check for flooded tiles, but it doesn't for some reason
                if (!rc.getType().canFly()) {
                    final MapLocation next = rc.getLocation().add(dir);
                    if (onTheMap(rc, next) && rc.senseFlooding(next)) {
                        continue;
                    }
                }
                if (rc.canMove(otherDir)) {
                    rc.move(otherDir);
                    return BehaviorResult.SUCCESS;
                }
            }
        }
        return BehaviorResult.FAIL;
    }
}
