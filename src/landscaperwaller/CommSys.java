package landscaperwaller;

import battlecode.common.*;

import java.util.ArrayList;

/* =====================README============================
 *                   Comm interface
 * How to use ?
 *   Add an instance of the class to your RobotController
 *   Call ReadNews every new round
 * Adding More Commands?
 *   Add your choice of Command code as a constant
 *   Add the new command code to the corresponding switch
 *   in function ReadNececute()
 *   For adding a specific command for a specific unit (only HQ, or...)
 *   modify the switch case of function CatchUpPress()
 *   In your RobotController class
 *   when you need to send the command, use
 *   function send(int[] message)
 *   maximal length of message is GameConstants.MAX_BLOCKCHAIN_TRANSACTION_LENGTH
 *   You have to put the COMMAND CODE of your code to the
 *   first element of your message array (i.e:message[0])
 *   a message array needs at least 1 element
 *   the size of the message array can be smaller than
 *   GameConstants.MAX_BLOCKCHAIN_TRANSACTION_LENGTH
 */

public class CommSys
{
    /*
    *   @Idea:
    *       + Key : the key used for check sum is what ever in the first transaction of
    *       the first block
    *       + CheckSum algorithm:
    *           Plan 1 :For subsequent transaction after the first block,
    *       if there is a transaction from our team, the messages (number) with odd index
    *       in that transaction will have the bit with odd index when xor with the message
    *       of the key at that index result 0. Use even index bits for even index message
    *           For ex: For simplicity, let key has 3 numbers
    *                  key =  0b00010100 0b10111001 0b11000110
    *                             even        odd      even
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

    public static final int MESSAGE_LENGTH             =   GameConstants.MAX_BLOCKCHAIN_TRANSACTION_LENGTH;
    public static final int UNIMPORTANT_TRANSC_COST    =   1;
    public static final int IMPORTANT_TRANSC_COST      =   5;          // I am so cheap
    // ENCODE_EVEN->DECODE_EVEN ECONDE_ODD->DECODE_ODD
    public static final boolean DECODE_EVEN            =   true;
    public static final boolean DECODE_ODD             =   !DECODE_EVEN;
    public static final boolean ENCODE_ODD             =   DECODE_ODD;
    public static final boolean ENCODE_EVEN             =  DECODE_EVEN;

    // Checksum mask
    public static final int PLAN_1_CHECK_SUM_MASK_ODD  =   0b10101010101010101010101010101010; // Odd bit for checksum
    public static final int PLAN_1_CHECK_SUM_MASK_EVEN =   0b01010101010101010101010101010101; // Even for checksum
    public static final int PLAN_2_CHECK_SUM_MASK_ODD  =   0b11111111111111110000000000000000; // high word for checksum
    public static final int PLAN_2_CHECK_SUM_MASK_EVEN =   0b00000000000000001111111111111111; // low word for checksum
    // Maplocation Masks
    public static final int MAP_DECODE_X_MASK          =   0xFF00;
    public static final int MAP_DECODE_Y_MASK          =   0x00FF;
    // Notice, to get the value of the message, the mask is the complement of the the corresponding checksum
    public static final int NO_USE_SLOT                =   0xFF;
    // News list
    // For these news below, 
    // First message is the command code, second message 
    // after decoded will has X coordinate in high byte,
    // Y coordinate in low byte
    public static final int NEWS_ENEMY_HQ_FOUND        =   1;
    public static final int NEWS_REFINERY_BUILT        =   2;
    public static final int NEWS_DESIGN_SCHOOL_BUILT   =   3;
    public static final int NEWS_SOUP_FOUND            =   4;
    public static final int NEWS_SOUP_IS_OUT           =   5;
    // When communication start
    // This is to make sure that we can have different key for different opponents
    // The getID() only return pseudo random number so the id a robot received is deterministic
    // base on their spawn order, so if we just use getID(), the first robot Id will alwasy be the same
    // and the same key will be obtained everytime
    // however, different oppent have different strategy and different rate of spawning robots
    // Thus let comm start on later round will result in a random id for our first robot
    // It is not truly random, but because of the different in spawning strategy
    // when we will likely get diffrent key from opponent to opponent 
    public static final int COMM_START_ROUND            =   20;
    // Start communication when population is large enough
    // Again, this is also to add to the randomness of the key
//    public static final int COMM_START_POPULATION       =   4;

    public static int[] Key;
    private int LastReadRound;         // Index of last read transaction in the block chain
    private int CurrentRound;
    private Transaction[] Magazine;                 // Block added in the latest round
    private final RobotController robot;
    private final DLinkedList<MapLocation> RefineryLocs;
    private final DLinkedList<MapLocation> SoupLocs;
    private MapLocation Enemy_HQ;

    public static int DECENT_TRANSACTION_COST  = IMPORTANT_TRANSC_COST;     // Should implement a mechanism to find out the minimal cost for the message to be posted

    public CommSys(final RobotController robot)
    {
        Key=null;
        Enemy_HQ=null;
        this.robot=robot;
        RefineryLocs=new DLinkedList<>();
        SoupLocs=new DLinkedList<>();
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

    // Input in a list of decoded messages
    private void ReadNExecute(final ArrayList<int[]> orderStack)
    {
        MapLocation tmp;
        int[] message;
        // messages in here are already decoded
        for(int i=0;i<orderStack.size();i++)
        {
            message=orderStack.get(i);
            System.out.println("MESSAGE_CODE = "+message[0]);
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
                        System.out.println("Enemy_HQ found at ["+Enemy_HQ.x+","+Enemy_HQ.y+"]");
                    }
                    break;
                case NEWS_REFINERY_BUILT:
                    tmp=DecodeMapLocation(message[1]);
                    System.out.println("Refinery found at ["+tmp.x+","+tmp.y+"]");
                    if(RefineryLocs.isNew(tmp))
                    {
                        RefineryLocs.add(tmp);
                    }
                    break;
                case NEWS_SOUP_FOUND:
                    tmp=DecodeMapLocation(message[1]);
                    System.out.println("Soup found at ["+tmp.x+","+tmp.y+"]");
                    if(SoupLocs.isNew(tmp))
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

    // Read all the message from the first round to the last round
    // Increase the counter along the way
    // This will take a lot of time when the bot calls it the first time
    // However, once all the blocks are read, every time it only read 1 last block
    private void CatchUpPress()
    {
        if(CurrentRound<COMM_START_ROUND)
        {
            return;
        }

        ArrayList<int[]> DecodedMessage;

        while(LastReadRound<CurrentRound)
        {
            // System.out.println(LastReadRound);
            try
            {
                Magazine=robot.getBlock(LastReadRound);       // Get the transactions
            }
            catch(GameActionException e)
            {
                System.out.println("Cannot get block for round "+LastReadRound);
                // What can go wrong with this?
                // System.out.println(e.getMessage());
            }
            if(isKeyAvailable())
            {
                // Key is available, let's read
                DecodedMessage=FilterMessage(Magazine);

                switch(robot.getType())
                {
                    case HQ:
                    case DESIGN_SCHOOL:
                    case FULFILLMENT_CENTER:
                    case NET_GUN:
                    case VAPORATOR:
                    case MINER:
                    case LANDSCAPER:
                    case DELIVERY_DRONE:
                    ReadNExecute(DecodedMessage);
                    break;
                }
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

                // Printing the key
                System.out.print("Obtained Key - ");
                for(int i=0;i<Key.length;i++)
                {
                    System.out.print(Key[i] + " ");
                }
                System.out.println();

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

    // messeageCode is one of the constant used for indicating the identity
    // Ex: for enemy's HQ, NEWS_ENEMY_HQ_FOUND
    public void broadcastLocs(int messageCode, MapLocation loc)
    {
        broadcastLocs(messageCode,loc.x,loc.y);
    }

    public void broadcastLocs(final int messageCode, final int x, final int y)
    {
        final int[] news=new int[]{messageCode,EncodeMapLocation(x,y)};
        send(news,DECENT_TRANSACTION_COST);
    }

    public void broadcastUnitLocs(final RobotInfo rb)
    {
        // Currently only building Unit is here
        final RobotType unit_Type=rb.getType();
        if(!rb.getTeam().isPlayer())
        {
            // From the enemy
            switch(unit_Type)
            {
                case HQ:
                    System.out.println("Broadcasting Enemy HQ location");
                    broadcastLocs(NEWS_ENEMY_HQ_FOUND,rb.getLocation());
                    break;
            }
        }
        else
        {
            switch(unit_Type)
            {
                case REFINERY:
                    System.out.println("Broadcasting newly built Refinery");
                    broadcastLocs(NEWS_REFINERY_BUILT,rb.getLocation());
                    break;
                case DESIGN_SCHOOL:
                    System.out.println("Broadcasting newly built DESIGN_SCHOOL");
                    broadcastLocs(NEWS_DESIGN_SCHOOL_BUILT,rb.getLocation());
                    break;
            }
        }

    }

    public boolean send(final int[] message)
    {
        return send(message,DECENT_TRANSACTION_COST);
    }

    public boolean send(final int[] message, final int bid)
    {
        try
        {
            final int[] encodedMessage=EncodeMessage(message);
            if(robot.canSubmitTransaction(encodedMessage,bid))
            {
                robot.submitTransaction(encodedMessage,bid);
                return true;
            }
            else
            {
                return false;
            }
        }
        catch(final GameActionException e)
        {
            System.out.println("Having trouble submitting transaction");
            return false;
        }
        catch(final IllegalStateException e)
        {
            System.out.println("Encoding message failed, maybe because of no key?");
            return false;
        }
    }

    /*
    *   General checksum
    *   checksum and decode message
    *   return the decoded meassge if checksum make
    *   null or else
    */
    public int[] checksumNextract(final int[] message)
    {
        return checksum1Nextract(message);
    }

