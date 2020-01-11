import java.util.ArrayList;

class CommTest
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

    public static final int MESSAGE_LENGTH             =   7;   
    public static final int UNIMPORTANT_TRANSC_COST    =   1;
    public static final int IMPORTANT_TRANSC_COST      =   5;          // I am so cheap
    public static final Boolean DECODE_EVEN            =   true;
    public static final Boolean DECODE_ODD             =   !DECODE_EVEN;
    public static final Boolean ENCODE_ODD             =   DECODE_ODD;
    public static final Boolean ENCODE_EVEN             =  DECODE_EVEN;

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

    public CommTest()
    {
        Key=new int[]{192923,2,3,4};
        int mess=22223;
        int encoded=Encode1(mess,Key[0],ENCODE_EVEN);
        System.out.println("original "+mess+" encoded "+encoded + " decoded " + Decode(encoded,DECODE_EVEN));
    }

    public static void main(String[] args) {
        CommTest a= new CommTest();
    }

    // extract message from the original  message
    static int Decode1(int orgMess,Boolean odd_mask)
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
    static int[] EncodeMessage(int[] message) throws IllegalStateException
    {
        if(Key==null)
        {   
            System.out.println("No key, no Encoding!");
            throw(new IllegalStateException());
        }

        int[] tmp=new int[7];
        for(int i=0;i<message.length;i++)
        {
            tmp[i]=Encode(message[i],Key[i],(i%2==0?ENCODE_EVEN:ENCODE_ODD));
        }
        return tmp;
    }

    // General interface, change Encode1->Encode2 to use plan 2
    static int Decode(int orgMess,Boolean odd_mask)
    {
        return Decode1(orgMess,odd_mask);
    }

    static int Encode(int orgMess,int CheckSum,Boolean odd_mask)
    {
        return Encode1(orgMess,CheckSum,odd_mask);
    }

    // Encode with plan 1 message from the original  message
    static int Encode1(int orgMess,int CheckSum,Boolean odd_mask)
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

    public static int EncodeMapLocation(int X,int Y)
    {
        return (X<<8)|Y;
    }

}