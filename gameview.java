
package com.example.flappybirdjava;

import android.content.Context;
import android.graphics.*;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

public class GameView extends SurfaceView implements Runnable {

    private Thread thread;
    private boolean isPlaying;

    private Bitmap bird, pipe, background;

    private int birdX = 100, birdY = 500;
    private int velocity = 0, gravity = 2;

    private int pipeX;
    private int gap = 400;
    private int pipeOffset;

    private int score = 0;

    private boolean gameOver = false; // ? NEW

    private SurfaceHolder holder;
    private Random random;

    public GameView(Context context) {
        super(context);

        holder = getHolder();
        random = new Random();

        // Load images
        bird = BitmapFactory.decodeResource(getResources(), R.drawable.bird);
        pipe = BitmapFactory.decodeResource(getResources(), R.drawable.pipe);
        background = BitmapFactory.decodeResource(getResources(), R.drawable.background);

        // Resize images
        bird = Bitmap.createScaledBitmap(bird, 120, 120, false);
        pipe = Bitmap.createScaledBitmap(pipe, 150, 600, false);

        pipeX = 800;
        pipeOffset = random.nextInt(400) - 200;
    }

    @Override
    public void run() {
        while (isPlaying) {
            update();
            draw();
            sleep();
        }
    }

    private void update() {

        // STOP movement if game over
        if (gameOver) return;

        // Bird physics
        birdY += velocity;
        velocity += gravity;

        // Pipe movement
        pipeX -= 8;

        if (pipeX < -pipe.getWidth()) {
            pipeX = getWidth();
            pipeOffset = random.nextInt(400) - 200;
            score++;
        }

        // Collision detection
        Rect birdRect = new Rect(birdX, birdY,
                birdX + bird.getWidth(), birdY + bird.getHeight());

        Rect topPipe = new Rect(pipeX, 0,
                pipeX + pipe.getWidth(),
                getHeight()/2 - gap/2 + pipeOffset);

        Rect bottomPipe = new Rect(pipeX,
                getHeight()/2 + gap/2 + pipeOffset,
                pipeX + pipe.getWidth(),
                getHeight());

        if (Rect.intersects(birdRect, topPipe) ||
                Rect.intersects(birdRect, bottomPipe) ||
                birdY < 0 || birdY > getHeight()) {

            gameOver = true; // ? STOP GAME
        }
    }

    private void draw() {

        if (holder.getSurface().isValid()) {

            Canvas canvas = holder.lockCanvas();

            // Clear screen
            canvas.drawColor(Color.BLACK);

            // Background
            canvas.drawBitmap(background, null,
                    new Rect(0, 0, getWidth(), getHeight()), null);

            // Pipes
            canvas.drawBitmap(pipe, pipeX,
                    getHeight()/2 - gap/2 + pipeOffset - pipe.getHeight(), null);

            canvas.drawBitmap(pipe, pipeX,
                    getHeight()/2 + gap/2 + pipeOffset, null);

            // Bird
            canvas.drawBitmap(bird, birdX, birdY, null);

            // Score
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setTextSize(60);
            canvas.drawText("Score: " + score, 50, 100, paint);

            // ? Game Over UI
            if (gameOver) {
                paint.setTextSize(80);
                canvas.drawText("GAME OVER", 200, 500, paint);

                paint.setTextSize(50);
                canvas.drawText("Tap to Restart", 200, 600, paint);
            }

            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void sleep() {
        try {
            Thread.sleep(17); // ~60 FPS
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }

    public void pause() {
        try {
            isPlaying = false;
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            if (gameOver) {
                // Restart Game
                gameOver = false;
                score = 0;
                birdY = 500;
                velocity = 0;
                pipeX = getWidth();
            } else {
                velocity = -20; // Jump
            }
        }

        return true;
    }
}
