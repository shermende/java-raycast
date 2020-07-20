package dev.shermende.game;

/**
 * Created by abdys on 18/7/20.
 * n.u.abdysamat@gmail.com,developer@shermende.dev
 */

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Texture {
    public int[] pixels;
    private String loc;
    public final int SIZE;

    public Texture(String location, int size) {
        loc = location;
        SIZE = size;
        pixels = new int[SIZE * SIZE];
        load();
    }

    private void load() {
        try {
            BufferedImage image = ImageIO.read(new File(
                    Texture.class.getClassLoader().getResource(loc).getFile()
            ));
            int w = image.getWidth();
            int h = image.getHeight();
            image.getRGB(0, 0, w, h, pixels, 0, w);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Texture wood = new Texture("wood.png", 64);
    public static Texture brick = new Texture("redbrick.png", 64);
    public static Texture bluestone = new Texture("bluestone.png", 64);
    public static Texture stone = new Texture("greystone.png", 64);
}
