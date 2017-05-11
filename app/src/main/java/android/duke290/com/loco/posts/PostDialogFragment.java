package android.duke290.com.loco.posts;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.duke290.com.loco.RatingDialogFragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.duke290.com.loco.R;
import android.widget.EditText;
import android.widget.RatingBar;

public class PostDialogFragment extends DialogFragment {

    final static String TAG = "PostDialogFragment";

    /* The activity that creates an instance of this dialog fragment must
    * implement this interface in order to receive event callbacks.
    * Each method passes the DialogFragment in case the host needs to query it. */
    public interface PostDialogListener {
        public void onPostDialogPositiveClick(DialogFragment dialog, String post);
        public void onPostDialogNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    PostDialogFragment.PostDialogListener mListener;

    final static String CONFIRM = "Confirm";
    final static String CANCEL = "Cancel";

    public PostDialogFragment() {

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_post_dialog, null);
        final EditText post = (EditText) v.findViewById(R.id.user_post);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(v)
                // Add action buttons
                .setPositiveButton(CONFIRM, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the positive button event back to the host activity
                        Log.d(TAG, "Post: " + post.getText().toString());
                        mListener.onPostDialogPositiveClick(PostDialogFragment.this, post.getText().toString());
                    }
                })
                .setNegativeButton(CANCEL, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        mListener.onPostDialogNegativeClick(PostDialogFragment.this);
                    }
                });
        return builder.create();
    }

    // Override the Fragment.onAttach() method to instantiate the PostDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity a = null;

        if (context instanceof Activity){
            a=(Activity) context;
        }
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (PostDialogFragment.PostDialogListener) a;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(a.toString()
                    + " must implement PostDialogListener");
        }
    }
}
