package com.example.photofy;

import android.util.Log;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Uploads image from local storage to GCS bucket for DetectProperties to obtain the dominant color
 */
public class UploadObject {

    public static final String TAG = "UploadObject";

    public static void uploadObject(Storage storage, String bucketName, String objectName, String filePath) throws IOException {
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        storage.create(blobInfo, Files.readAllBytes(Paths.get(filePath)));

        Log.i(TAG, "File " + filePath + " uploaded to bucket " + bucketName + " as " + objectName);
    }
}