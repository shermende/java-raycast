package dev.shermende.raycast;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * Created by abdys on 20/7/20.
 * n.u.abdysamat@gmail.com,developer@shermende.dev
 */
public class RayCast implements KeyListener, MouseMotionListener, MouseListener {

    private final String title = "";

    private final int screenWidth = 640;
    private final int screenHeight = 480;
    private BufferedImage image;
    public int[] pixels;
    private int mouseX;
    private int mouseY;
    private int playerX;
    private int playerY;
    private double playerDirection;
    private final double fFOV = Math.PI / 3;
    private final double playerDirectionSpeed = 1.5;
    private final double playerMovementSpeed = 0.001;
    private boolean playerForward;
    private boolean playerBack;
    private boolean playerLeft;
    private boolean playerRight;
    private JFrame frame;

    public static int[][] map =
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


    public RayCast() {
        frame = new JFrame();
        frame.setSize(screenWidth, screenHeight);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setTitle(title);
        frame.addMouseMotionListener(this);
        frame.addKeyListener(this);
        frame.addMouseListener(this);
        frame.requestFocus();

        image = new BufferedImage(640, 480, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        this.playerX = 3;
        this.playerY = 11;

        long lastTime = System.nanoTime();
        final double ns = 1000000000.0 / 60.0;//60 times per second
        double delta = 0;
        while (true) {
            long now = System.nanoTime();
            delta = delta + ((now - lastTime) / ns);
            lastTime = now;
            while (delta >= 1)//Make sure update is only happening 60 times a second
            {
                //handles all of the logic restricted time
                imageRender();
                movement();
                delta--;
            }
            render();
        }

    }

    public int color() {
        return 0;
    }

    private void imageRender() {
        for (int n = 0; n < pixels.length / 2; n++)
            if (pixels[n] != Color.DARK_GRAY.getRGB())
                pixels[n] = Color.DARK_GRAY.getRGB();
        for (int i = pixels.length / 2; i < pixels.length; i++)
            if (pixels[i] != Color.gray.getRGB())
                pixels[i] = Color.gray.getRGB();

        for (int x = 0; x < screenWidth; x++) {
            double fRayAngle = (playerDirection - fFOV / 4.0f) + ((float) x / (float) screenWidth) * fFOV;
            double distance = 0.0;
            int nTestX = 0;
            int nTestY = 0;
            double step = 0.5;
            double fEyeX = Math.sin(fRayAngle);
            double fEyeY = Math.cos(fRayAngle);
            boolean hit = false;
            while (!hit) {
                distance += step;
                nTestX = (int) (playerX + fEyeX * distance);
                nTestY = (int) (playerY + fEyeY * distance);
                if (nTestX < 0 || nTestX >= 15 || nTestY < 0 || nTestY >= 15) {
                    hit = true;            // Just set distance to maximum depth
                    distance = 50;
                } else if (map[nTestX][nTestY] > 0) {
                    hit = true;
                } else if (distance >= 50) {
                    hit = true;
                }
            }

            int lineHeight;
            if (distance > 0)
                lineHeight = Math.abs((int) (screenHeight / distance));
            else
                lineHeight = screenHeight;
            //calculate lowest and highest pixel to fill in current stripe
            int drawStart = -lineHeight / 2 + screenHeight / 2;
            if (drawStart < 0)
                drawStart = 0;
            int drawEnd = lineHeight / 2 + screenHeight / 2;
            if (drawEnd >= screenHeight)
                drawEnd = screenHeight - 1;

            for (int y = drawStart; y < drawEnd; y++) {
                pixels[x + y * screenWidth] = color(map[nTestX][nTestY]);
            }
        }
    }

    public int color(int x) {
        if (x == 4)
            return new Color(150, 0, 0).getRGB();
        if (x == 3)
            return new Color(0, 150, 0).getRGB();
        if (x == 2)
            return new Color(0, 0, 150).getRGB();
        return new Color(150, 0, 150).getRGB();
    }

//    public int brigthnessColor(int x, double distance) {
//        if (distance >= 2)
//            return x & new Color(150, 150, 150).getRGB();
//        if (distance >= 5)
//            return x & new Color(130, 130, 130).getRGB();
//        if (distance >= 10)
//            return x & new Color(110, 110, 110).getRGB();
//        if (distance >= 15)
//            return x & new Color(80, 80, 80).getRGB();
//        return x & new Color(50, 50, 50).getRGB();
//    }

    public void render() {
        BufferStrategy bs = frame.getBufferStrategy();
        if (bs == null) {
            frame.createBufferStrategy(3);
            return;
        }
        Graphics g = bs.getDrawGraphics();

        g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
        bs.show();
//        final Optional<BufferStrategy> optional = Optional.ofNullable(canvas.getBufferStrategy());
//        if (!optional.isPresent()) {
//            canvas.createBufferStrategy(3);
//            return;
//        }
//
//        final BufferStrategy bs = optional.get();
//        final Graphics g = bs.getDrawGraphics();
//        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
//        //Begin
//
//        g.setColor(Color.WHITE);
//        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
//
//        double fFOV = Math.PI / 3;
//        g.setColor(Color.RED);
//        for (int x = 0; x < screenWidth; x++) {
//            double fRayAngle = (playerDirection - fFOV / 4.0f) + ((float) x / (float) screenWidth) * fFOV;
//            double fEyeX = Math.sin(fRayAngle) * 100;
//            double fEyeY = Math.cos(fRayAngle) * 100;
//            g.drawLine(playerX, playerY, (playerX + (int) fEyeX), (playerY + (int) fEyeY));
//        }
//
//        //Stop
//        g.dispose();
//        bs.show();
    }

    private void movement() {
        if (playerForward && playerY > 0) {
            playerY += 1 * playerMovementSpeed;
        }
        if (playerBack && playerY < 16) {
            playerY -= 1 * playerMovementSpeed;
        }
        if (playerLeft && playerX > 0) {
            playerX += 1 * playerMovementSpeed;
        }
        if (playerRight && playerX < 16) {
            playerX -= 1 * playerMovementSpeed;
        }
    }

    @Override
    public void keyPressed(
        KeyEvent keyEvent
    ) {
        if ((keyEvent.getKeyCode() == KeyEvent.VK_W))
            this.playerForward = true;

        if ((keyEvent.getKeyCode() == KeyEvent.VK_S))
            this.playerBack = true;

        if ((keyEvent.getKeyCode() == KeyEvent.VK_A))
            this.playerLeft = true;

        if ((keyEvent.getKeyCode() == KeyEvent.VK_D))
            this.playerRight = true;
    }

    @Override
    public void keyReleased(
        KeyEvent keyEvent
    ) {
        if ((keyEvent.getKeyCode() == KeyEvent.VK_W))
            this.playerForward = false;

        if ((keyEvent.getKeyCode() == KeyEvent.VK_S))
            this.playerBack = false;

        if ((keyEvent.getKeyCode() == KeyEvent.VK_A))
            this.playerLeft = false;

        if ((keyEvent.getKeyCode() == KeyEvent.VK_D))
            this.playerRight = false;
    }

    public void mouseDragged(
        MouseEvent mouseEvent
    ) {
        final int oldMouseX = this.mouseX;
        this.mouseX = mouseEvent.getX();
        this.mouseY = mouseEvent.getY();
        if (oldMouseX < mouseX) this.playerDirection -= 0.05 * playerDirectionSpeed;// left
        else this.playerDirection += 0.05 * playerDirectionSpeed;// right
    }

    public void mouseMoved(
        MouseEvent mouseEvent
    ) {
        final int oldMouseX = this.mouseX;
        this.mouseX = mouseEvent.getX();
        this.mouseY = mouseEvent.getY();
        if (oldMouseX < mouseX) this.playerDirection -= 0.05 * playerDirectionSpeed;// left
        else this.playerDirection += 0.05 * playerDirectionSpeed;// right
    }

    @Override
    public void keyTyped(
        KeyEvent keyEvent
    ) {

    }

    public static void main(
        String... args
    ) {
        new RayCast();
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        System.out.println(mouseEvent.getButton());
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {

    }
}