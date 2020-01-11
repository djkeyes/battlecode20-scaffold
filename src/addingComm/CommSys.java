package addingComm;

import java.util.ArrayList;

import battlecode.common.*;

import static battlecode.common.Direction.*;

public class CommSys
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
    public final Boolean ENCODE_ODD             =   DECODE_ODD;
    public final Boolean ENCODE_EVEN             =  DECODE_EVEN;

    // Checksum mask
    public final int PLAN_1_CHECK_SUM_MASK_ODD  =   0b10101010101010101010101010101010; // Odd bit for checksum
    public final int PLAN_1_CHECK_SUM_MASK_EVEN =   0b01010101010101010101010101010101; // Even for checksum
    public final int PLAN_2_CHECK_SUM_MASK_ODD  =   0b11111111111111110000000000000000; // high word for checksum
    public final int PLAN_2_CHECK_SUM_MASK_EVEN =   0b00000000000000001111111111111111; // low word for checksum
    // Maplocation Masks
    public final int MAP_DECODE_X_MASK          =   0xFF00;
    public final int MAP_DECODE_Y_MASK          =   0x00FF;
    // Notice, to get the value of the message, the mask is the complement of the the corresponding checksum

    // News list
    // For these news below, 
    // First message is the command code, second message 
    // after decoded will has X coordinate in high byte,
    // Y coordinate in low byte
    public final int NEWS_ENEMY_HQ_FOUND        =   1;   
    public final int NEWS_REFINERY_FOUND        =   2;
    public final int NEWS_SOUP_FOUND            =   3;
    public final int NEWS_SOUP_IS_OUT           =   4;


    private int LastReadRound;         // Index of last read transaction in the block chain 
    private int CurrentRound;
    private int[] Key;
    private Transaction[] Magazine;                 // Block added in the latest round
    private int[] Mes;                              // Use to store decoded message
    private RobotController robot;
    private DLinkedList<MapLocation> RefineryLocs;
    private DLinkedList<MapLocation> SoupLocs;
    private MapLocation Enemy_HQ;

    public CommSys(RobotController robot)
    {
        Key=null;
        Enemy_HQ=null;
        this.robot=robot;
        RefineryLocs=new DLinkedList();
        SoupLocs=new DLinkedList();
        LastReadRound=1;
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
        if(CurrentRound<2)
        {
            return;
        }
        while(LastReadRound<CurrentRound)
        {
            // System.out.println(LastReadRound);
            try
            {
                Magazine=robot.getBlock(LastReadRound);       // Get the transactions
            }
            catch(GameActionException e)
            {
                // What can go wrong with this?
                // System.out.println(e.getMessage());
            }
            if(isKeyAvailable())
            {
                // Key is available, let's read
                ReadNExecute(FilterMessage(Magazine));
            }
            LastReadRound++;
        }
    }

    // Check if key is available
    // Check if no key is because this robot is newly born?
    // or is no key because there is no first block?
    // place a transaction to create the first block, might be it become our key
    // Increase counter
    private boolean isKeyAvailable()
    {
        if(Key!=null)
        {
            return true;
        }
        else
        {
            // First block is alread yplaced
            if(Magazine.length!=0)
            {
                Key=Magazine[0].getMessage();       // Save as the key then
                // Also, take off the first transaction so that ReadMessage won't read this again
                // HOW???
                // Instead of removing the message, let makes the first message become invalid
                // This might not be a most clever way to do this, but it quick, so!
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
                    // Check remaining round
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

    // Encode with plan 1 message from the original  message
    int Encode1(int orgMess,int CheckSum,Boolean odd_mask)
    {
        int finalMess=0;
        int shift=0;
        if(odd_mask==ENCODE_EVEN)
        {
            finalMess|=(CheckSumc&PLAN_1_CHECK_SUM_MASK_ODD);
            shift=0;
        }
        else
        {
            finalMess|=(CheckSumc&PLAN_1_CHECK_SUM_MASK_EVEN);            
            shift=1;
        }
        for(int i=0;i<16;i++)
        {
            if(orgMess&(1<<i))
            {
                finalMess|=(1<<(2*i+shift))
            }
        }
        return finalMess;
    }

    int EncodeLocation(int X,int Y)
    {
        return (X<<8)|Y;
    }

    private MapLocation DecodeMapLocation(int rawMes)
    {
        return new MapLocation(((rawMes&MAP_DECODE_X_MASK)>>8),rawMes&MAP_DECODE_Y_MASK);
    }

    private <T> boolean isNew(T vl, DLinkedList<T> list)
    {
        DLinkedList<T>.Iterator it=list.getIterator();
        while(it.hasNext())
        {
            if(vl.equals(it.getNext()))
            {
                return false;
            }
        }
        return true;
    }


    // Input in a list of decoded messages
    private void ReadNExecute(ArrayList<int[]> orderStack)
    {
        MapLocation tmp;
        int[] message;
        for(int i=0;i<orderStack.size();i++)
        {
            message=orderStack.get(i);
            switch(message[0])
            {
                case NEWS_ENEMY_HQ_FOUND:
                    if(Enemy_HQ!=null)
                    {
                        System.out.println("Second Enemy_HQ found, something is wrong");
                    }
                    else
                    {
                        Enemy_HQ=DecodeMapLocation(message[1]);
                    }
                    break;
                case NEWS_REFINERY_FOUND:
                    tmp=DecodeMapLocation(message[1]);
                    if(isNew(tmp,RefineryLocs))
                    {
                        RefineryLocs.add(tmp);
                    }
                    break;
                case NEWS_SOUP_FOUND:
                    tmp=DecodeMapLocation(message[1]);
                    if(isNew(tmp,SoupLocs))
                    {
                        SoupLocs.add(tmp);
                    }
                    break;
                case NEWS_SOUP_IS_OUT:
                    tmp=DecodeMapLocation(message[1]);
                    SoupLocs.findNremove(tmp);
                    break;
                default:
                    System.out.println("ALERT: ENEMY JAMMING IN EFFECTS");
            }
        }
    }
}