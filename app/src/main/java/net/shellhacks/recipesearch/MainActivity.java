package net.shellhacks.recipesearch;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 672;
    private String imageFilePath;
    private Uri photoUri;

    private Vision vision = null;
    private ProgressBar spinner;

    public byte[] byteArray;
    public ArrayList<EntityAnnotation> annots;

    public static final String EXTRA_PICTURE = "net.shellhacks.recipesearch.byteArray";
    public static final String EXTRA_ = "net.shellhacks.recipesearch.byteArray";
    public static final String RECIPE_URL = "";
    public static final String API_KEY = "dcb4a456236a815460124f52f51ed8b9";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.take).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTakePhotoIntent();
            }
        });

        Vision.Builder visionBuilder = new Vision.Builder(
                new NetHttpTransport(),
                new AndroidJsonFactory(),
                null);

        visionBuilder.setVisionRequestInitializer(
                new VisionRequestInitializer("AIzaSyCk3cQWmCNWL1qssZxELEzLziIuYIxUa0Y"));

        vision = visionBuilder.build();
        spinner = (ProgressBar)findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            final Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
            Log.d("[image]", imageFilePath);
            ExifInterface exif = null;

            try {
                exif = new ExifInterface(imageFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            int exifOrientation;
            int exifDegree;

            if (exif != null) {
                exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                exifDegree = exifOrientationToDegrees(exifOrientation);
            } else {
                exifDegree = 0;
            }

            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byteArray = byteArrayOutputStream .toByteArray();
            //((ImageView)findViewById(R.id.photo)).setImageBitmap(rotate(bitmap, exifDegree));
            spinner.setVisibility(View.VISIBLE);

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    Image inputImage = new Image();
                    inputImage.encodeContent(byteArray);

                    Feature desiredFeature = new Feature();
                    desiredFeature.setType("LABEL_DETECTION");

                    AnnotateImageRequest request = new AnnotateImageRequest();
                    request.setImage(inputImage);
                    request.setFeatures(Arrays.asList(desiredFeature));
                    BatchAnnotateImagesRequest batchRequest =
                            new BatchAnnotateImagesRequest();

                    batchRequest.setRequests(Arrays.asList(request));
                    try {
                        BatchAnnotateImagesResponse batchResponse =
                                vision.images().annotate(batchRequest).execute();
                        annots = new ArrayList<>(batchResponse.getResponses()
                                .get(0).getLabelAnnotations());
                        //for(EntityAnnotation l : labels){
                        //    Log.d("test", l.getDescription() + ": " + l.getScore());
                        //}

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    HttpResponse response = null;
                    try {
                        HttpClient client = new DefaultHttpClient();
                        HttpGet hrequest = new HttpGet();
                        String foodName = "pasta";
                        hrequest.setURI(new URI("https://www.food2fork.com/api/search?key=" + API_KEY +"&q=" + foodName));
                        response = client.execute(hrequest);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                        String line;
                        String jsonText = reader.readLine();
                        jsonText = jsonText.substring(jsonText.indexOf("recipe_id"), jsonText.indexOf("image_url"));
                        jsonText = jsonText.substring(jsonText.indexOf('"') + 4, jsonText.lastIndexOf('"') -3);
                        int rid = Integer.parseInt(jsonText);





                        Log.d("[========json=======]", jsonText);
                        Log.d("[========rid=======]", "" + rid);





                        // Getting Information of Food

                        client = new DefaultHttpClient();
                        hrequest = new HttpGet();
                        hrequest.setURI(new URI("https://www.food2fork.com/api/get?key=" + API_KEY +"&rId=" + rid));
                        response = client.execute(hrequest);
                        reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                        jsonText = "";

                        while ((line = reader.readLine()) != null) {
                            jsonText += line;
                        }

                        Log.d("[ingredients]", jsonText);

                        JSONObject obj = new JSONObject(jsonText);
                        obj = new JSONObject(obj.getString("recipe"));
                        String ingred = obj.getString("ingredients");
                        Log.d("[ingredients]", ingred);

                        Intent i = new Intent(MainActivity.this, RecipeActivity.class);
                        i.putExtra(EXTRA_PICTURE, ingred); //annots.get(0).getDescription()
                        startActivity(i);


                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    } catch (ClientProtocolException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });


        }
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


    private void sendTakePhotoIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }

            if (photoFile != null) {
                Log.d("test", getPackageName());
                photoUri = FileProvider.getUriForFile(this, getPackageName(), photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TEST_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,      /* prefix */
                ".jpg",         /* suffix */
                storageDir          /* directory */
        );
        imageFilePath = image.getAbsolutePath();
        return image;
    }


}
