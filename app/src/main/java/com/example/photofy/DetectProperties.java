package com.example.photofy;

import static com.example.photofy.PhotofyApplication.googleCredentials;

import android.content.Context;
import android.util.Log;

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
    private GoogleCredentials credentials;

    public DetectProperties(Photo picture, String path, Context context) {
        this.picture = picture;
        this.context = context;
        this.path = path;

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
        detectPropertiesGcs(gcsPath);
    }

    // Detects most dominant color from the specified remote image
    public void detectPropertiesGcs(String gcsPath) throws IOException {
        List<AnnotateImageRequest> requests = new ArrayList<>();

        ImageSource imgSource = ImageSource.newBuilder().setGcsImageUri(gcsPath).build();
        Image img = Image.newBuilder().setSource(imgSource).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.IMAGE_PROPERTIES).setMaxResults(1).build();
        AnnotateImageRequest request =
                AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
        requests.add(request);

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

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    System.out.format("Error: %s%n", res.getError().getMessage());
                    return;
                }

                DominantColorsAnnotation colors = res.getImagePropertiesAnnotation().getDominantColors();
                for (ColorInfo color : colors.getColorsList()) {

                    int red = Math.round(color.getColor().getRed());
                    int green = Math.round(color.getColor().getGreen());
                    int blue = Math.round(color.getColor().getBlue());

                    // Save dominant color hex code in Parse
                    String hex = String.format("#%02X%02X%02X", red, green, blue);
                    picture.setColor(hex);
                    picture.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                        }
                    });

                    Log.i(TAG, "generated color " + hex);
                }
            }
        }
    }
}