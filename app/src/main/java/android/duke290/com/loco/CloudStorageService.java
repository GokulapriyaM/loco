package android.duke290.com.loco;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

public class CloudStorageService extends IntentService {

    protected String mActionType; // either "upload" or "download"
    protected ResultReceiver mReceiver;
    protected String mLocalFilePath;
    protected String mStoragePath;

    StorageReference mStorageRef;
    StorageReference mDestinationRef;
    String TAG = "CloudStorageService";

    protected String downloaded_msg = "";

    public CloudStorageService() {
        super("CloudStorageService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "onHandleIntent called");

        mActionType = intent.getStringExtra("CLOUD_STORAGE_OPTION");
        mReceiver = intent.getParcelableExtra("CLOUD_STORAGE_RECEIVER");
        mLocalFilePath = intent.getStringExtra("CLOUD_STORAGE_LOCAL_FILE_PATH");
        mStoragePath = intent.getStringExtra("CLOUD_STORAGE_STORAGE_PATH");

        FirebaseStorage storage = FirebaseStorage.getInstance();

        mStorageRef = storage.getReference();

        mDestinationRef = mStorageRef.child(mStoragePath);

        if (mActionType.equals("upload")) {
            uploadFile();
        } else if (mActionType.equals("download")) {
            downloadFile();
        } else {
            deliverResultToReceiver(Constants.FAILURE_RESULT,
                    "No command to upload or download");
        }


    }

    protected void uploadFile() {
        InputStream inputStream = getAssetsFile(mLocalFilePath);

        UploadTask uploadTask = mDestinationRef.putStream(inputStream);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.d(TAG, "Upload failed");
                deliverResultToReceiver(Constants.FAILURE_RESULT, "Upload failed.");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "Upload successful!");
                deliverResultToReceiver(Constants.SUCCESS_RESULT, "Upload successful!");
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                @SuppressWarnings("VisibleForTests")
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                Log.d(TAG, "Upload is " + progress + "% done");
            }
        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "Upload is paused");
            }
        });
    }

    protected void downloadFile() {
        try {
            final File localFile = File.createTempFile("message", "txt");
            mDestinationRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // Local temp file has been created
                    Log.d(TAG, "Download successful!");
                    handleDownloadSuccess(localFile);
                    deliverResultToReceiver(Constants.SUCCESS_RESULT, "Download successful!");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    Log.d(TAG, "Download failed");
                    deliverResultToReceiver(Constants.FAILURE_RESULT, "Download failed.");
                }
            });
        } catch (IOException e) {
            Log.d(TAG, "Cannot create temp file");
        }
    }



    protected void handleDownloadSuccess(File file) {

        // read file, save to downloaded_msg

        try {
            InputStream inputStream = new FileInputStream(file);
            Scanner s = new Scanner(inputStream).useDelimiter("\\A");
            downloaded_msg = s.hasNext() ? s.next() : "";
            Log.d(TAG, "Downloaded message:" + downloaded_msg);

            inputStream.close();

        } catch (IOException e) {
            Log.d(TAG, "InputStream creation from downloaded file failed");
        }
    }

    protected InputStream getAssetsFile(String file_path) {
        AssetManager assetManager = getAssets();

        InputStream inputStream = null;

        try {
            inputStream = assetManager.open("Text/message.txt");
        } catch (IOException e) {
            Log.d(TAG, "Assets file not found");
        }

        if (inputStream != null) {
            Log.d(TAG, "Assets file found!");
        }

        return inputStream;

    }

    private void deliverResultToReceiver(int resultCode, String process_message) {
        Bundle bundle = new Bundle();
        bundle.putString("CLOUD_PROCESS_MSG_KEY", process_message);
        bundle.putString("CLOUD_DOWNLOADED_MSG_KEY", downloaded_msg);
        mReceiver.send(resultCode, bundle);
    }

}
