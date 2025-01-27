package SmartMiner;

import battlecode.common.*;

public abstract class Activity
{
	int activityName;		// The activity Code
	boolean isDone;		// is it Done yet
	public static RobotController rc;
	// The general knowledge
	public static MapLocation myHQLoc;
	public static MapLocation enemyHQLoc;

	public static final int DO_NOTHING=0;

	public Activity(RobotController robot, MapLocation myHQ,MapLocation enemyHQ);
	{
		rc=robot;
		myHQLoc=myHQ;
		enemyHQLoc=enemyHQ;
		activityName = DO_NOTHING;		// The activity Code
		isDone = true;		// is it Done yet
	}

	public Activity(int activityName)
	{

	}

	public void activityDone()
	{
		isDone=true;
	}
	public void getBusy()
	{
		isDone=false;
	}
	public boolean isBusy()
	{
		return !isDone;
	}
	abstract boolean tryNextMove();

}

class MarchingActivity extends Activity
{
	MapLocation Target;
	MiniBitMapUtils trailMap;
	public MarchingActivity(MapLocation target, RobotController robot, MapLocation myHQ,MapLocation enemyHQ)
	{
		super(robot,myHQ,enemyHQ);
		Target=target;
		trailMap=new MiniBitMapUtils(rc.getMapWidth(),rc.getMapHeight());
		trailMap.set(target.getLocation().x,target.getLocation().y);
	}
	
	public MarchingActivity(MapLocation target)
	{
		Target=target;
		trailMap=new MiniBitMapUtils(rc.getMapWidth(),rc.getMapHeight().y);
		trailMap.set(target.getLocation().x,target.getLocation().y);
	}

	// Simple part finding
	// false means can't move
	// throw NoSuchMethodException if no path found 
	void boolean tryNextMove() throws NoSuchMethodException
	{
		Direction nextMove=rc.directionTo(Target);
		boolean canMove;
		if(canMove=rc.canMove(nextMove))
		{
			rc.move(nextMove);
		}
		else if(canMove=rc.canMove(nextMove.rotateLeft()))
		{
			rc.move(nextMove.rotateLeft());
		}
		else if(canMove=rc.canMove(nextMove.rotateRight()))
		{
			rc.move(nextMove.rotateRight);
		}
		else
		{
			canMove=false;
		}
		// if target reached, stop activity
		if(canMove && rc.getLocation().equals(Target))
		{
			this.activityDone();
		}
		return canMove;
	}

}
