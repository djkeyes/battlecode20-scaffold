package addingComm;

import java.util.ArrayList;

import battlecode.common.*;

import static battlecode.common.Direction.*;

class TestFunctions
{

	/*
	* The order of trasaction returned by getBlock of a specific round return an array of which
	* messages have the same index.
	*/
	static void testBlockChain(RobotController rc)
    {
        Transaction[] trans;
        int[] message;
        if(rc.getRoundNum()==1)
        {
            try
            {   
                for(int i=0;i<7;i++)
                {
                    message=new int[]{i,i,i,i,i,i,i};
                    rc.submitTransaction(message,5);
                }
            }
            catch(GameActionException e)
            {
                System.out.println("Message did not get thru");
            }
        }
        else
        {
            try
            {
                trans=rc.getBlock(1);
                for(int i=0;i<trans.length;i++)
                {
                    System.out.println("Transaciton "+i);
                    for(int j=0;j<trans[i].getMessage().length;j++)
                    {
                        System.out.print(" "+trans[i].getMessage()[j]);
                    }
                    System.out.println();
                }
            }
            catch(GameActionException e)
            {

            }
        }
    }

}