import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {

    private static final long serialVersionUID = 1L;
    int boardWidth = 360;
    int boardHeight = 640;

    // Images
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    // For the Bird
    int birdX = boardWidth / 8;
    int birdY = boardHeight / 2;
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    // For the Pipes / Obstacles
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;
    int pipeHeight = 512;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int Width = pipeWidth;
        int Height = pipeHeight;
        Image img;

        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    // Game Processing
    Bird bird;
    int velocityX = -4;
    int velocityY = 0;
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipesTimer;
    boolean gameOver = false;
    double score = 0;
    double highScore = 0;

    JButton restartButton;

    private void restartGame() {
        bird.y = birdY;
        velocityY = 0;
        pipes.clear();
        score = 0;
        gameOver = false;
        placePipesTimer.restart();
        gameLoop.restart();
        repaint();
    }

    FlappyBird() {

        restartButton = new JButton("Restart");
        setPreferredSize(new Dimension(boardWidth, boardHeight));

        setFocusable(true);
        addKeyListener(this);

        // Load images
        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        // Bird
        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();

        // Place pipes timer
        placePipesTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });

        placePipesTimer.start();

        // Game timer
        gameLoop = new Timer(1000 / 60, this); // 1000/60 = 16.6
        gameLoop.start();

        restartButton = new JButton("Restart");
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restartGame();
            }
        });
        add(restartButton);
    }

    public void placePipes() {
        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingSpace = boardHeight / 4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);

        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));

        if (gameOver) {
            String gameOverMessage = "Game Over!";
            String scoreMessage = "Score: " + String.valueOf((int) score);

            int messageWidth = g.getFontMetrics().stringWidth(gameOverMessage);
            int messageHeight = g.getFontMetrics().getHeight();

            g.drawString(gameOverMessage, boardWidth / 2 - messageWidth / 2, boardHeight / 2 - messageHeight);
            g.drawString(scoreMessage, boardWidth / 2 - g.getFontMetrics().stringWidth(scoreMessage) / 2,
                    boardHeight / 2 + messageHeight);

            restartButton.setLocation(boardWidth / 2 - restartButton.getWidth() / 2,
                    boardHeight / 2 + 2 * messageHeight);
            restartButton.setVisible(true);
        } else {
            g.drawString("Score: " + String.valueOf((int) score), 10, 35);
            g.drawString("High Score: " + String.valueOf((int) highScore), 10, 75);
            restartButton.setVisible(false);
        }
    }



    public void draw(Graphics g) {
        // Background
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);

        // Bird
        g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

        // Pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.Width, pipe.Height, null);
        }
    }

    public void move() {
        // Bird
        velocityY += gravity;
        bird.y += velocityY;

        // Pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;

            if (!pipe.passed && bird.x > pipe.x + pipe.Width) {
                pipe.passed = true;
                score += 0.5;
            }

            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }

        if (bird.y > boardHeight) {
            gameOver = true;
        }

        if (score > highScore) {
            highScore = score;
        }
    }

    public boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.Width && // a's top left corner does not reach b's top right corner
                a.x + a.width > b.x && // a's top right corner passes b's top left corner
                a.y < b.y + b.Height && // a's top left corner does not reach b's bottom left corner
                a.y + a.height > b.y; // a's bottom left corner passes b's top left corner
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();

        if (gameOver) {
            placePipesTimer.stop();
            gameLoop.stop();
            restartButton.setVisible(true);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -9;
            if (gameOver) {
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                score = 0;
                gameOver = false;
                gameLoop.start();
                placePipesTimer.start();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

}
