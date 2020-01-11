package maps;

import battlecode.world.MapBuilder;

import java.io.IOException;

/**
 * Generate a map.
 */
public final class CheckerboardB {

    // change this!!!
    // this needs to be the same as the name of the file
    // it also cannot be the same as the name of an existing engine map
    public static final String mapName = new Object() { }.getClass().getEnclosingClass().getSimpleName();

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
        final int width = 49;
        final int height = 49;
        final MapBuilder mapBuilder = new MapBuilder(mapName, width, height, 148);
        mapBuilder.setWaterLevel(0);
        mapBuilder.setSymmetry(MapBuilder.MapSymmetry.horizontal);
        final int parity = 1; // 0 = even tiles tall, 1 = odd tiles tall
        mapBuilder.addSymmetricHQ(19 + parity, 6);

        final int defaultElevation = 3;
        final int superTall = 10000 * 9 + 4 + defaultElevation; // even 9 landscapers working nonstop could not remove
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                if ((i + j) % 2 == parity) {
                    mapBuilder.setSymmetricDirt(i, j, superTall);
                } else {
                    mapBuilder.setSymmetricDirt(i, j, defaultElevation);
                }
            }
        }
        // water in the center
        for (int dx = -3; dx <= 3; ++dx) {
            for (int dy = -3; dy <= 3; ++dy) {
                mapBuilder.setSymmetricDirt(width / 2 + parity, height / 2, -20);
                mapBuilder.setWater(width / 2 + parity, height / 2, true);
            }
        }
        mapBuilder.setSymmetricDirt(width / 2 + parity, height / 2, Integer.MIN_VALUE);

        mapBuilder.addSymmetricCow(5, 18);
        mapBuilder.addSymmetricCow(17, 3);

        mapBuilder.saveMap(outputDirectory);

    }
}
