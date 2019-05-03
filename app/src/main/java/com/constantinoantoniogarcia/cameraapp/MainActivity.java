package com.constantinoantoniogarcia.cameraapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.constantinoantoniogarcia.cameraapp.models.ClassificationResult;
import com.constantinoantoniogarcia.cameraapp.models.TensorFlowClassifier;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {


    static final int REQUEST_IMAGE_CAPTURE = 1;
    private TensorFlowClassifier classifier;
    private static final String modelFile = "opt_Melanoma_NotMelanomaProtBuf.pb";//CAMBIAR
    private static final String inputTensorName = "conv2d_1_input";
    private static int pixelWidth = 224;
    private static final String outputTensorName = "dense_2/Sigmoid";
    private static final String labelsFile = "labels.txt";

    private Bitmap scaledBitmap;

    private int[] bitmapIntPixels;
    private float[] bitmapPixels;

    TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        result = findViewById(R.id.ClassificationText);
        Button buttonCamera = findViewById(R.id.button);
        buttonCamera.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                        }
                    }
                }
        );


        bitmapIntPixels = new int[pixelWidth * pixelWidth*3];
        bitmapPixels = new float[pixelWidth * pixelWidth*3];

        try {
            classifier = TensorFlowClassifier.create(getAssets(), "TensorFlow NNet",
                            modelFile, labelsFile, pixelWidth, inputTensorName, outputTensorName,
                            true);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error initializing classifiers!");
        }


    }
//puede desaparecer porque es para convertir a gris la foto que tomas
   /* public Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);



        // inversiont
        float invertMX[] = {
                -1.0f, 0.0f, 0.0f, 0.0f, 255f,
                0.0f, -1.0f, 0.0f, 0.0f, 255f,
                0.0f, 0.0f, -1.0f, 0.0f, 255f,
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f
        };
        ColorMatrix finalCM = new ColorMatrix(cm);
        ColorMatrix invertCM = new ColorMatrix(invertMX);
        finalCM.postConcat(invertCM);

        ColorMatrixColorFilter f = new ColorMatrixColorFilter(finalCM);
        paint.setColorFilter(f);

        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }*/

    private void bitmapToPixels(Bitmap bitmap) {
        scaledBitmap = Bitmap.createScaledBitmap(bitmap, pixelWidth, pixelWidth, true);
        scaledBitmap.getPixels(bitmapIntPixels, 0, scaledBitmap.getWidth(), 0, 0,
                scaledBitmap.getWidth(), scaledBitmap.getHeight());

        System.out.println("========================================");
        for (int i = 0; i < pixelWidth*pixelWidth; ++i) {//MODIFICAR a pixel por pixel
            final int val = bitmapIntPixels[i];
            // bitmapIntPixels[i] = (val & 0xFF);
            bitmapPixels[3 * i ] = Color.blue(val)/255;
            bitmapPixels[3 * i + 1] = Color.green(val)/255;
            bitmapPixels[3 * i + 2] = Color.red(val)/255;

            // https://stackoverflow.com/questions/48017924/how-to-get-android-pixel-rgb-array-for-keras-model
            //bitmapPixels[i * 3 + 1] = Color.green(val);
            //bitmapPixels[i * 3 + 2] = Color.blue(val);
        }
        scaledBitmap.setPixels(bitmapIntPixels, 0, scaledBitmap.getWidth(), 0, 0,
                scaledBitmap.getWidth(), scaledBitmap.getHeight());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ImageView imageView = findViewById(R.id.imageView);

            // crops and generates the pixels from the bitmap. The result is in bitmapPixels
            bitmapToPixels(imageBitmap);
            //System.out.println(imageBitmap);
            imageView.setImageBitmap(scaledBitmap);

            final ClassificationResult res = classifier.recognize(bitmapPixels);

            String text = String.format("%s: %s, %f\n", classifier.getName(), res.getLabel(),
                            res.getProbability());

            result.setText(text);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
