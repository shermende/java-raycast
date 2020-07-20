package dev.shermende.raycast;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.util.Optional;

/**
 * Created by abdys on 20/7/20.
 * n.u.abdysamat@gmail.com,developer@shermende.dev
 */
public class RayCast implements KeyListener, MouseMotionListener, MouseListener {

    private final String title = "";
    private final Canvas canvas;

    private int mouseX;
    private int mouseY;
    private int playerX;
    private int playerY;
    private double playerDirection;
    private final double playerDirectionSpeed = 1.5;
    private final double playerMovementSpeed = 3;
    private boolean playerForward;
    private boolean playerBack;
    private boolean playerLeft;
    private boolean playerRight;

    public RayCast() {
        canvas = new Canvas();
        JFrame frame = new JFrame();
        frame.setSize(640, 480);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setTitle(title);
        frame.add(canvas);
        canvas.addMouseMotionListener(this);
        canvas.addKeyListener(this);
        canvas.addMouseListener(this);
        canvas.requestFocus();

        this.playerX = frame.getWidth() / 2;
        this.playerY = frame.getHeight() / 2;

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
                movement();
                delta--;
            }
            render();
        }

    }

    public void render() {
        final Optional<BufferStrategy> optional = Optional.ofNullable(canvas.getBufferStrategy());
        if (!optional.isPresent()) {
            canvas.createBufferStrategy(3);
            return;
        }

        final BufferStrategy bs = optional.get();
        final Graphics g = bs.getDrawGraphics();
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        //Begin

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        double fFOV = Math.PI / 3;
        int nScreenWidth = 640;
        g.setColor(Color.RED);
        for (int x = 0; x < 640; x++) {
            double fRayAngle = (playerDirection - fFOV / 4.0f) + ((float) x / (float) nScreenWidth) * fFOV;
            double fEyeX = Math.sin(fRayAngle) * 100;
            double fEyeY = Math.cos(fRayAngle) * 100;
            g.drawLine(playerX, playerY, (playerX + (int) fEyeX), (playerY + (int) fEyeY));
        }

        //Stop
        g.dispose();
        bs.show();
    }

    private void movement() {
        if (playerForward) {
            playerY -= 1 * playerMovementSpeed;
        }
        if (playerBack && playerY < 900) {
            playerY += 1 * playerMovementSpeed;
        }
        if (playerLeft) {
            playerX -= 1 * playerMovementSpeed;
        }
        if (playerRight && playerX < 1600) {
            playerX += 1 * playerMovementSpeed;
        }
    }

    @Override
    public void keyPressed(
            KeyEvent keyEvent
    ) {
        if ((keyEvent.getKeyCode() == KeyEvent.MOUSE_EVENT_MASK))
            System.out.println("mouse");

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