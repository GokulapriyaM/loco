package android.duke290.com.loco.registration;

import android.content.Intent;
import android.duke290.com.loco.R;
import android.duke290.com.loco.database.User;
import android.duke290.com.loco.discover.MainActivity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * Allows the user to make a Loco account.
 */
public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";

    private static final String USERS = "users";
    private static final String USERINFO = "userinfo";


    private EditText inputEmail, inputName, inputPassword;
    private Button signinButton, signupButton, resetButton;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //Get Firebase mAuth instance
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        signinButton = (Button) findViewById(R.id.sign_in_button);
        signupButton = (Button) findViewById(R.id.sign_up_button);
        inputEmail = (EditText) findViewById(R.id.email);
        inputName = (EditText) findViewById(R.id.name);
        inputPassword = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = inputEmail.getText().toString().trim();
                String name = inputName.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(),getString(R.string.enter_email), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(getApplicationContext(),getString(R.string.enter_name), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.enter_password), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), getString(R.string.minimum_password), Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                createUser(email, name, password);

            }
        });
    }

    /**
     * Registers the user with Firebase Authentication.
     * @param email - user's email
     * @param username - user's username
     * @param password - user's password
     */
    private void createUser(String email, String username, String password){
        final String name = username;
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUser:onComplete:" + task.isSuccessful());
                        progressBar.setVisibility(View.GONE);
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the mAuth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignupActivity.this, "Authentication failed." + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            onAuthSuccess(task.getResult().getUser(), name);
                        }
                    }
                });
    }

    /**
     * Called when user successfully authenticates with Firebase Authentication.
     * @param user - user that Firebase Authentication sees
     * @param name - user's username
     */
    private void onAuthSuccess(FirebaseUser user, String name) {
        // Write new user
        writeNewUser(user.getUid(), name, user.getEmail());

        // Go to MainActivity
        startActivity(new Intent(SignupActivity.this, MainActivity.class));
        finish();
    }

    /**
     * Adds the user to Firebase Database.
     * @param userId - ID of the user given by Firebase Authentication.
     * @param name - user's username
     * @param email - user's email
     */
    // [START basic_write]
    private void writeNewUser(String userId, String name, String email) {
        User user = new User(name, email);
        mDatabase.child(USERS).child(userId).child(USERINFO).setValue(user);
    }
    // [END basic_write]

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }
}