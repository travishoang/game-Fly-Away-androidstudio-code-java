package com.example.game;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
public class GameView extends SurfaceView implements Runnable {
    private Thread thread;
    private List<Bullet> bullets;
    private boolean isPlaying, isGameOver = false;
    private int screenX, screenY, score = 0;
    public static float screenRatioX, screenRatioY;
    private Paint paint;
    private Random random;
    private GreenFlight[] greenflys;
    private Flight flight;

    private SharedPreferences prefs;
    private GameActivity activity;
    private Background background1, background2;

    public GameView(GameActivity activity, int screenX, int screenY) {
        super(activity);

        this.screenX = screenX;
        this.screenY = screenY;
        screenRatioX = 1920f / screenX;
        screenRatioY = 1080f / screenY;

        background1 = new Background(screenX, screenY, getResources());
        background2 = new Background(screenX, screenY, getResources());

        flight = new Flight(this, screenY, getResources());
        bullets = new ArrayList<>();
        background2.x = screenX;
        prefs = activity.getSharedPreferences("game", Context.MODE_PRIVATE);
        paint = new Paint();
        paint.setTextSize(128);
        paint.setColor(Color.WHITE);

        greenflys = new GreenFlight[4];

        for (int i = 0;i < 4;i++) {

            GreenFlight greenFlight = new GreenFlight(getResources());
            greenflys[i] = greenFlight;
        }
        random = new Random();

    }


    @Override
    public void run() {

        while (isPlaying) {

            update ();
            draw ();
            sleep ();

        }

    }

    private void update () {

        background1.x -= 10;
        background2.x -= 10;

        if (background1.x + background1.background.getWidth() < 0) {
            background1.x = screenX;
        }

        if (background2.x + background2.background.getWidth() < 0) {
            background2.x = screenX;
        }

        if (flight.isGoingUp)
            flight.y -= 30 * screenRatioY;
        else
            flight.y += 30 * screenRatioY;

        if (flight.y < 0)
            flight.y = 0;

        if (flight.y >= screenY - flight.height)
            flight.y = screenY - flight.height;

        List<Bullet> trash = new ArrayList<>();

        for (Bullet bullet : bullets) {

            if (bullet.x > screenX)
                trash.add(bullet);

            bullet.x += 50 * screenRatioX;
            for (GreenFlight bird : greenflys) {

                if (Rect.intersects(bird.getCollisionShape(),
                        bullet.getCollisionShape())) {

                    score++;
                    bird.x = -500;
                    bullet.x = screenX + 500;
                    bird.wasShot = true;

                }

            }
        }



        for (Bullet bullet : trash)
            bullets.remove(bullet);

        for (GreenFlight greenFlight : greenflys){
            greenFlight.x -= greenFlight.speed;

            if (greenFlight.x + greenFlight.width <0 ){
                int bound = (int) (30 * screenRatioX);
                greenFlight.speed = random.nextInt(bound);

                if (greenFlight.speed < 10 * screenRatioX)
                    greenFlight.speed = (int) (10 * screenRatioX);

                greenFlight.x = screenX;
                greenFlight.y = random.nextInt(screenY - greenFlight.height);

                greenFlight.wasShot = false;
            }
            if (Rect.intersects(greenFlight.getCollisionShape(), flight.getCollisionShape())) {

                isGameOver = true;
                return;
            }
        }
    }

    private void draw () {

        if (getHolder().getSurface().isValid()) {

            Canvas canvas = getHolder().lockCanvas();
            canvas.drawBitmap(background1.background, background1.x, background1.y, paint);
            canvas.drawBitmap(background2.background, background2.x, background2.y, paint);
            for (GreenFlight greenFlight : greenflys)
                canvas.drawBitmap(greenFlight.getGreenfly(), greenFlight.x, greenFlight.y, paint);
            canvas.drawText(score + "", screenX / 2f, 164, paint);
            if (isGameOver) {
                isPlaying = false;
                canvas.drawBitmap(flight.getDead(), flight.x, flight.y, paint);
                getHolder().unlockCanvasAndPost(canvas);
                saveIfHighScore();
                waitBeforeExiting ();
                return;
            }
            canvas.drawBitmap(flight.getFlight(), flight.x, flight.y, paint);

            for (Bullet bullet : bullets)
                canvas.drawBitmap(bullet.bullet, bullet.x, bullet.y, paint);

//            int minHeight =850;
//            if (flight.y > minHeight){
//                isGameOver = true;
//            }
            getHolder().unlockCanvasAndPost(canvas);

        }

    }

    private void waitBeforeExiting() {

        try {
            Thread.sleep(1000);
            activity.startActivity(new Intent(activity, MainActivity.class));
            activity.finish();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void saveIfHighScore() {

        if (prefs.getInt("highscore", 0) < score) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("highscore", score);
            editor.apply();
        }

    }
    private void sleep () {
        try {
            Thread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume () {

        isPlaying = true;
        thread = new Thread(this);
        thread.start();

    }

    public void pause () {

        try {
            isPlaying = false;
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getX() < screenX / 2) {
                    flight.isGoingUp = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                flight.isGoingUp = false;
                if (event.getX() > screenX / 2)
                    flight.toShoot++;
                break;        }

        return true;
    }
    public void newBullet() {

        Bullet bullet = new Bullet(getResources());
        bullet.x = flight.x + flight.width;
        bullet.y = flight.y + (flight.height / 2);
        bullets.add(bullet);
    }
}

