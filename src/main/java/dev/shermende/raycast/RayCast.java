package dev.shermende.raycast;

import lombok.Builder;
import lombok.Data;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Optional;

/**
 * Created by abdys on 20/7/20.
 * n.u.abdysamat@gmail.com,developer@shermende.dev
 */
public class RayCast extends JFrame implements KeyListener, MouseMotionListener {

    private final int screenWidth;
    private final int screenHeight;

    private final int[] pixels;
    private final transient BufferedImage image;
    private final transient Player player;

    private int mouseX;

    protected static final int[][] MAP =
        {
            {1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2},
            {1, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2},
            {1, 0, 3, 3, 3, 3, 3, 0, 0, 0, 0, 0, 0, 0, 2},
            {1, 0, 3, 0, 0, 0, 3, 0, 2, 0, 0, 0, 0, 0, 2},
            {1, 0, 3, 0, 0, 0, 3, 0, 2, 2, 2, 0, 2, 2, 2},
            {1, 0, 3, 0, 0, 0, 3, 0, 2, 0, 0, 0, 0, 0, 2},
            {1, 0, 3, 3, 0, 3, 3, 0, 2, 0, 0, 0, 0, 0, 2},
            {1, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2},
            {1, 1, 1, 1, 1, 1, 1, 1, 4, 4, 4, 0, 4, 4, 4},
            {1, 0, 0, 0, 0, 0, 1, 4, 0, 0, 0, 0, 0, 0, 4},
            {1, 0, 0, 0, 0, 0, 1, 4, 0, 0, 0, 0, 0, 0, 4},
            {1, 0, 0, 0, 0, 0, 1, 4, 0, 3, 3, 3, 3, 0, 4},
            {1, 0, 0, 0, 0, 0, 1, 4, 0, 3, 3, 3, 3, 0, 4},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4},
            {1, 1, 1, 1, 1, 1, 1, 4, 4, 4, 4, 4, 4, 4, 4}
        };


    public RayCast(
        int screenWidth,
        int screenHeight
    ) {
        setTitle("");
        setSize(screenWidth, screenHeight);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
        addMouseMotionListener(this);
        addKeyListener(this);
        requestFocus();
        createBufferStrategy(3);

        this.screenHeight = screenHeight;
        this.screenWidth = screenWidth;
        this.image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        this.pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        this.player = Player.builder()
            .playerX(3.5)
            .playerY(4.5)
            .playerFieldOfView(Math.PI / 4)
            .playerDirection(Math.PI / 2)
            .playerDirectionSpeed(0.5)
            .playerMovementSpeed(0.1)
            .build();

        long lastTime = System.nanoTime();
        final double ns = 1000000000.0 / 60.0;//60 times per second
        double delta = 0;
        while (true) {
            long now = System.nanoTime();
            delta = delta + ((now - lastTime) / ns);
            lastTime = now;
            while (delta >= 1) {
                imageRender();
                movement();
                delta--;
            }
            render();
        }

    }

    private void movement() {
        if (this.player.isPlayerForward()) {
            double nextX = Math.sin(this.player.getPlayerDirection()) * this.player.getPlayerMovementSpeed();
            double nextY = Math.cos(this.player.getPlayerDirection()) * this.player.getPlayerMovementSpeed();
            if (MAP[(int) (this.player.getPlayerX() + nextX)][(int) (this.player.getPlayerY() + nextY)] == 0) {
                this.player.setPlayerX(this.player.getPlayerX() + nextX);
                this.player.setPlayerY(this.player.getPlayerY() + nextY);
            }
        }
        if (this.player.isPlayerBack()) {
            double nextX = Math.sin(this.player.getPlayerDirection()) * this.player.getPlayerMovementSpeed();
            double nextY = Math.cos(this.player.getPlayerDirection()) * this.player.getPlayerMovementSpeed();
            if (MAP[(int) (this.player.getPlayerX() - nextX)][(int) (this.player.getPlayerY() - nextY)] == 0) {
                this.player.setPlayerX(this.player.getPlayerX() - nextX);
                this.player.setPlayerY(this.player.getPlayerY() - nextY);
            }
        }
    }

    private void imageRender() {
        for (int n = 0; n < pixels.length / 2; n++)
            if (pixels[n] != Color.DARK_GRAY.getRGB()) pixels[n] = Color.DARK_GRAY.getRGB();
        for (int i = pixels.length / 2; i < pixels.length; i++)
            if (pixels[i] != Color.gray.getRGB()) pixels[i] = Color.gray.getRGB();

        for (int x = 0; x < screenWidth; x++) {
            double fRayAngle =
                (this.player.getPlayerDirection() - this.player.getPlayerFieldOfView() / 2.0f)
                    + ((float) x / (float) screenWidth) * this.player.getPlayerFieldOfView();
            Color color = null;
            double step = 0.01;
            double distance = 0.0;
            double fEyeX = Math.sin(fRayAngle);
            double fEyeY = Math.cos(fRayAngle);
            boolean hit = false;
            while (!hit) {
                distance += step;
                int nTestX = (int) (this.player.getPlayerX() + fEyeX * distance);
                int nTestY = (int) (this.player.getPlayerY() + fEyeY * distance);
                if (nTestX < 0 || nTestX >= 15 || nTestY < 0 || nTestY >= 15) {
                    hit = true;
                    distance = 50;
                    color = shadeColor(color(MAP[nTestX][nTestY]), distance);
                }
                if (MAP[nTestX][nTestY] > 0 || distance >= 50) {
                    hit = true;
                    color = shadeColor(color(MAP[nTestX][nTestY]), distance);
                }
            }

            int lineHeight = Optional.of(distance)
                .filter(var -> var > 0).map(var -> Math.abs((int) (screenHeight / var))).orElse(screenHeight);

            int drawStart = Optional.of(((-lineHeight / 2) + (screenHeight / 2)))
                .filter(var -> var > 0).orElse(0);
            int drawEnd = Optional.of(((lineHeight / 2) + (screenHeight / 2)))
                .filter(var -> var <= screenHeight).orElse(screenHeight - 1);

            for (int y = drawStart; y < drawEnd; y++) pixels[x + y * screenWidth] = color.getRGB();

        }
    }

    public Color color(int x) {
        if (x == 4) return new Color(150, 0, 0);
        if (x == 3) return new Color(0, 150, 0);
        if (x == 2) return new Color(0, 0, 150);
        return new Color(150, 0, 150);
    }

    public Color shadeColor(Color color, double distance) {
        if (distance <= 1) return darken(color, 0.1);
        if (distance <= 2) return darken(color, 0.15);
        if (distance <= 3) return darken(color, 0.2);
        if (distance <= 4) return darken(color, 0.25);
        return darken(color, 0.3);
    }

    public Color darken(Color color, double fraction) {
        int red = (int) Math.round(Math.max(0, color.getRed() - 255 * fraction));
        int green = (int) Math.round(Math.max(0, color.getGreen() - 255 * fraction));
        int blue = (int) Math.round(Math.max(0, color.getBlue() - 255 * fraction));
        int alpha = color.getAlpha();
        return new Color(red, green, blue, alpha);
    }

    public void render() {
        final BufferStrategy bs = getBufferStrategy();
        final Graphics g = bs.getDrawGraphics();
        g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
        bs.show();
    }

    @Override
    public void keyPressed(
        KeyEvent keyEvent
    ) {
        if ((keyEvent.getKeyCode() == KeyEvent.VK_W)) this.player.setPlayerForward(true);
        if ((keyEvent.getKeyCode() == KeyEvent.VK_S)) this.player.setPlayerBack(true);
    }

    @Override
    public void keyReleased(
        KeyEvent keyEvent
    ) {
        if ((keyEvent.getKeyCode() == KeyEvent.VK_W)) this.player.setPlayerForward(false);
        if ((keyEvent.getKeyCode() == KeyEvent.VK_S)) this.player.setPlayerBack(false);
    }

    @Override
    public void mouseMoved(
        MouseEvent mouseEvent
    ) {
        final int oldMouseX = this.mouseX;
        this.mouseX = mouseEvent.getX();
        if (oldMouseX > mouseX)
            this.player.setPlayerDirection(this.player.getPlayerDirection() - 0.05 * this.player.getPlayerDirectionSpeed());
        else
            this.player.setPlayerDirection(this.player.getPlayerDirection() + 0.05 * this.player.getPlayerDirectionSpeed());
    }

    @Override
    public void mouseDragged(
        MouseEvent mouseEvent
    ) {
        // no action
    }

    @Override
    public void keyTyped(
        KeyEvent keyEvent
    ) {
        // no action
    }

    @Data
    @Builder
    public static class Player {
        private double playerX;
        private double playerY;
        private double playerMovementSpeed;

        private double playerDirection;
        private double playerDirectionSpeed;
        private double playerFieldOfView;

        private boolean playerForward;
        private boolean playerBack;
        private boolean playerLeft;
        private boolean playerRight;
    }

    public static void main(
        String... args
    ) {
        new RayCast(640, 480);
    }

}