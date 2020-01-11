package maps;

import battlecode.world.MapBuilder;

import java.io.IOException;

import static maps.MapGenUtils.addCircleSoup;
import static maps.MapGenUtils.addRectangleSoup;

/**
 * Generate a map.
 */
public final class Small {

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
        final int width = 32;
        final int height = 32;
        final MapBuilder mapBuilder = new MapBuilder(mapName, width, height, 148);
        mapBuilder.setWaterLevel(0);
        mapBuilder.setSymmetry(MapBuilder.MapSymmetry.rotational);
        mapBuilder.addSymmetricHQ(13, 13);

        // make a ramp starting from the middle

        for (int i = 1; i < width - 1; ++i) {
            for (int j = 1; j < height - 1; ++j) {
                final int dist_from_edge = Math.min(i, Math.min(j, Math.min(width - 1 - i, height - 1 - j)));
                final int elev = Math.min(5, dist_from_edge);
                mapBuilder.setSymmetricDirt(i, j, elev);
            }
        }
        addRectangleSoup(mapBuilder, 3, 4, 7, 9, 100);
        addCircleSoup(mapBuilder, 12, 5, 3, 200);
        addCircleSoup(mapBuilder, 12, 5, 2, 500);
        addCircleSoup(mapBuilder, 12, 5, 1, 1000);
        // water on edges
        for (int i = 0; i < width; ++i) {
            mapBuilder.setSymmetricDirt(i, 0, -20);
            mapBuilder.setWater(i, 0, true);
            mapBuilder.setSymmetricDirt(i, height - 1, -20);
            mapBuilder.setWater(i, height - 1, true);
        }
        for (int i = 0; i < height; ++i) {
            mapBuilder.setSymmetricDirt(0, i, -20);
            mapBuilder.setWater(0, i, true);
            mapBuilder.setSymmetricDirt(width - 1, i, -20);
            mapBuilder.setWater(width - 1, i, true);
        }
        mapBuilder.setSymmetricDirt(0, 0, Integer.MIN_VALUE);

        mapBuilder.saveMap(outputDirectory);
    }
}
