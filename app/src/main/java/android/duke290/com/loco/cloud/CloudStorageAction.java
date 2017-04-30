package android.duke290.com.loco.cloud;

import android.content.Context;
import android.duke290.com.loco.location.Constants;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CloudStorageAction {

    protected Context mContext;
    protected String mActionType; // either "upload" or "download"
    protected ResultReceiver mReceiver;
    protected InputStream mLocalStream;
    protected String mStoragePath;
    protected String mContentType;

    StorageReference mStorageRef;
    StorageReference mDestinationRef;
    String TAG = "CloudStorageAction";

    protected InputStream mDownloadedStream;
    protected String mDownloadedContentType;

    protected int mResultCode;
    protected String mProcessMsg;

    String stg_filename = null;

    public CloudStorageAction(Context context, String action_type,
                              ResultReceiver receiver, InputStream inputstream_to_store,
                              String storage_path, String content_type) {
        mContext = context;
        mActionType = action_type;
        mReceiver = receiver;
        mLocalStream = inputstream_to_store;
        mStoragePath = storage_path;
        mContentType = content_type;
    }

    public void doCloudAction() {

        Log.d(TAG, "doCloudAction");

        FirebaseStorage storage = FirebaseStorage.getInstance();

        mStorageRef = storage.getReference();

        mDestinationRef = mStorageRef.child(mStoragePath);

        if (mActionType.equals("upload")) {
            uploadFile();
        } else if (mActionType.equals("download")) {
            downloadFile();
        } else {
            mResultCode = Constants.FAILURE_RESULT;
            mProcessMsg = "No command to upload or download";
            deliverResultToReceiver();
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
                mResultCode = Constants.FAILURE_RESULT;
                mProcessMsg = "Upload failed.";
                deliverResultToReceiver();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "Upload successful!");
                mResultCode = Constants.SUCCESS_RESULT;
                mProcessMsg = "Upload successful!";
                deliverResultToReceiver();
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
                if (mDownloadedStream != null) {
                    Log.d(TAG, "mDownloadedStream already being used");
                }
                mDownloadedStream = taskSnapshot.getStream();

                Log.d(TAG, "Downloading file metadata");
                downloadFileContentType();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.d(TAG, "Download failed");

                mResultCode = Constants.FAILURE_RESULT;
                mProcessMsg = "Download failed";

                deliverResultToReceiver();
            }
        });
    }

    protected void downloadFileContentType() {
        mDestinationRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                mDownloadedContentType = storageMetadata.getContentType();

                Log.d(TAG, "Download file and metadata successful");

                mResultCode = Constants.SUCCESS_RESULT;
                mProcessMsg = "Download completely successful!";

                deliverResultToReceiver();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "Download metadata failed");

                mResultCode = Constants.FAILURE_RESULT;
                mProcessMsg = "Download of file successful but download of metadata failed";

                deliverResultToReceiver();
            }
        });
    }

    private void deliverResultToReceiver() {
        if (mDownloadedStream != null && mActionType.equals("download")) {
            stg_filename = "stg" + System.currentTimeMillis();
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    File dir = mContext.getDir("downloaded_storage_items", Context.MODE_PRIVATE);

                    if (!dir.exists()) {
                        dir.mkdirs();
                    }

                    File stg_file = new File(mContext.getDir("downloaded_storage_items", Context.MODE_PRIVATE),
                            stg_filename);

                    Log.d(TAG, "writing to : " + stg_file.getPath());

                    try {
                        FileOutputStream os = new FileOutputStream(stg_file);

                        byte[] buffer = new byte[1024];
                        int len = mDownloadedStream.read(buffer);
                        Log.d(TAG, "len = " + len);
                        while (len >= 0) {
                            os.write(buffer, 0, len);
                            len = mDownloadedStream.read(buffer);
                        }

                        os.close();
                        os.flush();
                        os.close();

                        Log.d(TAG, "outputstream written to");
                        Log.d(TAG, "Write to internal storage done");
                    } catch (IOException e) {
                        Log.d(TAG, "IOException while writing to outputstream: " + e.getMessage());
                    }

                    Bundle bundle = new Bundle();
                    bundle.putString("CLOUD_PROCESS_MSG_KEY", mProcessMsg);
                    bundle.putString("CLOUD_ACTION_TYPE", mActionType);
                    bundle.putString("CLOUD_DOWNLOADED_FILENAME", stg_filename);
                    bundle.putString("CLOUD_DOWNLOADED_CONTENT_TYPE", mDownloadedContentType);
                    mReceiver.send(mResultCode, bundle);

                    Log.d(TAG, "receiver sent");
                }
            });

        } else {

            Bundle bundle = new Bundle();
            bundle.putString("CLOUD_PROCESS_MSG_KEY", mProcessMsg);
            bundle.putString("CLOUD_ACTION_TYPE", mActionType);
            mReceiver.send(mResultCode, bundle);

            Log.d(TAG, "receiver sent");
        }


    }
}
