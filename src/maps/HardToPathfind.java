package maps;

import battlecode.world.MapBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Generate a map.
 */
public final class HardToPathfind {

    // change this!!!
    // this needs to be the same as the name of the file
    // it also cannot be the same as the name of an existing engine map
    public static final String mapName = new Object() {
    }.getClass().getEnclosingClass().getSimpleName();

    // don't change this!!
    public static final String outputDirectory = "maps/";

    private static final String IMAGE_FILENAME = "map_images/" + mapName + ".png";

    private static final int DEFAULT_ELEVATION = 2;
    private static final ArrayList<int[]> LOW_ELEVATION_TILES = new ArrayList<>();
    private static final ArrayList<int[]> MED_ELEVATION_TILES = new ArrayList<>();
    private static final ArrayList<int[]> HIGH_ELEVATION_TILES = new ArrayList<>();
    private static final ArrayList<int[]> SOUP_TILES = new ArrayList<>();
    private static final ArrayList<int[]> WATER_TILES = new ArrayList<>();
    private static final ArrayList<int[]> DEEP_WATER_TILES = new ArrayList<>();

    static {
        try {
            final BufferedImage image = ImageIO.read(new File(IMAGE_FILENAME));
            for (int x = 0; x < image.getWidth(); ++x) {
                for (int y = 0; y < image.getHeight(); ++y) {
                    // flip y coord
                    final int rgb = image.getRGB(x, image.getHeight() - 1 - y);
                    final int r = (rgb >> 16) & 255;
                    final int g = (rgb >> 8) & 255;
                    final int b = (rgb) & 255;

                    if (r == g && g == b) {
                        if (r == 0) {
                            HIGH_ELEVATION_TILES.add(new int[]{x, y});
                        } else if (r == 128) {
                            MED_ELEVATION_TILES.add(new int[]{x, y});
                        } else if (r == 192) {
                            LOW_ELEVATION_TILES.add(new int[]{x, y});
                        }
                    } else if (g == 0 && b == 0) {
                        SOUP_TILES.add(new int[]{x, y});
                    } else if (r == 0 && g == 0) {
                        WATER_TILES.add(new int[]{x, y});
                    } else if (r == 0 && b == 0) {
                        DEEP_WATER_TILES.add(new int[]{x, y});
                    }
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

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
        mapBuilder.addSymmetricHQ(0, height - 1);

        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                mapBuilder.setSymmetricDirt(i, j, DEFAULT_ELEVATION);
            }
        }
        for (final int[] coord : LOW_ELEVATION_TILES) {
            mapBuilder.setSymmetricDirt(coord[0], coord[1], 5);
        }
        for (final int[] coord : MED_ELEVATION_TILES) {
            mapBuilder.setSymmetricDirt(coord[0], coord[1], 50);
        }
        for (final int[] coord : HIGH_ELEVATION_TILES) {
            mapBuilder.setSymmetricDirt(coord[0], coord[1], 500);
        }
        for (final int[] coord : SOUP_TILES) {
            mapBuilder.setSymmetricSoup(coord[0], coord[1], 200);
        }
        for (final int[] coord : WATER_TILES) {
            mapBuilder.setSymmetricDirt(coord[0], coord[1], -20);
            mapBuilder.setSymmetricWater(coord[0], coord[1], true);
        }
        for (final int[] coord : DEEP_WATER_TILES) {
            mapBuilder.setSymmetricDirt(coord[0], coord[1], Integer.MIN_VALUE);
            mapBuilder.setSymmetricWater(coord[0], coord[1], true);
        }

        mapBuilder.saveMap(outputDirectory);
    }
}
