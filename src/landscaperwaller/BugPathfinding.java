package landscaperwaller;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

import static landscaperwaller.RobotPlayer.rc;
import static landscaperwaller.SimplePathfinding.canMoveWithFlooding;

/**
 * Bug pathfinding. This code is partly based on the duck's 2016 submission.
 * https://github.com/TheDuck314/battlecode2016/blob/master/src/final_25/Nav.java
 */
public final class BugPathfinding {

    private static final BooleanGrid bugVisitedLocations = new BooleanGrid();
    private static MapLocation currentLoc = null;
    private static MapLocation targetLocation = null;
    private static boolean bugTracing = false;
    private static MapLocation bugLastWall = null;
    private static int closestDistWhileBugging = Integer.MAX_VALUE;
    private static int bugNumTurnsWithNoWall = 0;
    private static boolean bugWallOnLeft = true; // whether the wall is on our left or our right

    private static int latestSensorRadius = 0;

    /**
     * Sets a target location for bug pathfinding
     */
    public static void setTargetLocation(final MapLocation location) {
        targetLocation = location;
        bugTracing = false;
    }

    /**
     * Sets a target location without resetting the state. Only use this if the new location is close in distance to
     * the old location.
     */
    public static void setTargetLocationWithoutReset(final MapLocation location) {
        targetLocation = location;
    }

    /**
     * Sets a target location for bug pathfinding, but does not reset the state if it's the same as the last position
     */
    public static void trySetTargetLocation(final MapLocation location) {
        if (targetLocation != location) {
            setTargetLocation(location);
        }
    }

    public static BehaviorResult pathfind() throws GameActionException {
        return goToBug();
    }

    public static BehaviorResult tryMoveInDirection(final Direction dir) throws GameActionException {
        if (canMoveWithFlooding(dir, latestSensorRadius)) {
            rc.move(dir);
            return BehaviorResult.SUCCESS;
        }
        final Direction left = dir.rotateLeft();
        if (canMoveWithFlooding(left, latestSensorRadius)) {
            rc.move(left);
            return BehaviorResult.SUCCESS;
        }
        final Direction right = dir.rotateRight();
        if (canMoveWithFlooding(right, latestSensorRadius)) {
            rc.move(right);
            return BehaviorResult.SUCCESS;
        }
        return BehaviorResult.FAIL;
    }

    public static BehaviorResult goToBug() throws GameActionException {
        latestSensorRadius = rc.getCurrentSensorRadiusSquared();
        currentLoc = rc.getLocation();
        // TODO: should this be a precondition for this function? might save bytecodes.
        if (currentLoc.equals(targetLocation)) {
            return BehaviorResult.FAIL;
        }
        // TODO: should this be a precondition for this function? might save bytecodes
        if (rc.getCooldownTurns() >= 1.f) {
            return BehaviorResult.SUCCESS;
        }

        if (!bugTracing) {
            // try to go direct; start bugging on failure
            final Direction destDir = currentLoc.directionTo(targetLocation);
            final BehaviorResult result = tryMoveInDirection(destDir);
            if (result != BehaviorResult.FAIL) {
                return result;
            } else {
                bugStartTracing();
            }
        } else {
            // try to stop bugging
            if (currentLoc.distanceSquaredTo(targetLocation) < closestDistWhileBugging) {
                final BehaviorResult result = tryMoveInDirection(currentLoc.directionTo(targetLocation));
                if (result != BehaviorResult.FAIL) {
                    bugTracing = false;
                    return result;
                }
            }
        }
        final BehaviorResult result = bugTraceMove(false);

        if (bugNumTurnsWithNoWall >= 2) {
            bugTracing = false;
        }
        return result;
    }

    public static void bugReset() {
        bugTracing = false;
    }

    private static void bugStartTracing() throws GameActionException {
        bugTracing = true;
        bugVisitedLocations.reset();

        closestDistWhileBugging = currentLoc.distanceSquaredTo(targetLocation);
        bugNumTurnsWithNoWall = 0;

        final Direction dirToDest = currentLoc.directionTo(targetLocation);
        Direction leftDir = dirToDest;
        int leftDistSq = Integer.MAX_VALUE;
        for (int i = 0; i < 8; ++i) {
            leftDir = leftDir.rotateLeft();
            if (canMoveWithFlooding(leftDir, latestSensorRadius)) {
                leftDistSq = currentLoc.add(leftDir).distanceSquaredTo(targetLocation);
                break;
            }
        }
        Direction rightDir = dirToDest;
        int rightDistSq = Integer.MAX_VALUE;
        for (int i = 0; i < 8; ++i) {
            rightDir = rightDir.rotateRight();
            if (canMoveWithFlooding(rightDir, latestSensorRadius)) {
                rightDistSq = currentLoc.add(rightDir).distanceSquaredTo(targetLocation);
                break;
            }
        }
        if (rightDistSq < leftDistSq) {
            bugWallOnLeft = true;
            bugLastWall = currentLoc.add(rightDir.rotateLeft());
        } else {
            bugWallOnLeft = false;
            bugLastWall = currentLoc.add(leftDir.rotateRight());
        }
    }

    private static BehaviorResult bugTraceMove(final boolean recursed) throws GameActionException {
        Direction tryDir = currentLoc.directionTo(bugLastWall);
        bugVisitedLocations.setTrue(currentLoc.x, currentLoc.y);
        if (canMoveWithFlooding(tryDir, latestSensorRadius)) {
            bugNumTurnsWithNoWall += 1;
        } else {
            bugNumTurnsWithNoWall = 0;
        }
        for (int i = 0; i < 8; ++i) {
            if (bugWallOnLeft) {
                tryDir = tryDir.rotateRight();
            } else {
                tryDir = tryDir.rotateLeft();
            }
            final MapLocation dirLoc = currentLoc.add(tryDir);
            if (!rc.onTheMap(dirLoc) && !recursed) {
                // if we hit the edge of the map, reverse direction and recurse
                bugWallOnLeft = !bugWallOnLeft;
                return bugTraceMove(true);
            }
            if (canMoveWithFlooding(tryDir, latestSensorRadius)) {
                rc.move(tryDir);
                currentLoc = rc.getLocation(); // we just moved
                if (bugVisitedLocations.get(currentLoc.x, currentLoc.y)) {
                    bugTracing = false;
                }
                return BehaviorResult.SUCCESS;
            } else {
                bugLastWall = currentLoc.add(tryDir);
            }
        }
        return BehaviorResult.FAIL;
    }

}
