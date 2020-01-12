package landscaperwaller;

import battlecode.common.*;

import static battlecode.common.Direction.*;
import static landscaperwaller.BugPathfinding.setTargetLocationWithoutReset;
import static landscaperwaller.BugPathfinding.trySetTargetLocation;
import static landscaperwaller.SimplePathfinding.badPathFindTo;

public final strictfp class RobotPlayer {
    static final Direction[] cardinalDirections = Direction.cardinalDirections();
    static final Direction[] octalDirections = {NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST};
    static final Direction[] diagonalDirections = {NORTHEAST, SOUTHEAST, SOUTHWEST, NORTHWEST};
    static final int ADJACENT_DIST_SQ = 2;
    static final int[][] spiralOffsets = {{0, 0}, {0, 1}, {-1, 0}, {-0, -1}, {1, -0}, {1, 1}, {-1, 1}, {-1, -1}, {1, -1}, {0, 2}, {-2, 0}, {-0, -2}, {2, -0}, {1, 2}, {-2, 1}, {-1, -2}, {2, -1}, {2, 2}, {-2, 2}, {-2, -2}, {2, -2}, {0, 3}, {-3, 0}, {-0, -3}, {3, -0}, {1, 3}, {-3, 1}, {-1, -3}, {3, -1}, {2, 3}, {-3, 2}, {-2, -3}, {3, -2}, {0, 4}, {-4, 0}, {-0, -4}, {4, -0}, {1, 4}, {-4, 1}, {-1, -4}, {4, -1}, {3, 3}, {-3, 3}, {-3, -3}, {3, -3}, {2, 4}, {-4, 2}, {-2, -4}, {4, -2}, {0, 5}, {-5, 0}, {-0, -5}, {5, -0}, {3, 4}, {-4, 3}, {-3, -4}, {4, -3}, {1, 5}, {-5, 1}, {-1, -5}, {5, -1}, {2, 5}, {-5, 2}, {-2, -5}, {5, -2}, {4, 4}, {-4, 4}, {-4, -4}, {4, -4}, {3, 5}, {-5, 3}, {-3, -5}, {5, -3}, {0, 6}, {-6, 0}, {-0, -6}, {6, -0}, {1, 6}, {-6, 1}, {-1, -6}, {6, -1}, {2, 6}, {-6, 2}, {-2, -6}, {6, -2}, {4, 5}, {-5, 4}, {-4, -5}, {5, -4}, {3, 6}, {-6, 3}, {-3, -6}, {6, -3}, {0, 7}, {-7, 0}, {-0, -7}, {7, -0}, {1, 7}, {-7, 1}, {-1, -7}, {7, -1}, {5, 5}, {-5, 5}, {-5, -5}, {5, -5}, {4, 6}, {-6, 4}, {-4, -6}, {6, -4}, {2, 7}, {-7, 2}, {-2, -7}, {7, -2}, {3, 7}, {-7, 3}, {-3, -7}, {7, -3}, {5, 6}, {-6, 5}, {-5, -6}, {6, -5}, {0, 8}, {-8, 0}, {-0, -8}, {8, -0}, {1, 8}, {-8, 1}, {-1, -8}, {8, -1}, {4, 7}, {-7, 4}, {-4, -7}, {7, -4}};
    static RobotController rc;
    static int turnCount;

    static MapLocation savedSpawnLoc = null;
    static MapLocation cachedHqLocation = null;
    private static MapLocation randomExplorationDestination = null;
    private static int randomExplorationDestinationStartTurn = 0;
    private static MapLocation cachedSoupLoc = null;
    private static int miners_built = 0;
    private static int lastAttackTargetTurn = 0;

    private static boolean isAggressiveLandscaper = false;
    private static int attackRoundNumInterval;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(final RobotController rc) throws GameActionException {
        RobotPlayer.rc = rc;

        int startRoundNum = rc.getRoundNum();
        turnCount = 0;

        final String enablePrintingProp = System.getProperty("bc.testing.local-testing");
        final boolean enablePrinting = enablePrintingProp != null && enablePrintingProp.equals("true");

        savedSpawnLoc = rc.getLocation();

        try {
            switch (rc.getType()) {
                case HQ:
                    initHQ();
                    break;
                case MINER:
                    initMiner();
                    break;
                case REFINERY:
                    initRefinery();
                    break;
                case VAPORATOR:
                    initVaporator();
                    break;
                case DESIGN_SCHOOL:
                    initDesignSchool();
                    break;
                case FULFILLMENT_CENTER:
                    initFulfillmentCenter();
                    break;
                case LANDSCAPER:
                    initLandscaper();
                    break;
                case DELIVERY_DRONE:
                    initDeliveryDrone();
                    break;
                case NET_GUN:
                    initNetGun();
                    break;
            }

        } catch (final Exception e) {
            if (enablePrinting) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }

        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                switch (rc.getType()) {
                    case HQ:
                        runHQ();
                        break;
                    case MINER:
                        runMiner();
                        break;
                    case REFINERY:
                        runRefinery();
                        break;
                    case VAPORATOR:
                        runVaporator();
                        break;
                    case DESIGN_SCHOOL:
                        runDesignSchool();
                        break;
                    case FULFILLMENT_CENTER:
                        runFulfillmentCenter();
                        break;
                    case LANDSCAPER:
                        runLandscaper();
                        break;
                    case DELIVERY_DRONE:
                        runDeliveryDrone();
                        break;
                    case NET_GUN:
                        runNetGun();
                        break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                final int endRoundNum = rc.getRoundNum();
                if (enablePrinting && endRoundNum != startRoundNum) {
                    System.out.println("Over bytecode limit!");
                    System.out.println("bytecodes used: " + Clock.getBytecodeNum());
                }
                Clock.yield();
                startRoundNum = rc.getRoundNum();

            } catch (final Exception e) {
                if (enablePrinting) {
                    System.out.println(rc.getType() + " Exception");
                    e.printStackTrace();
                }
            }
            turnCount += 1;
        }
    }

    private static void initNetGun() {
    }

    private static void initDeliveryDrone() {
    }

    private static void initLandscaper() {
        isAggressiveLandscaper = rc.getRoundNum() < 100;
        if (isAggressiveLandscaper) {
            // to cover a big map, we need roughly 2 * the side length steps, assuming we manage to mostly go in a
            // straight line. Offset a little, since HQs usually aren't nestled right in the corner.
            attackRoundNumInterval = 2 * (Math.max(rc.getMapWidth(), rc.getMapHeight()) - 9);
            chooseInitialAttackTargetLocation();
        }
    }

    private static void initFulfillmentCenter() {
    }

    private static void initDesignSchool() {
    }

    private static void initVaporator() {
    }

    private static void initRefinery() {
    }

    private static void initMiner() {
    }

    private static void initHQ() {
    }

    static void runHQ() throws GameActionException {

        final RobotInfo[] nearby = rc.senseNearbyRobots(RobotType.MINER.sensorRadiusSquared, rc.getTeam());
        final RobotInfo design = findNearestByType(nearby, RobotType.DESIGN_SCHOOL);
        final RobotInfo[] landscapers = findAllByType(nearby, RobotType.LANDSCAPER);
        if (miners_built == 0 || (miners_built < 2 && design != null) || (miners_built < 4 && design != null && landscapers.length >= 8) || rc.getTeamSoup() > 1.5 * RobotType.VAPORATOR.cost) {
            final BehaviorResult result = trySpawn(RobotType.MINER);
            if (result != BehaviorResult.FAIL) {
                if (result == BehaviorResult.SUCCESS) {
                    miners_built++;
                }
                return;
            }
        }

        final RobotInfo[] targetList = CheckRadar();
        final RobotInfo target = pickTarget(targetList);
        if (target != null) {
            rc.shootUnit(target.getID());
            return;
        }
    }

    static void runMiner() throws GameActionException {
        if (tryClusteredBuild() != BehaviorResult.FAIL) {
            return;
        }

        if (tryMining() != BehaviorResult.FAIL) {
            return;
        }

        // path randomly
        randomlyExplore();

    }

    private static void randomlyExplore() throws GameActionException {
        if (randomExplorationDestination == null || turnCount - randomExplorationDestinationStartTurn > 50 || rc.getLocation().isAdjacentTo(randomExplorationDestination)) {
            final int height = rc.getMapHeight();
            final int width = rc.getMapWidth();
            final int row = (int) (Math.random() * width);
            final int col = (int) (Math.random() * height);

            randomExplorationDestination = new MapLocation(col, row);
            randomExplorationDestinationStartTurn = turnCount;
            BugPathfinding.setTargetLocation(randomExplorationDestination);
        }
        BugPathfinding.pathfind();
    }

    private static BehaviorResult tryMining() throws GameActionException {

        if (rc.getCooldownTurns() >= 1.f) {
            return BehaviorResult.POSTPONED;
        }

        MapLocation targetLoc = null;
        if (rc.getSoupCarrying() < RobotType.MINER.soupLimit) {
            final int curSensorRadiusSq = rc.getCurrentSensorRadiusSquared();
            // this loop has the potential to be egregiously costly
            for (final int[] offset : spiralOffsets) {
                if (offset[0] * offset[0] + offset[1] * offset[1] > curSensorRadiusSq) {
                    break;
                }
                final MapLocation loc = rc.getLocation().translate(offset[0], offset[1]);
                if (!onTheMap(rc, loc)) {
                    continue;
                }
                final int soup = rc.senseSoup(loc);
                if (soup > 0) {
                    targetLoc = loc;
                    break;
                }
            }

            // cache the last successful soup observation, in case it's far away
            if (targetLoc != null) {
                cachedSoupLoc = targetLoc;
            } else if (cachedSoupLoc != null) {
                if (rc.getLocation().distanceSquaredTo(cachedSoupLoc) <= curSensorRadiusSq) {
                    // no longer available
                    cachedSoupLoc = null;
                } else {
                    targetLoc = cachedSoupLoc;
                }
            }
        }

        if (targetLoc != null) {
            // path to soup
            if (targetLoc.distanceSquaredTo(rc.getLocation()) <= ADJACENT_DIST_SQ) {
                rc.mineSoup(rc.getLocation().directionTo(targetLoc));
                return BehaviorResult.SUCCESS;
            } else {
                setTargetLocationWithoutReset(targetLoc);
                return BugPathfinding.pathfind();
            }
        } else {
            if (rc.getSoupCarrying() > 0) {
                final RobotInfo[] nearby = rc.senseNearbyRobots(RobotType.MINER.sensorRadiusSquared, rc.getTeam());
                final RobotInfo nearestRefinery = findNearestByType(nearby, RobotType.REFINERY);
                final RobotInfo nearestHq = findNearestByType(nearby, RobotType.HQ);
                final RobotInfo nearestDropOff;
                if (nearestRefinery == null) {
                    nearestDropOff = nearestHq;
                } else if (nearestHq == null) {
                    nearestDropOff = nearestRefinery;
                } else {
                    if (rc.getLocation().distanceSquaredTo(nearestRefinery.location) < rc.getLocation().distanceSquaredTo(nearestHq.location)) {
                        nearestDropOff = nearestRefinery;
                    } else {
                        nearestDropOff = nearestHq;
                    }
                }

                final MapLocation depositLoc;
                if (nearestDropOff != null) {
                    if (nearestDropOff.location.distanceSquaredTo(rc.getLocation()) <= ADJACENT_DIST_SQ) {
                        rc.depositSoup(rc.getLocation().directionTo(nearestDropOff.location), rc.getSoupCarrying());
                        return BehaviorResult.SUCCESS;
                    } else {
                        depositLoc = nearestDropOff.location;
                    }
                } else {
                    depositLoc = savedSpawnLoc;
                }

                // path back home

                trySetTargetLocation(depositLoc);
                return BugPathfinding.pathfind();
            }
        }
        return BehaviorResult.FAIL;
    }

    static boolean onTheMap(final RobotController rc, final MapLocation loc) {
        // FIXME: this is a quick fix, until the function rc.onTheMap is fixed in the engine.
        return loc.x >= 0 && loc.y >= 0 && loc.x < rc.getMapWidth() && loc.y < rc.getMapHeight();
    }

    private static BehaviorResult tryClusteredBuild() throws GameActionException {
        final RobotInfo[] nearby = rc.senseNearbyRobots(RobotType.MINER.sensorRadiusSquared, rc.getTeam());
        // 1. find nearby HQ
        final RobotInfo nearestHq = findNearestByType(nearby, RobotType.HQ);
        // no HQ nearby? it must not be important
        if (nearestHq == null || nearestHq.location.distanceSquaredTo(rc.getLocation()) >= 25) {
            return BehaviorResult.FAIL;
        }

        // 2. find nearby design school
        final RobotInfo nearestDesign = findNearestByType(nearby, RobotType.DESIGN_SCHOOL);

        if (nearestDesign == null) {
            return pathToAndDoClusteredBuild(nearestHq.location, RobotType.DESIGN_SCHOOL);
        }

        // 3. find nearby net guns and vaporators
        // TODO: vaporators are super expensive (but pay for themselves in 150 turns). not worth it until we figure
        // out mining.
        final RobotInfo[] netGuns = findAllByType(nearby, RobotType.NET_GUN);
        if (netGuns.length < 4) {
            return pathToAndDoClusteredBuild(nearestHq.location, RobotType.NET_GUN);
        }
//        final RobotInfo[] netGuns = findAllByType(nearby, RobotType.NET_GUN);
//        final RobotInfo[] vaporators = findAllByType(nearby, RobotType.VAPORATOR);
//        if (netGuns.length < 4 || vaporators.length < 2) {
//            if (vaporators.length <= netGuns.length) {
//                BehaviorResult result = pathToAndDoClusteredBuild(nearestHq.location, RobotType.VAPORATOR);
//                if (result == BehaviorResult.FAIL) {
//                    // maybe obstacles in the way
//                    return pathToAndDoClusteredBuild(nearestHq.location, RobotType.NET_GUN);
//                } else {
//                    return result;
//                }
//            } else {
//                BehaviorResult result = pathToAndDoClusteredBuild(nearestHq.location, RobotType.NET_GUN);
//                if (result == BehaviorResult.FAIL) {
//                    // maybe obstacles in the way
//                    return pathToAndDoClusteredBuild(nearestHq.location, RobotType.VAPORATOR);
//                } else {
//                    return result;
//                }
//            }
//        }

        return BehaviorResult.FAIL;
    }

    private static BehaviorResult pathToAndDoClusteredBuild(final MapLocation hqLocation, final RobotType type) throws GameActionException {
        if (rc.getCooldownTurns() >= 1.f) {
            return BehaviorResult.POSTPONED;
        }

        // 1. pick the closest location
        MapLocation closestLoc = null;
        int closestDistSq = 100;
        final Direction[] candidateDirs;
        if (type == RobotType.DESIGN_SCHOOL) {
            candidateDirs = new Direction[]{WEST}; //cardinalDirections;
        } else if (type == RobotType.VAPORATOR) {
            candidateDirs = new Direction[]{NORTH}; //cardinalDirections;
        } else { // type == RobotType.NET_GUN
            candidateDirs = new Direction[]{NORTHWEST}; //diagonalDirections;
        }

        for (final Direction d : candidateDirs) {
            final MapLocation loc = hqLocation.add(d);
            if (rc.canSenseLocation(loc) && !rc.isLocationOccupied(loc)) {
                final int distSq = rc.getLocation().distanceSquaredTo(loc);
                if (distSq < closestDistSq) {
                    closestDistSq = distSq;
                    closestLoc = loc;
                }
            }
        }

        if (closestLoc == null) {
            // all occupied; nothing else to do
            return BehaviorResult.FAIL;
        }

        final int soup = rc.getTeamSoup();

        // don't bother if it's too expensive
        if (type.cost - soup >= 30) {
            return BehaviorResult.FAIL;
        }

        // try build
        if (closestLoc.isAdjacentTo(rc.getLocation())) {
            // no need to check canBuildRobot. we've already checked the other preconditions.
            if (soup >= type.cost) {
                final Direction dir = rc.getLocation().directionTo(closestLoc);
                rc.buildRobot(type, dir);
                return BehaviorResult.SUCCESS;
            } else {
                // might as well wait
                return BehaviorResult.POSTPONED;
            }
        }
        // otherwise, path there
        return badPathFindTo(closestLoc);
    }

    private static RobotInfo findNearestByType(final RobotInfo[] nearby, final RobotType type) {
        for (final RobotInfo r : nearby) {
            if (r.type == type) {
                return r;
            }
        }
        return null;
    }

    private static RobotInfo[] findAllByType(final RobotInfo[] nearby, final RobotType type) {
        int count = 0;
        for (final RobotInfo r : nearby) {
            if (r.type == type) {
                ++count;
            }
        }
        final RobotInfo[] matches = new RobotInfo[count];
        int idx = 0;
        for (final RobotInfo r : nearby) {
            if (r.type == type) {
                matches[idx++] = r;
            }
        }
        return matches;
    }

    static void runRefinery() throws GameActionException {

    }

    static void runVaporator() throws GameActionException {

    }

    static void runDesignSchool() throws GameActionException {
        final RobotInfo[] nearby = rc.senseNearbyRobots(RobotType.MINER.sensorRadiusSquared, rc.getTeam());
        final RobotInfo[] landscapers = findAllByType(nearby, RobotType.LANDSCAPER);
        if (landscapers.length < 8) {
            trySpawn(RobotType.LANDSCAPER);
        }
    }

    static void runFulfillmentCenter() throws GameActionException {

    }

    static void runLandscaper() throws GameActionException {
        final RobotInfo[] nearby = rc.senseNearbyRobots(RobotType.MINER.sensorRadiusSquared, rc.getTeam());
        if (cachedHqLocation == null) {
            final RobotInfo hq = findNearestByType(nearby, RobotType.HQ);
            if (hq == null) {
                // can't do much else
                randomlyExplore();
                return;
            } else {
                cachedHqLocation = hq.location;
            }
        }

        if (rc.getCooldownTurns() >= 1.f) {
            return;
        }

        if (isAggressiveLandscaper) {
            if (attackEnemyHq() != BehaviorResult.FAIL) {
                return;
            }
            chooseAttackTargetLocation();

            BugPathfinding.pathfind();
        } else {
            // want to form a wall of size 5x5, with evenly spaced landscapers
            // fill from one edge to the other
            final int[][] offsets = {{2, 0}, {2, 2}, {2, -2}, {0, 2}, {0, -2}, {-2, 2}, {-2, -2}, {-2, 0}};
            final int sensorRange = rc.getCurrentSensorRadiusSquared();
            MapLocation last_unoccupied_location = cachedHqLocation.translate(offsets[offsets.length - 1][0],
                    offsets[offsets.length - 1][1]);
            for (final int[] offset : offsets) {
                final MapLocation target = cachedHqLocation.translate(offset[0], offset[1]);
                if (target.distanceSquaredTo(rc.getLocation()) <= sensorRange) {
                    if (!rc.isLocationOccupied(target) || rc.getLocation().equals(target)) {
                        last_unoccupied_location = target;
                        break;
                    }
                }
            }
            if (rc.getLocation().equals(last_unoccupied_location)) {
                // already there. start shoveling
                if (rc.getDirtCarrying() > 0) {
                    Direction lowestWall = null;
                    int minWallHeight = 0;
                    for (final Direction d : allDirections()) {
                        final MapLocation tile = rc.getLocation().add(d);
                        final int dx = Math.abs(tile.x - cachedHqLocation.x);
                        final int dy = Math.abs(tile.y - cachedHqLocation.y);
                        final boolean isWall = (dx == 2 || dy == 2) && (dx <= 2 && dy <= 2);
                        if (!isWall) {
                            continue;
                        }
                        if (!rc.canDepositDirt(d)) {
                            continue;
                        }
                        final int elevation = rc.senseElevation(tile);
                        if (lowestWall == null || elevation < minWallHeight) {
                            minWallHeight = elevation;
                            lowestWall = d;
                        }
                    }
                    if (lowestWall != null) {
                        rc.depositDirt(lowestWall);
                    }
                } else {
                    Direction highestTrough = null;
                    int maxTroughHeight = 0;
                    for (final Direction d : allDirections()) {
                        final MapLocation tile = rc.getLocation().add(d);
                        final int dx = Math.abs(tile.x - cachedHqLocation.x);
                        final int dy = Math.abs(tile.y - cachedHqLocation.y);
                        final boolean isTrough = (dx >= 3 || dy >= 3);
                        if (!isTrough) {
                            continue;
                        }
                        final int elevation = rc.senseElevation(tile);
                        if (!rc.canDigDirt(d)) {
                            continue;
                        }
                        if (highestTrough == null || elevation > maxTroughHeight) {
                            maxTroughHeight = elevation;
                            highestTrough = d;
                        }
                    }
                    if (highestTrough != null) {
                        rc.digDirt(highestTrough);
                    }
                }
            } else {
                badPathFindTo(last_unoccupied_location);
            }
        }
    }

    private static BehaviorResult attackEnemyHq() throws GameActionException {
        // TODO: detect HQ in other ways, e.g. via blockchain
        if (cachedHqLocation == null) {
            return BehaviorResult.FAIL;
        }
        MapLocation detectedHqLoc = null;
        for (int i = 0; i < 3; ++i) {
            if (MapSymmetry.isSymmetryPossible[i]) {
                final MapLocation possibleLocation = MapSymmetry.getSymmetricCoords(rc, cachedHqLocation, i);
                final int distSq = rc.getLocation().distanceSquaredTo(possibleLocation);
                if (rc.getCurrentSensorRadiusSquared() >= distSq) {
                    final RobotInfo robot = rc.senseRobotAtLocation(possibleLocation);
                    if (robot != null && robot.type == RobotType.HQ) {
                        detectedHqLoc = robot.location;
                        break;
                    }
                }
            }
        }
        if (detectedHqLoc == null) {
            return BehaviorResult.FAIL;
        }

        final int distSq = rc.getLocation().distanceSquaredTo(detectedHqLoc);
        if (distSq >= 9) {
            // TODO: if we add an aggressive pathfinding function that digs holes, add it here
            BugPathfinding.trySetTargetLocation(detectedHqLoc);
            return BugPathfinding.pathfind();
        }

        final RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(2, rc.getTeam().opponent());
        int weakestBuildingHealth = 100;
        // is it better to target a weak building, or the HQ?
        // is it possible to sense dirt on a building (other than tracking it)? seems like no.
        RobotInfo weakestBuilding = null;
        for (final RobotInfo enemy : nearbyEnemies) {
            if (enemy.type.isBuilding()) {
                if (enemy.type.dirtLimit < weakestBuildingHealth) {
                    weakestBuilding = enemy;
                    weakestBuildingHealth = enemy.type.dirtLimit;
                }
            }
        }
        if (weakestBuilding != null) {
            // attack this building
            if (rc.getDirtCarrying() > 0) {
                rc.depositDirt(rc.getLocation().directionTo(weakestBuilding.location));
                return BehaviorResult.SUCCESS;
            }
        }

        // can we move next to the HQ?
        // (if we're already there, figure out where to possibly dig, and if we're not, figure out where to possible
        // drop off)
        Direction lowestDirAdjacentToHq = CENTER;
        int lowestElevation = Integer.MAX_VALUE;
        Direction dirFurthestFromHq = null;
        int furthestDistFromHq = 0;
        for (final Direction d : octalDirections) {
            final MapLocation next = rc.getLocation().add(d);
            final boolean canDig = rc.canDigDirt(d);
            if (!rc.getLocation().isAdjacentTo(detectedHqLoc)) {
                if (next.isAdjacentTo(detectedHqLoc)) {
                    if (rc.canMove(d)) {
                        rc.move(d);
                        return BehaviorResult.SUCCESS;
                    }
                    if (canDig) {
                        final int elevation = rc.senseElevation(next);
                        if (elevation < lowestElevation) {
                            lowestElevation = elevation;
                            lowestDirAdjacentToHq = d;
                        }
                    }
                }
            }
            if (canDig) {
                final int distSqToHq = next.distanceSquaredTo(detectedHqLoc);
                if (distSqToHq > furthestDistFromHq) {
                    furthestDistFromHq = distSqToHq;
                    dirFurthestFromHq = d;
                }
            }
        }

        if (rc.getDirtCarrying() < rc.getType().dirtLimit) {
            // dig a tile near the hq
            rc.digDirt(lowestDirAdjacentToHq);
            return BehaviorResult.SUCCESS;
        } else if (dirFurthestFromHq != null) {
            // we're full, and there are no buildings to destroy
            // deposit the dirt in a direction far from the hq
            rc.depositDirt(dirFurthestFromHq);
            return BehaviorResult.SUCCESS;
        }

        return BehaviorResult.POSTPONED;
    }

    private static void chooseInitialAttackTargetLocation() {
        // initially, just aim for center of map as a staging ground
        BugPathfinding.setTargetLocation(new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2));
        lastAttackTargetTurn = rc.getRoundNum() / 100 * 100;
    }

    private static void chooseAttackTargetLocation() throws GameActionException {
        if (rc.getRoundNum() - lastAttackTargetTurn >= 100) {
            final int symmetryAssumption = (rc.getRoundNum() / 100) % 3;
            MapLocation target = null;
            if (MapSymmetry.isSymmetryPossible[symmetryAssumption]) {
                target = MapSymmetry.getSymmetricCoords(rc, cachedHqLocation, symmetryAssumption);
                if (rc.getLocation().distanceSquaredTo(target) <= rc.getCurrentSensorRadiusSquared()) {
                    final RobotInfo robot = rc.senseRobotAtLocation(target);
                    if (robot == null || robot.type != RobotType.HQ) {
                        MapSymmetry.isSymmetryPossible[symmetryAssumption] = false;
                    }
                }
            }
            if (!MapSymmetry.isSymmetryPossible[symmetryAssumption]) {
                target = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);
            }
            BugPathfinding.setTargetLocation(target);
            lastAttackTargetTurn = rc.getRoundNum() / 100 * 100;
        }
    }

    static void runDeliveryDrone() throws GameActionException {

    }

    // Return all enemy unit in vision
    static RobotInfo[] CheckRadar() {
        return rc.senseNearbyRobots(-1, rc.getTeam().opponent());
    }

    // Pick the closest shootable unit
    static RobotInfo pickTarget(final RobotInfo[] targetList) {
        RobotInfo target = null;
        int minDis = Integer.MAX_VALUE;
        for (final RobotInfo tg : targetList) {
            if (minDis > rc.getLocation().distanceSquaredTo(tg.getLocation())) {
                if (rc.canShootUnit(tg.getID())) {
                    target = tg;
                    minDis = rc.getLocation().distanceSquaredTo(tg.getLocation());
                }
            }
        }
        return target;

    }

    static void runNetGun() throws GameActionException {
        final RobotInfo[] targetList = CheckRadar();
        final RobotInfo target = pickTarget(targetList);
        if (target != null) {
            rc.shootUnit(target.getID());
        }
    }

    static BehaviorResult trySpawn(final RobotType type) throws GameActionException {
        for (final Direction d : octalDirections) {
            if (rc.canBuildRobot(type, d)) {
                rc.buildRobot(type, d);
                return BehaviorResult.SUCCESS;
            }
        }
        return BehaviorResult.FAIL;
    }

}