    /*
    *   Plan 1 checksum
    *   checksum and decode message
    *   return the decoded meassge if checksum make
    *   null or else
    */
    private int[] checksum1Nextract(final int[] message)
    {
        final int[] tmp=new int[message.length];
        for(int i=0;i<message.length;i++)
        {
            if(i%2==0)
            {
                if(((message[i]^Key[i])&PLAN_1_CHECK_SUM_MASK_ODD)==0)
                {
                    tmp[i]=Decode(message[i],DECODE_EVEN);
                }
                else
                {
                    System.out.println("Checksum unmatch");
                    return null;
                }
            }
            else
            {
                if(((message[i]^Key[i])&PLAN_1_CHECK_SUM_MASK_EVEN)==0)
                {
                    tmp[i]=Decode(message[i],DECODE_ODD);
                }
                else
                {
                    System.out.println("Checksum unmatched.");
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

        if(robot.getType()==RobotType.HQ)
        {
            // Do not let HQ sends in the first transaction
            // The RandomMessage() utilizes the randomness of robot id
            // to creaete random message.
            // id of HQ is either 0 or 1, so...
            return;
        }
        else
        {

            final int[] randMess= RandomMessage();

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
    }

    private int[] RandomMessage()
    {
        final int[] randMess= new int[GameConstants.MAX_BLOCKCHAIN_TRANSACTION_LENGTH];
        for(int i=0;i<GameConstants.MAX_BLOCKCHAIN_TRANSACTION_LENGTH;i++)
        {
            randMess[i]=robot.getID()*(i+1);     // Math.random() does not work ? so Whatever
        }
        return randMess;
    }

    // Filter and decode message in transactions
    private ArrayList<int[]> FilterMessage(final Transaction[] news)
    {
        final ArrayList<int[]> message = new ArrayList<>();
        int[] tmp;
        for(int i=0;i<news.length;i++)
        {
            // If checksum match, add it to the message list
            tmp=checksumNextract(news[i].getMessage());
            // Checksum1Nextract automatically decode the message
            if(tmp!=null)
            {
                // Save the decoded message
                message.add(tmp);
            }

        }
        return message;
    }

    // extract message from the original  message
    static int Decode1(int orgMess, final boolean odd_mask)
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

    // Encode message ( a sequence of integer)
    static int[] EncodeMessage(final int[] message) throws IllegalStateException
    {
        if(Key==null)
        {
            System.out.println("No key, no Encoding!");
            throw(new IllegalStateException());
        }

        final int[] tmp=new int[message.length];
        for(int i=0;i<message.length;i++)
        {
            tmp[i]=Encode(message[i],Key[i],(i%2==0?ENCODE_EVEN:ENCODE_ODD));
        }
        return tmp;
    }

    // General interface, change Encode1->Encode2 to use plan 2
    static int Decode(final int orgMess, final boolean odd_mask)
    {
        return Decode1(orgMess,odd_mask);
    }

    static int Encode(final int orgMess, final int CheckSum, final boolean odd_mask)
    {
        return Encode1(orgMess,CheckSum,odd_mask);
    }

    // Encode with plan 1 message from the original  message
    static int Encode1(final int orgMess, final int CheckSum, final boolean odd_mask)
    {
        int finalMess=0;
        int shift=0;
        if(odd_mask==ENCODE_EVEN)
        {
            finalMess|=(CheckSum&PLAN_1_CHECK_SUM_MASK_ODD);
            shift=0;
        }
        else
        {
            finalMess|=(CheckSum&PLAN_1_CHECK_SUM_MASK_EVEN);
            shift=1;
        }
        for(int i=0;i<16;i++)
        {
            if((orgMess&(1<<i))!=0)
            {
                finalMess|=(1<<(2*i+shift));
            }
        }
        return finalMess;
    }

    public static int EncodeMapLocation(final int X, final int Y)
    {
        return (X<<8)|Y;
    }

    public static MapLocation DecodeMapLocation(final int rawMes)
    {
        return new MapLocation(((rawMes&MAP_DECODE_X_MASK)>>8),rawMes&MAP_DECODE_Y_MASK);
    }
}