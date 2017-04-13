package android.duke290.com.loco;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;
import com.google.firebase.storage.UploadTask;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

public class CloudStorageService extends IntentService {

    protected String mActionType; // either "upload" or "download"
    protected ResultReceiver mReceiver;
    protected InputStream mLocalStream;
    protected String mStoragePath;
    protected String mContentType;

    StorageReference mStorageRef;
    StorageReference mDestinationRef;
    String TAG = "CloudStorageService";

    protected InputStream mDownloadedStream;
    protected String mDownloadedContentType;

    byte[] dwnld_b_ar;
    int byte_array_conversion_complete = 0;

    public CloudStorageService() {
        super("CloudStorageService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "onHandleIntent called");

        mActionType = intent.getStringExtra("CLOUD_STORAGE_OPTION");
        mReceiver = intent.getParcelableExtra("CLOUD_STORAGE_RECEIVER");
        if (intent.getByteArrayExtra("CLOUD_STORAGE_LOCAL_BYTE_ARRAY") != null) {
            mLocalStream = new ByteArrayInputStream(
                    intent.getByteArrayExtra("CLOUD_STORAGE_LOCAL_BYTE_ARRAY"));
        }
        mStoragePath = intent.getStringExtra("CLOUD_STORAGE_STORAGE_PATH");
        mContentType = intent.getStringExtra("CLOUD_STORAGE_CONTENT_TYPE");

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

        // add metadata (content type)
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType(mContentType)
                .build();

        UploadTask uploadTask = mDestinationRef.putStream(mLocalStream, metadata);

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
        mDestinationRef.getStream().addOnSuccessListener(new OnSuccessListener<StreamDownloadTask.TaskSnapshot>() {
            @Override @SuppressWarnings("VisibleForTests")
            public void onSuccess(StreamDownloadTask.TaskSnapshot taskSnapshot) {
                // Local temp file has been created
                Log.d(TAG, "Download of file successful!");
                mDownloadedStream = taskSnapshot.getStream();

                Log.d(TAG, "Downloading file metadata");
                downloadFileContentType();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.d(TAG, "Download failed");
                deliverResultToReceiver(Constants.FAILURE_RESULT, "Download failed.");
            }
        });
    }

    protected void downloadFileContentType() {
        mDestinationRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                mDownloadedContentType = storageMetadata.getContentType();

                Log.d(TAG, "Download file and metadata successful");
                deliverResultToReceiver(Constants.SUCCESS_RESULT,
                        "Download completely successful!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "Download metadata failed");

                deliverResultToReceiver(Constants.FAILURE_RESULT,
                        "Download of file successful but download of metadata failed");
            }
        });
    }

    private void deliverResultToReceiver(int resultCode,
                                         String process_message) {
        if (mDownloadedStream != null && mActionType.equals("download")) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        dwnld_b_ar = IOUtils.toByteArray(mDownloadedStream);
                        Log.d(TAG, "size of dwnld_b_ar = " + dwnld_b_ar.length);
                        byte_array_conversion_complete = 1;
                    } catch (IOException e) {
                        Log.d(TAG, "IOException when converting downloaded input stream to byte array");
                    }
                }
            });

            while (byte_array_conversion_complete == 0) {
                // for some reason, putting a statement here fixed a problem where the app
                // would hang up and seem to get stuck even after completing byte array conversion
                Log.d(TAG, "converting");
            }
        }

        Bundle bundle = new Bundle();
        bundle.putString("CLOUD_PROCESS_MSG_KEY", process_message);
        bundle.putByteArray("CLOUD_DOWNLOADED_BYTE_ARRAY_KEY", dwnld_b_ar);
        bundle.putString("CLOUD_DOWNLOADED_CONTENT_TYPE", mDownloadedContentType);
        mReceiver.send(resultCode, bundle);
    }

}
