package com.example.game;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import static com.example.game.GameView.screenRatioX;
import static com.example.game.GameView.screenRatioY;
public class GreenFlight {
    public int speed = 20;
    public boolean wasShot = true;
    int x = 0, y, width, height, birdCounter = 1;
    Bitmap greenfly1, greenfly2;

    GreenFlight (Resources res) {

        greenfly1 = BitmapFactory.decodeResource(res, R.drawable.greenfly1);
        greenfly2 = BitmapFactory.decodeResource(res, R.drawable.greenfly2);


        width = greenfly1.getWidth();
        height = greenfly1.getHeight();

        width /= 6;
        height /= 6;

        width = (int) (width * screenRatioX);
        height = (int) (height * screenRatioY);

        greenfly1 = Bitmap.createScaledBitmap(greenfly1, width, height, false);
        greenfly2 = Bitmap.createScaledBitmap(greenfly2, width, height, false);


        y = -height;
    }

    Bitmap getGreenfly () {

        if (birdCounter == 0) {
            birdCounter++;
            return greenfly1;
        }

        birdCounter --;

        return greenfly2;
    }

    Rect getCollisionShape () {
        return new Rect(x, y, x + width, y + height);
    }

}
