package android.duke290.com.loco.profile;

import android.content.Intent;
import android.duke290.com.loco.R;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Allows the user to change her email/password.
 */
public class ChangeProfileActivity extends AppCompatActivity {

    private EditText mChangetext;
    private EditText mOldEmail;
    private EditText mPassword;
    private String change;
    private String mUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_profile);

        // initialize view elements
        mPassword = (EditText) findViewById(R.id.password_profile);
        mChangetext = (EditText) findViewById(R.id.change_text);

        Toolbar toolbar = (Toolbar) findViewById(R.id.changeprofile_toolbar);
        //toolbar.setTitle("My Profile");

        //get what's changing
        change = getIntent().getStringExtra("change");
        mPassword.setHint("Current Password");
        mPassword.setTypeface(Typeface.DEFAULT);
        mPassword.setTransformationMethod(new PasswordTransformationMethod());
        if (change.equals("email")){
            toolbar.setTitle("E-mail");
            mChangetext.setHint("New e-mail");
        }
        if (change.equals("password")){
            toolbar.setTitle("Password");
            mChangetext.setHint("New Password");
            mChangetext.setTransformationMethod(new PasswordTransformationMethod());
        }

        setSupportActionBar(toolbar);

        // back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * Calls ProfileActivity to indicate the email/password change.
     * @param view - button that activates the method
     */
    public void onConfirmClick(View view) {
        String newinfo = mChangetext.getText().toString().trim();
        if (newinfo.equals("")) {
            mChangetext.setError("Invalid");
        }
        if (change.equals("password") && newinfo.length() < 6) {
            Toast.makeText(ChangeProfileActivity.this, "Password too short, enter minimum 6 characters", Toast.LENGTH_LONG).show();
            mChangetext.setError("Invalid");
        }
        Intent changed = new Intent(ChangeProfileActivity.this, ProfileActivity.class);

        changed.putExtra("oldpassword", mPassword.getText().toString().trim());
        changed.putExtra("newdata", newinfo);
        changed.putExtra("change",change);
        startActivity(changed);
    }
}
