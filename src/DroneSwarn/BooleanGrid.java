package DroneSwarm;

import battlecode.common.GameConstants;

/**
 * A grid that stores booleans, backed by longs.
 * <p>
 * In the current battlecode implementation, allocating a bool[64][64] costs around 4000 bytecodes, but allocating a
 * long[64] costs around 70. Of course, it's slightly more expensive to get and set values in the array, so only use
 * this if you do sparse array access. And of course, this trick only works for booleans--it wouldn't work if you
 * need a double[][].
 */
public final class BooleanGrid {

    private static final int BITS_IN_BACKING_DATATYPE = 64;
    private long[] grid;

    public BooleanGrid() {
        // assumes evenly divisible
        reset();
    }

    public void reset() {
        grid = new long[GameConstants.MAP_MAX_HEIGHT * GameConstants.MAP_MAX_WIDTH / BITS_IN_BACKING_DATATYPE];
    }

    public boolean get(final int x, final int y) {
        final int gridIdx = y * GameConstants.MAP_MAX_WIDTH + x;
        final int elementIdx = gridIdx / BITS_IN_BACKING_DATATYPE;
        final int bitIdx = gridIdx % BITS_IN_BACKING_DATATYPE;
        return ((grid[elementIdx] >> bitIdx) & 1) != 0;
    }

    public void setTrue(final int x, final int y) {
        final int gridIdx = y * GameConstants.MAP_MAX_WIDTH + x;
        final int elementIdx = gridIdx / BITS_IN_BACKING_DATATYPE;
        final int bitIdx = gridIdx % BITS_IN_BACKING_DATATYPE;
        grid[elementIdx] |= (1 << bitIdx);
    }

    public void setFalse(final int x, final int y) {
        final int gridIdx = y * GameConstants.MAP_MAX_WIDTH + x;
        final int elementIdx = gridIdx / BITS_IN_BACKING_DATATYPE;
        final int bitIdx = gridIdx % BITS_IN_BACKING_DATATYPE;
        grid[elementIdx] &= ~(1 << bitIdx);
    }
}
