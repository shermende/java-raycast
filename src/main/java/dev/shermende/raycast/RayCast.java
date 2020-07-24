package dev.shermende.raycast;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class RayCast extends JFrame implements KeyListener, MouseMotionListener {

    private final int screenWidth;
    private final int screenHeight;

    private final int[] pixels;
    private final transient BufferedImage image;
    private final transient Camera player;

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
        this.player = Camera.builder()
            .positionX(4)
            .positionY(4.5)
            .movementSpeed(0.1)
            .angleOfRotation(Math.PI / 2)
            .angleOfRotationSpeed(0.5)
            .fieldOfView(Math.PI / 4)
            .build();

        long lastTime = System.nanoTime();
        final double ns = 1000000000.0 / 60;//60 times per second
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
            double nextX = Math.sin(this.player.getAngleOfRotation()) * this.player.getMovementSpeed();
            double nextY = Math.cos(this.player.getAngleOfRotation()) * this.player.getMovementSpeed();
            if (MAP[(int) (this.player.getPositionX() + nextX)][(int) (this.player.getPositionY() + nextY)] == 0) {
                this.player.setPositionX(this.player.getPositionX() + nextX);
                this.player.setPositionY(this.player.getPositionY() + nextY);
            }
        }
        if (this.player.isPlayerBack()) {
            double nextX = Math.sin(this.player.getAngleOfRotation()) * this.player.getMovementSpeed();
            double nextY = Math.cos(this.player.getAngleOfRotation()) * this.player.getMovementSpeed();
            if (MAP[(int) (this.player.getPositionX() - nextX)][(int) (this.player.getPositionY() - nextY)] == 0) {
                this.player.setPositionX(this.player.getPositionX() - nextX);
                this.player.setPositionY(this.player.getPositionY() - nextY);
            }
        }
    }

    private void imageRender() {
        for (int i = 0; i < pixels.length / 2; i++)
            if (pixels[i] != Color.DARK_GRAY.getRGB()) pixels[i] = Color.DARK_GRAY.getRGB();
        for (int i = pixels.length / 2; i < pixels.length; i++)
            if (pixels[i] != Color.gray.getRGB()) pixels[i] = Color.gray.getRGB();

        for (int x = 0; x < screenWidth; x++) {
            double fRayAngle =
                (this.player.getAngleOfRotation() - this.player.getFieldOfView() / 2.0f)
                    + ((float) x / (float) screenWidth) * this.player.getFieldOfView();
            Color color = null;
            double step = 0.01;
            double distance = 0.0;
            double fEyeX = Math.sin(fRayAngle);
            double fEyeY = Math.cos(fRayAngle);
            boolean hit = false;
            while (!hit) {
                distance += step;
                int nTestX = (int) (this.player.getPositionX() + fEyeX * distance);
                int nTestY = (int) (this.player.getPositionY() + fEyeY * distance);
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
                .filter(var -> var > 0).map(var -> Math.abs((int) (screenHeight / var)))
                .filter(var -> var <= screenHeight).orElse(screenHeight);

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
            this.player.setAngleOfRotation(this.player.getAngleOfRotation() - 0.05 * this.player.getAngleOfRotationSpeed());
        else
            this.player.setAngleOfRotation(this.player.getAngleOfRotation() + 0.05 * this.player.getAngleOfRotationSpeed());
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
    public static class Camera {
        private double positionX;
        private double positionY;
        private double movementSpeed;

        private double angleOfRotation;
        private double angleOfRotationSpeed;
        private double fieldOfView;

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