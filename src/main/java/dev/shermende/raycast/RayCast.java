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

    private final transient Camera camera;
    private final transient BufferedImage image;
    private final int[] pixels;

    private boolean forward;
    private boolean back;
    private boolean left;
    private boolean right;
    private int mouse;
    private boolean running = true;

    protected static final int[][] MAP =
        {
            {1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2},// → x
            {1, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2},// ↓ y
            {1, 0, 3, 3, 3, 3, 3, 0, 0, 0, 0, 0, 0, 0, 2},// → 0˚ 0
            {1, 0, 3, 0, 0, 0, 3, 0, 2, 0, 0, 0, 0, 0, 2},// ↓ 90˚ PI/2
            {1, 0, 3, 0, 0, 0, 3, 0, 2, 2, 2, 0, 2, 2, 2},// ← 180˚ PI
            {1, 0, 3, 0, 0, 0, 3, 0, 2, 0, 0, 0, 0, 0, 2},
            {1, 0, 3, 3, 0, 3, 3, 0, 2, 0, 0, 0, 0, 0, 2},
            {1, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2},
            {1, 1, 1, 1, 1, 1, 1, 1, 4, 4, 4, 0, 4, 4, 4},
            {1, 0, 0, 0, 0, 0, 1, 4, 0, 0, 0, 0, 0, 0, 4},
            {1, 0, 0, 0, 0, 0, 1, 4, 0, 0, 0, 0, 0, 0, 4},
            {1, 0, 4, 0, 0, 0, 1, 4, 0, 3, 3, 3, 3, 0, 4},
            {1, 0, 0, 0, 0, 0, 1, 4, 0, 3, 3, 3, 3, 0, 4},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4},
            {1, 1, 1, 1, 1, 1, 1, 4, 4, 4, 4, 4, 4, 4, 4}
        };

    public RayCast(
        int screenWidth,
        int screenHeight
    ) {
        requestFocus();
        setTitle("GAME");
        setVisible(true);
        setSize(screenWidth, screenHeight);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        addKeyListener(this);
        addMouseMotionListener(this);

        this.screenHeight = screenHeight;
        this.screenWidth = screenWidth;
        this.image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        this.pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        this.camera = Camera.builder()
            .positionY(3)
            .positionX(4.5)
            .movementSpeed(0.05)
            .angleOfRotation(Math.PI / 2)
            .angleOfRotationSpeed(0.02)
            .fieldOfView(Math.PI / 4)
            .build();

        long lastTime = System.nanoTime();
        final double ns = 1000000000.0 / 60; // 60 times per second
        double delta = 0;
        while (running) {
            long now = System.nanoTime();
            delta = delta + ((now - lastTime) / ns);
            lastTime = now;
            while (delta >= 1) {
                framePrepare();
                frameRender();
                movement();
                delta--;
            }
            render();
        }
    }

    private void movement() {
        if (this.forward) {
            camera.forward();
            if (MAP[(int) (camera.getPositionY())][(int) (this.camera.getPositionX())] != 0) camera.back();
        }
        if (this.back) {
            camera.back();
            if (MAP[(int) (camera.getPositionY())][(int) (this.camera.getPositionX())] != 0) camera.forward();
        }
        if (this.left) {
            camera.left();
            if (MAP[(int) (this.camera.getPositionY())][(int) (this.camera.getPositionX())] != 0) camera.right();
        }
        if (this.right) {
            camera.right();
            if (MAP[(int) (this.camera.getPositionY())][(int) (this.camera.getPositionX())] != 0) camera.left();
        }
    }

    private void framePrepare() {
        for (int i = 0; i < pixels.length / 2; i++)
            if (pixels[i] != Color.DARK_GRAY.getRGB()) pixels[i] = Color.DARK_GRAY.getRGB();
        for (int i = pixels.length / 2; i < pixels.length; i++)
            if (pixels[i] != Color.gray.getRGB()) pixels[i] = Color.gray.getRGB();
    }

    private void frameRender() {
        for (int x = 0; x < screenWidth; x++) {
            double fRayAngle =
                (this.camera.getAngleOfRotation() - this.camera.getFieldOfView() / 2.0f) // minus half of FOV from angle of rotation
                    + ((float) x / (float) screenWidth) * this.camera.getFieldOfView();
            Color color = null;
            double step = 0.01;
            double distance = 0.0;
            double fEyeX = Math.cos(fRayAngle);
            double fEyeY = Math.sin(fRayAngle);
            boolean hit = false;
            while (!hit) {
                distance += step;
                int nTestX = (int) (this.camera.getPositionX() + fEyeX * distance);
                int nTestY = (int) (this.camera.getPositionY() + fEyeY * distance);
                if (nTestX < 0 || nTestX >= 15 || nTestY < 0 || nTestY >= 15) {
                    hit = true;
                    distance = 50;
                    color = shadeColor(color(MAP[nTestY][nTestX]), distance);
                }
                if (MAP[nTestY][nTestX] > 0 || distance >= 50) {
                    hit = true;
                    color = shadeColor(color(MAP[nTestY][nTestX]), distance);
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
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }
        bs = getBufferStrategy();
        final Graphics g = bs.getDrawGraphics();
        g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
        bs.show();
    }

    @Override
    public void keyPressed(
        KeyEvent keyEvent
    ) {
        if ((keyEvent.getKeyCode() == KeyEvent.VK_W)) this.forward = true;
        if ((keyEvent.getKeyCode() == KeyEvent.VK_S)) this.back = true;
        if ((keyEvent.getKeyCode() == KeyEvent.VK_A)) this.left = true;
        if ((keyEvent.getKeyCode() == KeyEvent.VK_D)) this.right = true;
    }

    @Override
    public void keyReleased(
        KeyEvent keyEvent
    ) {
        if ((keyEvent.getKeyCode() == KeyEvent.VK_W)) this.forward = false;
        if ((keyEvent.getKeyCode() == KeyEvent.VK_S)) this.back = false;
        if ((keyEvent.getKeyCode() == KeyEvent.VK_A)) this.left = false;
        if ((keyEvent.getKeyCode() == KeyEvent.VK_D)) this.right = false;
    }

    @Override
    public void mouseMoved(
        MouseEvent mouseEvent
    ) {
        final int oldMouse = this.mouse;
        this.mouse = mouseEvent.getX();
        if (oldMouse > mouse) camera.rotateLeft();
        else camera.rotateRight();
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
        private double positionY;
        private double positionX;
        private double movementSpeed;

        private double angleOfRotation;
        private double angleOfRotationSpeed;
        private double fieldOfView;

        public void move(
            double nextY,
            double nextX
        ) {
            setPositionY(getPositionY() + nextY);
            setPositionX(getPositionX() + nextX);
        }

        public void forward() {
            double nextX = Math.cos(getAngleOfRotation()) * getMovementSpeed();
            double nextY = Math.sin(getAngleOfRotation()) * getMovementSpeed();
            move(nextY, nextX);
        }

        public void back() {
            double nextX = Math.cos(getAngleOfRotation() - Math.PI) * getMovementSpeed();
            double nextY = Math.sin(getAngleOfRotation() - Math.PI) * getMovementSpeed();
            move(nextY, nextX);
        }

        public void left() {
            double nextX = Math.cos(getAngleOfRotation() - Math.PI / 2) * getMovementSpeed();
            double nextY = Math.sin(getAngleOfRotation() - Math.PI / 2) * getMovementSpeed();
            move(nextY, nextX);
        }

        public void right() {
            double nextX = Math.cos(getAngleOfRotation() + Math.PI / 2) * getMovementSpeed();
            double nextY = Math.sin(getAngleOfRotation() + Math.PI / 2) * getMovementSpeed();
            move(nextY, nextX);
        }

        public void rotateLeft() {
            setAngleOfRotation(getAngleOfRotation() - getAngleOfRotationSpeed());
        }

        public void rotateRight() {
            setAngleOfRotation(getAngleOfRotation() + getAngleOfRotationSpeed());
        }
    }

    public static void main(
        String... args
    ) {
        new RayCast(640, 480);
    }

}