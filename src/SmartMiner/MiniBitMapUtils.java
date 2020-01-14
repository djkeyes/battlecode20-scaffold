package SmartMiner;

import java.util.ArrayList;

import battlecode.common.*;

import static battlecode.common.Direction.*;

class MiniBitMapUtils
{
	/*
		Use bit to represent map
		for example , an 8x8 map can be 
		8 integer of 8 bit
		  map[7] 1 0 0 0 1 0 0 1
		  map[6] 1 0 0 1 1 0 0 1
		  map[5] 1 0 0 0 1 0 0 1
		  map[4] 1 0 0 0 1 0 0 1
		  map[3] 1 0 1 0 1 0 0 1
		  map[2] 1 0 0 0 1 0 0 1
		  map[1] 1 0 0 0 1 0 0 1 
		  map[0] 1 0 0 0 1 0 0 1 
			x/y  0 1 2 3 4 5 6 7 
		so value of (x,y) of (2,3)=1 and (5,1)=0

		KEEP IN MIND the above is just example
		THIS CLASS USE 64-bit integer to represent map
		since map in Battlecode is either 32x32 or 64x64 
		index from 0 -> 
	*/
	public static final long BIT_MASK_SET 	=	1<<63;					// 0b100000...
	public static final long BIT_MASK_UNSET =	Long.MAX_VALUE>>1; //0b01111....
	public int width;
	public int height;
	public long[] map;
	
	public MiniBitMapUtils(int width,int height)
	{
		this.map=new long[height];
		this.width=width;
		this.height=height;
	}
	
	// set location x,y on map to 1
	public void set(int x,int y) 
	{
		if((x>=width) || (y>=height))
		{
			System.out.println("coordinate out of bound");
			return;
		}
		else
		{
			map[x]|= (BIT_MASK_SET>>y);
		}
	}

	// unset location x,y on map to 0
	public void unset(int x,int y) 
	{
		if((x>=width) || (y>=height))
		{
			System.out.println("coordinate out of bound");
			return;
		}
		else
		{
			map[x]&=(BIT_MASK_UNSET>>y);
		}
	}

	// Get value at a location
	public boolean get(int x,int y)
	{
		if((x>=width) || (y>=height))
		{
			System.out.println("coordinate out of bound");
			return false;
		}
		else
		{
			return (map[x]&(BIT_MASK_SET>>y))==1;
		}		
	}
}