package android.duke290.com.loco;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

/**
 * Created by Jihane on 4/29/17.
 */

public class ConfirmDialogFragment extends DialogFragment {
    Class mClass;
    int mRating;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Create an alert dialog to move to the next quiz
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        mRating = getArguments().getInt("rating");

        builder.setMessage("Confirm your rating: " + mRating)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent homeintent = new Intent(getActivity(), MainActivity.class);
                        homeintent.putExtra("TYPE", "dialog");
                        homeintent.putExtra("confirmed", 1);
                        homeintent.putExtra("rating", mRating);
                        homeintent.putExtra("LOCATION_KEY",
                                getArguments().getParcelable("LOCATION_KEY"));
                        homeintent.putStringArrayListExtra("PROCESS_MSGS",
                                getArguments().getStringArrayList("PROCESS_MSGS"));
                        startActivity(homeintent);

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Back to Home Screen
                        Intent homeintent = new Intent(getActivity(), MainActivity.class);
                        homeintent.putExtra("confirmed", 0);
                        startActivity(homeintent);

                    }
                });

        return builder.create();
    }

}
