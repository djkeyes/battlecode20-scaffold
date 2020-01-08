package landscaperwaller;

import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;

import static landscaperwaller.RobotPlayer.rc;

public class MapUtils {

    // Note: This code is copied from daniel's 2016 battlecode repo
    // In bc17, map locations had a (fixed) random number added to the x and y coordinates--that way, you couldn't
    // tell your absolute location, only your relative location. It has been hinted that this will happen in bc20
    // (otherwise teams further away from the origin get an unfair hint about the map size), so I'm leaving this
    // over-general code here. -daniel

    public static Integer minRow = 0, maxRow, minCol = 0, maxCol;
    public static Integer mapWidth, mapHeight;
    public static int maxHorizontalSight;

    public static void initMapEdges() {
        maxHorizontalSight = Util.sqrt(rc.getType().sensorRadiusSquared);
    }

    public static void checkMapEdges() throws GameActionException {
        // for each of these, start at the furthest visible loc and linear
        // search inward.
        // idt binary search would be any fast for such small sight ranges
        // TODO: we can speed this up by storing our last location, and only checking newly visible tiles
        MapLocation curLoc = rc.getLocation();
        if (minRow == null) {
            if (!rc.onTheMap(curLoc.translate(0, -maxHorizontalSight))) {
                for (int r = -maxHorizontalSight + 1; r <= 0; r++) {
                    if (rc.onTheMap(curLoc.translate(0, r))) {
                        minRow = curLoc.y + r;
                        break;
                    }
                }
            }
        }

        if (minCol == null) {
            if (!rc.onTheMap(curLoc.translate(-maxHorizontalSight, 0))) {
                for (int c = -maxHorizontalSight + 1; c <= 0; c++) {
                    if (rc.onTheMap(curLoc.translate(c, 0))) {
                        minCol = curLoc.x + c;
                        break;
                    }
                }
            }
        }

        if (maxRow == null) {
            if (!rc.onTheMap(curLoc.translate(0, maxHorizontalSight))) {
                for (int r = maxHorizontalSight - 1; r >= 0; r--) {
                    if (rc.onTheMap(curLoc.translate(0, r))) {
                        maxRow = curLoc.y + r;
                        break;
                    }
                }
            }
        }

        if (maxCol == null) {
            if (!rc.onTheMap(curLoc.translate(maxHorizontalSight, 0))) {
                for (int c = maxHorizontalSight - 1; c >= 0; c--) {
                    if (rc.onTheMap(curLoc.translate(c, 0))) {
                        maxCol = curLoc.x + c;
                        break;
                    }
                }
            }
        }

        if (mapHeight == null) {
            if ((minRow != null && maxRow != null)) {
                mapHeight = maxRow - minRow + 1;
            }
        }
        if (mapWidth == null) {
            if (maxCol != null && minCol != null) {
                mapWidth = maxCol - minCol + 1;
            }
        }

//        // these 4 cases are also possible, if partial information was broadcast
//        // to this robot for some reason
//        if (mapHeight != null) {
//            if (minRow == null && maxRow != null) {
//                minRow = maxRow - mapHeight + 1;
//            } else if (minRow != null && maxRow == null) {
//                maxRow = minRow + mapHeight - 1;
//            }
//        }
//        if (mapWidth != null) {
//            if (minCol == null && maxCol != null) {
//                minCol = maxCol - mapWidth + 1;
//            } else if (minCol != null && maxCol == null) {
//                maxCol = minCol + mapWidth - 1;
//            }
//        }
    }

    public static MapLocation clampWithKnownBounds(MapLocation loc) {
        if (minCol != null) {
            loc = new MapLocation(Math.max(loc.x, minCol), loc.y);
        }
        if (maxCol != null) {
            loc = new MapLocation(Math.min(loc.x, maxCol), loc.y);
        }
        if (minRow != null) {
            loc = new MapLocation(loc.x, Math.max(loc.y, minRow));
        }
        if (maxRow != null) {
            loc = new MapLocation(loc.x, Math.min(loc.y, maxRow));
        }
        return loc;
    }
}
