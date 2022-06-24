package com.example.photofy;

import static com.example.photofy.PhotofyApplication.googleCredentials;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.ArrayRes;

import com.example.photofy.activities.MainActivity;
import com.example.photofy.fragments.SongRecommendationsFragment;
import com.example.photofy.models.Photo;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.ColorInfo;
import com.google.cloud.vision.v1.DominantColorsAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import com.google.cloud.vision.v1.ImageSource;
import com.google.cloud.vision.v1.LocalizedObjectAnnotation;
import com.parse.ParseException;
import com.parse.SaveCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DetectProperties {

    public static final String TAG = "DetectProperties";

    private Context context;
    private Photo picture;
    private String path;
    private String spotifyToken;
    private GoogleCredentials credentials;
    private List<String> objects;

    public DetectProperties(Photo picture, String path, String token, Context context) {
        this.picture = picture;
        this.context = context;
        this.path = path;
        spotifyToken = token;
        objects = new ArrayList<String>();

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    authExplicit(googleCredentials, context);
                } catch (Exception e) {
                    Log.e(TAG, "Credentials error " + e);
                }
            }
        });

        thread.start();
    }

    public void authExplicit(String jsonPath, Context context) throws IOException {
        // Authorize Google credentials
        credentials = GoogleCredentials.fromStream(context.getAssets().open(jsonPath))
                .createScoped(Arrays.asList("https://www.googleapis.com/auth/cloud-platform"));
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

        // Upload captured image to Google Cloud storage
        String bucketName = "photofy-images0";
        String objectName = "image-" + picture.getImage().getName();
        UploadObject.uploadObject(storage, bucketName, objectName, path);

        // Get GCS path for image from bucket
        String gcsPath = "gs://" + bucketName + "/" + objectName;
//        //TESTING
//        String gcsPath = "gs://cloud-samples-data/vision/image_properties/bali.jpeg";
        detectPropertiesGcs(gcsPath);
    }

    // Detects most dominant color from the specified remote image
    public void detectPropertiesGcs(String gcsPath) throws IOException {
        List<AnnotateImageRequest> requests = new ArrayList<>();

        ImageSource imgSource = ImageSource.newBuilder().setGcsImageUri(gcsPath).build();
        Image img = Image.newBuilder().setSource(imgSource).build();
        Feature feat1 = Feature.newBuilder().setType(Feature.Type.IMAGE_PROPERTIES).setMaxResults(1).build();
        AnnotateImageRequest request1 =
                AnnotateImageRequest.newBuilder().addFeatures(feat1).setImage(img).build();
        requests.add(request1);

        ImageAnnotatorSettings imageAnnotatorSettings =
                ImageAnnotatorSettings.newBuilder()
                        .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                        .build();

        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the "close" method on the client to safely clean up any remaining background resources.
        try (ImageAnnotatorClient client = ImageAnnotatorClient.create(imageAnnotatorSettings)) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();
            client.close();

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    System.out.format("Error: %s%n", res.getError().getMessage());
                    return;
                }

                DominantColorsAnnotation colors = res.getImagePropertiesAnnotation().getDominantColors();
                ColorInfo color = colors.getColorsList().get(0);

                int red = Math.round(color.getColor().getRed());
                int green = Math.round(color.getColor().getGreen());
                int blue = Math.round(color.getColor().getBlue());

                // Save dominant color hex code in Parse
                String hex = String.format("#%02X%02X%02X", red, green, blue);
                picture.setColor(hex);
                Log.i(TAG, "generated color " + hex);
                picture.save();
                goToRecommendationsFragment();
            }
        } catch (ParseException e) {
            Log.e(TAG, "color error " + e);
        }
    }

    private void goToRecommendationsFragment() {
        SongRecommendationsFragment songRecommendationsFragment = new SongRecommendationsFragment();
        Bundle songBundle = new Bundle();
        songBundle.putParcelable("photo", picture);
        songBundle.putString("token", spotifyToken);
        songRecommendationsFragment.setArguments(songBundle);
        ((MainActivity) context).getSupportFragmentManager().beginTransaction()
                .replace(R.id.flContainer, songRecommendationsFragment)
                .addToBackStack(null)
                .commit();
    }
}