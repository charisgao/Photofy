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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DetectProperties {

    public static final String TAG = "DetectProperties";

    private GoogleCredentials credentials;
    private ImageAnnotatorClient client;

    public DetectProperties(String credentialsFilePath, Context context) {
        try {
            // Authorize Google credentials
            credentials = GoogleCredentials.fromStream(context.getAssets().open(credentialsFilePath))
                    .createScoped(Arrays.asList("https://www.googleapis.com/auth/cloud-platform"));

            // Create annotator client
            ImageAnnotatorSettings imageAnnotatorSettings =
                    ImageAnnotatorSettings.newBuilder()
                            .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                            .build();
            client = ImageAnnotatorClient.create(imageAnnotatorSettings);
        } catch (IOException e) {
            Log.e(TAG, "credentials error" + e);
        }
    }

    public String findDominantColor(Photo picture, String path) {
        try {
            // Upload captured image to Google Cloud storage
            Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
            String bucketName = "photofy-images0";
            String objectName = "image-" + picture.getImage().getName();
            UploadObject.uploadObject(storage, bucketName, objectName, path);

            // GCS path for image from bucket
            String gcsPath = "gs://" + bucketName + "/" + objectName;

            String hexColor = detectPropertiesGcs(gcsPath);
            return hexColor;
        } catch (IOException e) {
            Log.e(TAG, "problem with finding dominant color" + e);
            return "";
        }
    }

    // Detects most dominant color from the specified remote image
    private String detectPropertiesGcs(String gcsPath) throws IOException {
        List<AnnotateImageRequest> requests = new ArrayList<>();

        ImageSource imgSource = ImageSource.newBuilder().setGcsImageUri(gcsPath).build();
        Image img = Image.newBuilder().setSource(imgSource).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.IMAGE_PROPERTIES).setMaxResults(1).build();
        AnnotateImageRequest request =
                AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
        requests.add(request);

        BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
        List<AnnotateImageResponse> responses = response.getResponsesList();
        client.close();

        AnnotateImageResponse res = responses.get(0);
        if (res.hasError()) {
            Log.e(TAG, "error " + res.getError().getMessage());
            return "";
        }

        DominantColorsAnnotation colors = res.getImagePropertiesAnnotation().getDominantColors();
        ColorInfo color = colors.getColorsList().get(0);

        int red = Math.round(color.getColor().getRed());
        int green = Math.round(color.getColor().getGreen());
        int blue = Math.round(color.getColor().getBlue());

        String hex = String.format("#%02X%02X%02X", red, green, blue);
        return hex;
    }
}