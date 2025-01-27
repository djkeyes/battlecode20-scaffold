package addingComm;

import java.util.ArrayList;

import battlecode.common.*;

import static battlecode.common.Direction.*;

public strictfp class RobotPlayer 
{

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
    private static CommSys Com;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        RobotPlayer.rc = rc;
        Com=new CommSys(rc);
        turnCount = 0;

        savedSpawnLoc = rc.getLocation();

        while (true) {
            // Read the new every round
            Com.ReadNews();
            // Increase counter
            turnCount += 1;
            try {
                switch (rc.getType()) {
                    case HQ:
                        runHQ();
                        // TestFunctions.testBlockChain(rc);
                        break;
                    case MINER:
                        // runMiner();
                        runMiner1();
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
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

    static void runHQ() throws GameActionException {
        RobotInfo[] nearby = rc.senseNearbyRobots(RobotType.MINER.sensorRadiusSquared, rc.getTeam());
        RobotInfo[] miners = findAllByType(nearby, RobotType.MINER);
        RobotInfo design = findNearestByType(nearby, RobotType.DESIGN_SCHOOL);
        RobotInfo[] landscapers = findAllByType(nearby, RobotType.LANDSCAPER);
        if ((rc.getRoundNum()<15 && miners_built <3)  || (miners_built < 2 && design != null) || (miners_built < 4 && design != null && landscapers.length >= 8) || rc.getTeamSoup() > 1.5 * RobotType.VAPORATOR.cost) {
            BehaviorResult result = trySpawn(RobotType.MINER);
            if (result != BehaviorResult.FAIL) {
                if (result == BehaviorResult.SUCCESS) {
                    miners_built++;
                }
                return;
            }
        }
    }
    private static void lookAround()
    {
        RobotInfo[] around=rc.senseNearbyRobots(-1);
        for(RobotInfo rb:around)
        {
            // If it is building, broadast
            if(rb.getType().isBuilding())
            {
                Com.broadcastUnitLocs(rb);
            }
        }

    }

    static void runMiner1() throws GameActionException
    {
        // Identify things around
        lookAround();
        //
        if (tryMining() != BehaviorResult.FAIL) {
            return;
        }


        randomlyExplore();
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
            int row = (int) (Math.random() * width);
            int col = (int) (Math.random() * height);

            randomExplorationDestination = new MapLocation(col, row);
            randomExplorationDestinationStartTurn = turnCount;
        }
        badPathFindTo(randomExplorationDestination);
    }

    private static int getCurrentSensorRadiusSquared() throws GameActionException {
        return (int) Math.round(rc.getType().sensorRadiusSquared * GameConstants.getSensorRadiusPollutionCoefficient(rc.sensePollution(rc.getLocation())));
    }

    private static BehaviorResult tryMining() throws GameActionException {

        if (rc.getCooldownTurns() >= 1.f) {
            return BehaviorResult.POSTPONED;
        }

        MapLocation targetLoc = null;
        if (rc.getSoupCarrying() < RobotType.MINER.soupLimit) {
            int curSensorRadiusSq = getCurrentSensorRadiusSquared();
            // this loop has the potential to be egregiously costly
            for (final int[] offset : spiralOffsets) {
                if (offset[0] * offset[0] + offset[1] * offset[1] > curSensorRadiusSq) {
                    break;
                }
                MapLocation loc = rc.getLocation().translate(offset[0], offset[1]);
                int soup = rc.senseSoup(loc);
                if (soup > 0) {
                    targetLoc = loc;
                    Com.broadcastLocs(CommSys.NEWS_SOUP_FOUND,loc);
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
                return badPathFindTo(targetLoc);
            }
        } else {
            if (rc.getSoupCarrying() > 0) {
                RobotInfo[] nearby = rc.senseNearbyRobots(RobotType.MINER.sensorRadiusSquared, rc.getTeam());
                RobotInfo nearestRefinery = findNearestByType(nearby, RobotType.REFINERY);
                RobotInfo nearestHq = findNearestByType(nearby, RobotType.HQ);
                RobotInfo nearestDropOff = null;
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

                MapLocation depositLoc = null;
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
                return badPathFindTo(depositLoc);
            }
        }
        return BehaviorResult.FAIL;
    }

    private static BehaviorResult tryClusteredBuild() throws GameActionException {
        RobotInfo[] nearby = rc.senseNearbyRobots(RobotType.MINER.sensorRadiusSquared, rc.getTeam());
        // 1. find nearby HQ
        RobotInfo nearestHq = findNearestByType(nearby, RobotType.HQ);
        // no HQ nearby? it must not be important
        if (nearestHq == null || nearestHq.location.distanceSquaredTo(rc.getLocation()) >= 25) {
            return BehaviorResult.FAIL;
        }

        // 2. find nearby design school
        RobotInfo nearestDesign = findNearestByType(nearby, RobotType.DESIGN_SCHOOL);

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

    private static BehaviorResult pathToAndDoClusteredBuild(MapLocation hqLocation, RobotType type) throws GameActionException {
        if (rc.getCooldownTurns() >= 1.f) {
            return BehaviorResult.POSTPONED;
        }

        // 1. pick the closest location
        MapLocation closestLoc = null;
        int closestDistSq = 100;
        Direction[] candidateDirs;
        if (type == RobotType.DESIGN_SCHOOL) {
            candidateDirs = new Direction[]{WEST}; //cardinalDirections;
        } else if (type == RobotType.VAPORATOR) {
            candidateDirs = new Direction[]{NORTH}; //cardinalDirections;
        } else { // type == RobotType.NET_GUN
            candidateDirs = new Direction[]{NORTHWEST}; //diagonalDirections;
        }

        for (Direction d : candidateDirs) {
            MapLocation loc = hqLocation.add(d);
            if (!rc.isLocationOccupied(loc)) {
                int distSq = rc.getLocation().distanceSquaredTo(loc);
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
                Direction dir = rc.getLocation().directionTo(closestLoc);
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

    private static BehaviorResult badPathFindTo(final MapLocation target) throws GameActionException {
        if (target.equals(rc.getLocation())) {
            return BehaviorResult.FAIL;
        }

        Direction targetDir = rc.getLocation().directionTo(target);
        for (int offset = 0; offset <= 4; ++offset) {
            final Direction dir = Direction.values()[(targetDir.ordinal() + offset) % 8];
            // FIXME: canMove is supposed to check for flooded tiles, but it doesn't for some reason
            if (!rc.getType().canFly() && rc.senseFlooding(rc.getLocation().add(dir))) {
                continue;
            }
            if (rc.canMove(dir)) {
                rc.move(dir);
                return BehaviorResult.SUCCESS;
            }
            if ((8 - offset) % 8 != offset) {
                final Direction otherDir = Direction.values()[(targetDir.ordinal() + 8 - offset) % 8];
                // FIXME: canMove is supposed to check for flooded tiles, but it doesn't for some reason
                if (!rc.getType().canFly() && rc.senseFlooding(rc.getLocation().add(dir))) {
                    continue;
                }
                if (rc.canMove(otherDir)) {
                    rc.move(otherDir);
                    return BehaviorResult.SUCCESS;
                }
            }
        }
        return BehaviorResult.FAIL;
    }

    private static RobotInfo findNearestByType(final RobotInfo[] nearby, final RobotType type) {
        for (RobotInfo r : nearby) {
            if (r.type == type) {
                return r;
            }
        }
        return null;
    }

    private static RobotInfo[] findAllByType(final RobotInfo[] nearby, final RobotType type) {
        int count = 0;
        for (RobotInfo r : nearby) {
            if (r.type == type) {
                ++count;
            }
        }
        RobotInfo[] matches = new RobotInfo[count];
        int idx = 0;
        for (RobotInfo r : nearby) {
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
        RobotInfo[] nearby = rc.senseNearbyRobots(RobotType.MINER.sensorRadiusSquared, rc.getTeam());
        RobotInfo[] landscapers = findAllByType(nearby, RobotType.LANDSCAPER);
        if (landscapers.length < 8) {
            trySpawn(RobotType.LANDSCAPER);
        }
    }

    static void runFulfillmentCenter() throws GameActionException {

    }

    static void runLandscaper() throws GameActionException {
        RobotInfo[] nearby = rc.senseNearbyRobots(RobotType.MINER.sensorRadiusSquared, rc.getTeam());
        if (cachedHqLocation == null) {
            RobotInfo hq = findNearestByType(nearby, RobotType.HQ);
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

        // want to form a wall of size 5x5, with evenly spaced landscapers
        // fill from one edge to the other
        int[][] offsets = {{2, 0}, {2, 2}, {2, -2}, {0, 2}, {0, -2}, {-2, 2}, {-2, -2}, {-2, 0}};
        int sensorRange = getCurrentSensorRadiusSquared();
        MapLocation last_unoccupied_location = cachedHqLocation.translate(offsets[offsets.length - 1][0],
                offsets[offsets.length - 1][1]);
        for (int[] offset : offsets) {
            MapLocation target = cachedHqLocation.translate(offset[0], offset[1]);
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
                for (Direction d : allDirections()) {
                    MapLocation tile = rc.getLocation().add(d);
                    int dx = Math.abs(tile.x - cachedHqLocation.x);
                    int dy = Math.abs(tile.y - cachedHqLocation.y);
                    boolean isWall = (dx == 2 || dy == 2) && (dx <= 2 && dy <= 2);
                    if (!isWall) {
                        continue;
                    }
                    if (!rc.canDepositDirt(d)) {
                        continue;
                    }
                    int elevation = rc.senseElevation(tile);
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
                for (Direction d : allDirections()) {
                    MapLocation tile = rc.getLocation().add(d);
                    int dx = Math.abs(tile.x - cachedHqLocation.x);
                    int dy = Math.abs(tile.y - cachedHqLocation.y);
                    boolean isTrough = (dx >= 3 || dy >= 3);
                    if (!isTrough) {
                        continue;
                    }
                    int elevation = rc.senseElevation(tile);
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

    static void runDeliveryDrone() throws GameActionException {

    }

    // Return all enemy unit in vision
    static RobotInfo[] CheckRadar()
    {
        return rc.senseNearbyRobots(-1,rc.getTeam().opponent());
    }

    // Pick the closest shootable unit
    static RobotInfo pickTarget(RobotInfo[] targetList)
    {
        RobotInfo target=null;
        int minDis=Integer.MAX_VALUE;
        for(RobotInfo tg:targetList)
        {
            if(minDis>rc.getLocation().distanceSquaredTo(tg.getLocation()))
            {
                if(rc.canShootUnit(tg.getID()))
                {
                    target=tg;
                    minDis=rc.getLocation().distanceSquaredTo(tg.getLocation());
                }
            }
        }
        return target;

    }
    static void runNetGun() throws GameActionException {
        RobotInfo[] targetList=CheckRadar();
        RobotInfo target=pickTarget(targetList);
        if(target!=null)
        {
            rc.shootUnit(target.getID());
        }
    }

    static BehaviorResult trySpawn(RobotType type) throws GameActionException {
        for (Direction d : octalDirections) {
            if (rc.canBuildRobot(type, d)) {
                rc.buildRobot(type, d);
                return BehaviorResult.SUCCESS;
            }
        }
        return BehaviorResult.FAIL;
    }

    enum BehaviorResult {
        // the action complete successfully
        SUCCESS,
        // the action could not be peformed because preconditions of the action were not met
        FAIL,
        // the action could not be performed, but it might be possible next turn, so subsequent actions should be
        // canceled
        POSTPONED
    }
}