package DroneSwarm;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;

/**
 * Provides utility functions to reason about map symmetry.
 */
public final class MapSymmetry {

    public static final int VERTICAL_SYMMETRY = 0;
    public static final int HORIZONTAL_SYMMETRY = 1;
    public static final int ROTATIONAL_SYMMETRY = 2;

    public static final boolean[] isSymmetryPossible = {true, true, true};

    public static MapLocation[] getAllSymmetricCoords(final RobotController rc, final MapLocation location) {
        return new MapLocation[]{
                getVerticalSymmetricCoords(rc, location),
                getHorizontalSymmetricCoords(rc, location),
                getRotationalSymmetricCoords(rc, location)
        };
    }

    public static MapLocation getSymmetricCoords(final RobotController rc, final MapLocation location,
                                                 final int symmetry) {
        switch (symmetry) {
            case VERTICAL_SYMMETRY:
                return getVerticalSymmetricCoords(rc, location);
            case HORIZONTAL_SYMMETRY:
                return getHorizontalSymmetricCoords(rc, location);
            case ROTATIONAL_SYMMETRY:
                return getRotationalSymmetricCoords(rc, location);
        }
        return getVerticalSymmetricCoords(rc, location);
    }

    public static MapLocation getVerticalSymmetricCoords(final RobotController rc, final MapLocation location) {
        return new MapLocation(rc.getMapWidth() - location.x - 1, location.y);
    }

    public static MapLocation getHorizontalSymmetricCoords(final RobotController rc, final MapLocation location) {
        return new MapLocation(location.x, rc.getMapHeight() - location.y - 1);
    }

    public static MapLocation getRotationalSymmetricCoords(final RobotController rc, final MapLocation location) {
        return new MapLocation(rc.getMapWidth() - location.x - 1, rc.getMapHeight() - location.y - 1);
    }
}
