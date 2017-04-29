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
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Create an alert dialog to move to the next quiz
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.confirmation)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Back to Home Screen
                        Intent homeintent = new Intent(getActivity(), MainActivity.class);
                        startActivity(homeintent);

                    }
                });

        return builder.create();
    }
}
