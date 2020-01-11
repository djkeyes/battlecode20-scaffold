package addingComm;

import java.util.*;

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
        if (miners_built == 0 || (miners_built < 2 && design != null) || (miners_built < 4 && design != null && landscapers.length >= 8) || rc.getTeamSoup() > 1.5 * RobotType.VAPORATOR.cost) {
            BehaviorResult result = trySpawn(RobotType.MINER);
            if (result != BehaviorResult.FAIL) {
                if (result == BehaviorResult.SUCCESS) {
                    miners_built++;
                }
                return;
            }
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

    static void runNetGun() throws GameActionException {

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

class CommSys
{
    /*
    *   @Idea: 
    *       + Key : the key used for check sum is what ever in the first transaction of 
    *       the first block
    *       + CheckSum algorithm: 
                Plan 1 :For subsequent transaction after the first block,
    *       if there is a transaction from our team, the messages (number) with odd index
    *       in that transaction will have the bit with odd index when xor with the message
    *       of the key at that index result 0. Use even index bits for even index message 
    *           For ex: For simplicity, let key has 3 numbers
    *                  key =  0b00010100 0b10111001 0b11000110
                                  even        odd      even
    *        trasanction 1 =  0b10011100 0b10110011 0b11001100
    *        trasanction 2 =  0b10100100 0b10110101 0b10010100
    *        transaction 1 is from our team and transaction 2 is not
    *       real meassages are integers so we have 16 bits for our information in every message
    *           Plan 2 : For subsequent transaction after the first block,
    *       if there is a transaction from our team, the messages (number) with odd index
    *       will have their higher word for checksum, same method with XOR, even index messages
    *       have their low word for the checksum
    *       Plan 2 is more economical in term of computational power
    */

    public final int MESSAGE_LENGTH             =   GameConstants.MAX_BLOCKCHAIN_TRANSACTION_LENGTH;   
    public final int UNIMPORTANT_TRANSC_COST    =   1;
    public final int IMPORTANT_TRANSC_COST      =   5;          // I am so cheap
    public final Boolean DECODE_EVEN            =   true;
    public final Boolean DECODE_ODD             =   !DECODE_EVEN;
    // Checksum mask
    public final int PLAN_1_CHECK_SUM_MASK_ODD  =   0b10101010101010101010101010101010; // Odd bit for checksum
    public final int PLAN_1_CHECK_SUM_MASK_EVEN =   0b01010101010101010101010101010101; // Even for checksum
    public final int PLAN_2_CHECK_SUM_MASK_ODD  =   0b11111111111111110000000000000000; // high word for checksum
    public final int PLAN_2_CHECK_SUM_MASK_EVEN =   0b00000000000000001111111111111111; // low word for checksum
        // Notice, to get the value of the message, the mask is the complement of the the corresponding checksum

    private int LastReadRound;         // Index of last read round 
    private int CurrentRound;
    private int[] Key;
    Transaction[] Magazine;                 // Block added in the latest round
    int[] Mes;                              // Use to store decoded message
    private RobotController robot;
    
    public CommSys(RobotController robot)
    {
        Key=null;
        this.robot=robot;
        LastReadRound=0;
        CurrentRound=robot.getRoundNum();
    }

    /*
    *   Read the news on from the block chain
    *   This need to be called by the bot every round before they do anything
    */
    public void ReadNews()
    {
        CurrentRound=robot.getRoundNum();
        CatchUpPress();
    }

    // Read all the message from the first round to the last round
    // Increase the counter along the way
    // This will take a lot of time when the bot calls it the first time
    // However, once all the blocks are read, every time it only read 1 last block
    private void CatchUpPress()
    {
        while(LastReadRound!=CurrentRound-1)
        {
            try
            {
                Magazine=robot.getBlock(LastReadRound);       // Get the transactions
            }
            catch(GameActionException e)
            {
                // What can go wrong with this?
            }
            // lastReadRound should always be the last round, otherwise, catch up
            if(isKeyAvailable())
            {
                // Key is available, let's read
                ReadNExecute(FilterMessage(Magazine));
            }
            // Out when we read the last round
        }
    }

    // Check if key is available
    // Check if no key is because this robot is newly born?
    // or is no key because there is no first block?
    // place a transaction to create the first block, may be it become our key
    // Increase counter
    private boolean isKeyAvailable()
    {
        if(Key!=null)
        {
            return true;
        }
        else
        {
            // There is an available block here
            if(Magazine.length!=0)
            {
                Key=Magazine[0].getMessage();       // Save the first transaction as the key then
                // Also, take off the first transaction so that ReadMessage won't read this again
                // HOW???
                // Instead of removing the message, let makes the first message become invalid
                // This might not be the most clever way to do this, but it's quick, so!
                Magazine[0]= new Transaction(1,new int[GameConstants.MAX_BLOCKCHAIN_TRANSACTION_LENGTH],1);
                return true;
            }
            else
            {
                // if this block we examining is the last before the current
                // send in the first transaction
                if(LastReadRound==CurrentRound-1)
                {
                    SendInFirstTrans();
                    // Hm, don't know waht to do if I cannot submit the transaction
                    // Do nothing and wait for other bot to submit then
                }
                else
                {
                    // Do nothing here
                    LastReadRound++;
                }
                return false;
            }
        }
    }

    /*
    *   Plan 1 checksum
    */
    private int[] checksum1(int[] message)
    {
        int[] tmp=new int[message.length];
        for(int i=0;i<message.length;i++)
        {
            if(i%2==0)
            {
                if((message[i]^PLAN_1_CHECK_SUM_MASK_EVEN)==0)
                {
                    tmp[i]=Decode1(message[i],DECODE_ODD);
                }
                else
                {
                    return null;
                }
            }
            else
            {
                if((message[i]^PLAN_1_CHECK_SUM_MASK_ODD)==0)
                {
                    tmp[i]=Decode1(message[i],DECODE_EVEN);                    
                }
                else
                {
                    return null;
                }
            }
        }
        return tmp;
    }

    // Send in a random message to be the first transaction
    // return true if send succesfully
    // false otherwise
    private void SendInFirstTrans()
    {
        int[] randMess = RandomMessage();
        if(robot.canSubmitTransaction(randMess,UNIMPORTANT_TRANSC_COST))
        {
            try
            {
                robot.submitTransaction(randMess,UNIMPORTANT_TRANSC_COST);
            }
            catch(GameActionException e)
            {
                // Do what here?
            }
        }
        else
        {
            // Dont know what to do here
        }
    }
 
    private int[] RandomMessage()
    {
        int[] randMess= new int[GameConstants.MAX_BLOCKCHAIN_TRANSACTION_LENGTH];
        for(int i=0;i<GameConstants.MAX_BLOCKCHAIN_TRANSACTION_LENGTH;i++)
        {
            randMess[i]=robot.getID()*(i+1);     // Math.random() does not work ? so Whatever
        }
        return randMess;
    } 

    // Filter and decode message in transactions
    private ArrayList<int[]> FilterMessage(Transaction[] news)
    {
        ArrayList<int[]> message = new ArrayList<int[]>();
        int[] tmp;
        for(int i=0;i<news.length;i++)
        {
            // If checksum match, add it to the message list
            tmp=checksum1(news[i].getMessage());
            // Checksum1 automatically decode the message
            if(tmp!=null)
            {
                // Save the decoded message
                message.add(tmp);
            }
        }
        return message;
    }

    // extract message from the original  message
    int Decode1(int orgMess,Boolean odd_mask)
    {
        int finalMess=0;
        if(odd_mask==DECODE_ODD)
        {
            orgMess>>=1;
        }
        for(int i=0;i<16;i++)
        {
            if((orgMess & (1<<i*2))!=0)
            {
                finalMess|=(1<<i);  
            } 
        }
        return finalMess;
    }

    private void ReadNExecute(ArrayList<int[]> orderStack)
    {

    }
}
