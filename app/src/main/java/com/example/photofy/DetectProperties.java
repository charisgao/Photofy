package com.example.photofy;

import static com.example.photofy.PhotofyApplication.googleCredentials;

import android.content.Context;
import android.util.Log;

import com.example.photofy.models.Photo;
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
import com.google.cloud.vision.v1.ImageSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DetectProperties implements Runnable {

    public static final String TAG = "DetectProperties";

    private Context context;
    private Photo picture;

    private AtomicBoolean alive = new AtomicBoolean(true);

    public DetectProperties(Photo picture, Context context) {
        this.picture = picture;
        this.context = context;
        Log.d(TAG," Thread Started");
    }

    public void authExplicit(String jsonPath, Context context) throws IOException {
//        GoogleCredentials credential = GoogleCredentials.getApplicationDefault();

        GoogleCredentials credentials = GoogleCredentials.fromStream(context.getAssets().open(jsonPath))
                .createScoped(Arrays.asList("https://www.googleapis.com/auth/cloud-platform"));
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the "close" method on the client to safely clean up any remaining background resources.
        ImageAnnotatorClient client = ImageAnnotatorClient.create();

        String filePath = picture.getImage().getUrl();
        detectPropertiesGcs(filePath, client);
    }

    @Override
    public void run() {
        try {
            authExplicit(googleCredentials, context);
        } catch (IOException e) {
            Log.e(TAG, "Credentials error " + e);
        }
    }

    public void quit(){
        alive.set(false);
    }

    public void makeReady(){
        alive.set(true);
    }

    // Detects image properties such as color frequency from the specified remote image
    public static void detectPropertiesGcs(String gcsPath, ImageAnnotatorClient client) throws IOException {
        List<AnnotateImageRequest> requests = new ArrayList<>();

        ImageSource imgSource = ImageSource.newBuilder().setGcsImageUri(gcsPath).build();
        Image img = Image.newBuilder().setSource(imgSource).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.IMAGE_PROPERTIES).setMaxResults(3).build();
        AnnotateImageRequest request =
                AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
        requests.add(request);

        BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
        List<AnnotateImageResponse> responses = response.getResponsesList();

        for (AnnotateImageResponse res : responses) {
            if (res.hasError()) {
                System.out.format("Error: %s%n", res.getError().getMessage());
                return;
            }

            DominantColorsAnnotation colors = res.getImagePropertiesAnnotation().getDominantColors();
            for (ColorInfo color : colors.getColorsList()) {
                System.out.format(
                        "fraction: %f%nr: %f, g: %f, b: %f%n",
                        color.getPixelFraction(),
                        color.getColor().getRed(),
                        color.getColor().getGreen(),
                        color.getColor().getBlue());
            }
        }
        client.close();
    }
}