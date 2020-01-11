package maps;

import battlecode.world.MapBuilder;

public final class MapGenUtils {

    public static void addRectangleSoup(final MapBuilder mapBuilder, final int xl, final int yb, final int xr, final int yt, final int v) {
        for (int i = xl; i < xr + 1; i++) {
            for (int j = yb; j < yt + 1; j++) {
                mapBuilder.setSymmetricSoup(i, j, v);
            }
        }
    }

    public static void addCircleSoup(final MapBuilder mapBuilder, final int x, final int y, final int rad,
                                     final int v) {
        for (int dx = -rad; dx <= rad; ++dx) {
            for (int dy = 0; dx * dx + dy * dy <= rad * rad; ++dy) {
                mapBuilder.setSymmetricSoup(x + dx, y + dy, v);
            }
            for (int dy = -1; dx * dx + dy * dy <= rad * rad; --dy) {
                mapBuilder.setSymmetricSoup(x + dx, y + dy, v);
            }
        }
    }
}
