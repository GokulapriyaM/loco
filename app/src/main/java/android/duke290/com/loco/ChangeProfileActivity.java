package android.duke290.com.loco;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;

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
        mChangetext = (EditText) findViewById(R.id.change_text);
        mOldEmail = (EditText) findViewById(R.id.old_email);
        mPassword = (EditText) findViewById(R.id.password_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.changeprofile_toolbar);
        //toolbar.setTitle("My Profile");

        //get what's changing
        change = getIntent().getStringExtra("change");
        if (change.equals("email")){
            toolbar.setTitle("E-mail");
            String oldemail = getIntent().getStringExtra("oldemail");
            mOldEmail.setText(oldemail);
            mChangetext.setHint("New e-mail");
            mPassword.setHint("Current Password");
            mPassword.setTypeface(Typeface.DEFAULT);
            mPassword.setTransformationMethod(new PasswordTransformationMethod());
        }
        if (change.equals("password")){
            toolbar.setTitle("Password");
        }

        setSupportActionBar(toolbar);

        // back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mUsername = getIntent().getStringExtra("username");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void onConfirmClick(View view) {
        String newinfo = mChangetext.getText().toString().trim();
        while (newinfo.equals("")) {
            mChangetext.setError("Enter new e-mail");
            newinfo = mChangetext.getText().toString().trim();
        }
        Intent changed = new Intent(ChangeProfileActivity.this, ProfileActivity.class);
        changed.putExtra("change", change);
        changed.putExtra("oldemail", mOldEmail.getText().toString().trim());
        changed.putExtra("email", newinfo);
        changed.putExtra("username", mUsername);
        changed.putExtra("oldpassword", mPassword.getText().toString().trim());
        startActivity(changed);
    }
}
