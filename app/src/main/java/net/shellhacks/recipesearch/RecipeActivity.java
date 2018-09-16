package net.shellhacks.recipesearch;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.api.services.vision.v1.model.EntityAnnotation;

import java.util.ArrayList;

public class RecipeActivity extends AppCompatActivity {

    private String byteArray;
    //private ArrayList<EntityAnnotation> annots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);
       byteArray = getIntent().getStringExtra(MainActivity.EXTRA_PICTURE);
        ((TextView)findViewById(R.id.description)).setText(byteArray);
      // annots = getIntent().getParcelableExtra(MainActivity.EXTRA_ANNOTATIONS);
       //Log.d("wow", annots.get(0).getDescription());
    }
}
