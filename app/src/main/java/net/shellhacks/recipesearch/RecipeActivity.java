package net.shellhacks.recipesearch;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.api.services.vision.v1.model.EntityAnnotation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class RecipeActivity extends AppCompatActivity {

    private String subject;
    private String ingredients;
    private String pathToImage;
    private String direction;
    //private ArrayList<EntityAnnotation> annots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);
        subject = getIntent().getStringExtra(MainActivity.EXTRA_SUBJECT);
        Log.d("[test title]", subject);
        ((TextView)findViewById(R.id.title)).setText(Html.fromHtml("<h1>"+subject + "</h1>", Html.FROM_HTML_MODE_LEGACY));
       ingredients = getIntent().getStringExtra(MainActivity.EXTRA_INGREDIENTS).replace("[", "").replace("]", "");
       String[] ingreds = ingredients.split(",\"");
       ingredients = "<h2>Ingredients</h2><ul>";
       for(int i = 0; i < ingreds.length; i ++) {
           ingredients += "<li>" + ingreds[i].replace("\"", "") + "</li>";
       }
       ingredients += "</ul><br/><h2>Direction</h2>";
       direction = getIntent().getStringExtra(MainActivity.EXTRA_URL);
       if(!direction.equals("")) ingredients += "<a href = \""+ direction + "\"> Go To Direction </a>\n I hope you enjoy!";
        ((TextView)findViewById(R.id.description)).setText(Html.fromHtml(ingredients, Html.FROM_HTML_MODE_LEGACY));
        //Log.d("hrf", "<a href = \""+ direction + "\">Go To Direction</a>");
        pathToImage = getIntent().getStringExtra(MainActivity.EXTRA_PATH);

        Bitmap bitmap = BitmapFactory.decodeFile(pathToImage);
        ExifInterface exif = null;

        try {
            exif = new ExifInterface(pathToImage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int exifOrientation;
        int exifDegree;

        if (exif != null) {
            exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            exifDegree = exifOrientationToDegrees(exifOrientation); } else {
            exifDegree = 0; }

            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            ((ImageView)findViewById(R.id.photo)).setImageBitmap(rotate(bitmap, exifDegree));
    }

    private int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    private Bitmap rotate(Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}
