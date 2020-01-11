package maps;

import battlecode.world.MapBuilder;

import java.io.IOException;

import static maps.MapGenUtils.addCircleSoup;
import static maps.MapGenUtils.addRectangleSoup;

/**
 * Generate a map.
 */
public final class Big {

    // change this!!!
    // this needs to be the same as the name of the file
    // it also cannot be the same as the name of an existing engine map
    public static final String mapName = new Object() {
    }.getClass().getEnclosingClass().getSimpleName();

    // don't change this!!
    public static final String outputDirectory = "maps/";

    /**
     * @param args unused
     */
    public static void main(final String[] args) {
        try {
            makeSimple();
        } catch (final IOException e) {
            System.out.println(e);
        }
        System.out.println("Generated a map!");
    }

    public static void makeSimple() throws IOException {
        final int width = 64;
        final int height = 64;
        final MapBuilder mapBuilder = new MapBuilder(mapName, width, height, 148);
        mapBuilder.setWaterLevel(0);
        mapBuilder.setSymmetry(MapBuilder.MapSymmetry.rotational);
        mapBuilder.addSymmetricHQ(0, 0);

        // make a gradual ramp starting from the edges
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                final int dist_from_edge = Math.min(i, Math.min(j, Math.min(width - 1 - i, height - 1 - j)));
                final int elev = Math.max(5 - (int) Math.sqrt(dist_from_edge + 9) + 3, 2);
                mapBuilder.setSymmetricDirt(i, j, elev);
            }
        }
        addRectangleSoup(mapBuilder, 3, 4, 7, 9, 100);

        addCircleSoup(mapBuilder, 12, 5, 3, 200);
        addCircleSoup(mapBuilder, 12, 5, 2, 500);
        addCircleSoup(mapBuilder, 12, 5, 1, 1000);

        addRectangleSoup(mapBuilder, 20, 26, 27, 30, 100);
        addRectangleSoup(mapBuilder, 22, 24, 28, 29, 500);

        addRectangleSoup(mapBuilder, 1, 28, 8, 36, 100);
        addCircleSoup(mapBuilder, 4, 32, 3, 2000);

        // water in middle
        for (int i = width / 2 - 1; i <= width / 2 + 1; ++i) {
            for (int j = width / 2 - 1; j <= height / 2 + 1; ++j) {
                mapBuilder.setSymmetricDirt(i, j, -20);
                mapBuilder.setWater(i, j, true);
            }
        }
        mapBuilder.setSymmetricDirt(width / 2, height / 2, Integer.MIN_VALUE);

        mapBuilder.saveMap(outputDirectory);
    }
}
