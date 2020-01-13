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
	abstract boolean do();

}

class MarchingActivity extends Activity
{
	MapLocation Target;

	public MarchingActivity(MapLocation target, RobotController robot, MapLocation myHQ,MapLocation enemyHQ)
	{
		super(robot,myHQ,enemyHQ);
		Target=target;
	}

	void boolean tryNextMove()
	{
		Direction nextMove=rc.directionTo(Target);
		if(rc.canMove(nextMove))
		{
			rc.move(nextMove);
			if(rc.)
		}
	}

}