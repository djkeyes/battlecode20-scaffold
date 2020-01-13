package DroneSwarm;

import LinkedList.*;

public class DroneController
{
	// Activity code
	public static final int DO_NOTHING=0;
	public static final int CIRCLING_MY_TEAM_HQ=1;
	public static final int CIRCLING_ENEMY_TEAM_HQ=2;
	public static final int MARCHING_TO_TARGET=3;
	// Radius
	public static final int MY_HQ_CIRCLING_RADIUS = 4;		// blocks aways from the center
	public static final int ENEMY_HQ_CIRCLING_RADIUS = 5;	// blocks aways from the center
	// Direction Clockwise/counter-clockwise etc
	public static final int CLOCKWISE = 0;
	public static final int COUNTER_CLOCKWISE = 1;
	// The robot
	public static RobotController rc;
	// The general knowledge
	public static MapLocation myHQLoc;
	public static MapLocation enemyHQLoc;
	public static DLinkedList<Activity> thoughtStack; // last thought is the currently executed, if an acitivity is done, move the the previous activity in the list
													  // This is to create the effect that the unit do something, something else come up, the do something else and then come back to do the previous one after the new one is done

	public DroneController(RobotController robot, MapLocation myHQ,MapLocation enemyHQ);
	{
		rc=robot;
		MY_TEAM_HQ=null;
		ENEMY_TEAM_HQ=null;
		CirclingTarget = TARGET_ENEMY_TEAM_HQ;
		myHQLoc=myHQ;
		enemyHQLoc=enemyHQ;
		thoughtStack=new DLinkedList<Activity>();
	}

	public class Activity
	{
		int activityName;		// The activity Code
		boolean isDone;		// is it Done yet

		public Activity()
		{
			activityName = DO_NOTHING;		// The activity Code
			isDone = true;		// is it Done yet
		}
		// Send in an activity code
		public void doThis(int activity)
		{
			getBusy();
			switch(activityName)
			{
				case CIRCLING_ENEMY_TEAM_HQ:
					break;
				case CIRCLING_MY_TEAM_HQ:
					break;
				case MARCHING_TO_TARGET:
					break;
				default:
					System.out.println("Unrecognized activity code" + activity +" have you taught me this yet?");
			}
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
	}

	// Class represent the activity of orbitting a center
	public class CirclingActivity extends Activity
	{
																// CENTER, EAST, NORTH, NORTHEAST, NORTHWEST, 
		static final MapLocation[] COUNTER_CLOCKWISE_DIRECITON ={}; 
		MapLocation Center;
		int Radius;	// in block
		int RadiusSquared;
		int Direction;
	}

	public class MarchingActivity extends Activity
	{
		MapLocation Target;

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

	public static MapLocation findHQInitialApproaching2PointCircleTrajectory(Team HQteam)
	{
		MapLocation center;
		int CirclingRadius;
		if(HQteam==MY_TEAM)
		{
			center = myHQLoc;
			CirclingRadius= MY_HQ_CIRCLING_RADIUS;
		}
		else
		{
			center = enemyHQLoc;
			CirclingRadius= ENEMY_HQ_CIRCLING_RADIUS;
		}

		return 
	}

	// Find the obtimal point to approach the circile trajectory
	// Radius is # of block
	public static MapLocation findInitialApproachingPoint(MapLocation myLocation, MapLocation centerLocation, int radius) throws IllegalArgumentException
	{
		// is there space to do the circling trajectory?
		if((centerLocation.x-radius) < 0 || (centerLocation.x+radius > rc.getMapWidth-1) || (centerLocation.y-radius < 0) || (centerLocation.y+radius) > rc.getMapHeight - 1)
		{
			throw(new IllegalArgumentException());
			return null;
		}
		else
		{
			Direction dir=centerLocation.directionTo(myLocation);
			switch(dir)
			{
				case EAST:
				case WEST:
				case NORTH:
				case SOUTH:
					return centerLocation.translate(radius*dir.getDeltaX(),radius*dir.getDeltaY());
				case NORTHEAST:
				case NORTHWEST:
				case SOUTHEAST:
				case SOUTHWEST:
					// Pick the side resulting in smaller distance
					Direction roLeft=dir.rotateLeft();
					Direction roRight=dir.rotateRight();
					int distRoLeft=myLocation.distanceSquaredTo(centerLocation.translate(radius*roLeft.getDeltaX(),radius*roLeft.getDeltaY()));
					int distRoRight=myLocation.distanceSquaredTo(centerLocation.translate(radius*roRight.getDeltaX(),radius*roRight.getDeltaY()));
					if(distRoLeft<distRoRight)
					{
						return centerLocation.translate(radius*roLeft.getDeltaX(),radius*roLeft.getDeltaY());
					}
					else
					{
						return centerLocation.translate(radius*roRight.getDeltaX(),radius*roRight.getDeltaY());
					}
					break;

			}
		}

	}

	public void continueCircleMyHQ()
	{
		
	}
	
	public void continueCircleEnemyHQ()
	{

	}

}