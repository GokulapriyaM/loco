package android.duke290.com.loco;

import android.content.Intent;
import android.duke290.com.loco.database.DatabaseFetch;
import android.duke290.com.loco.database.DatabaseFetchCallback;
import android.duke290.com.loco.registration.LoginActivity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity implements DatabaseFetchCallback {

    private TextView mUsernameView;
    private TextView mEmailView;
    private RelativeLayout mSignout;
    private RelativeLayout mChangeEmail;
    private RelativeLayout mChangePassword;

    private String mUsername;
    private String mEmail;
    private String mPassword;
    private String mOldEmail;
    private String mNewPassword;
    private User currentUser;

    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    FirebaseUser mUser;
    private DatabaseReference mDatabase;

    private DatabaseFetch databaseFetch;

    private static final String USERS = "users";
    private static final String USERINFO = "userinfo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("My Profile");
        setSupportActionBar(toolbar);
        // back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // initializing views on profile
        mUsernameView = (TextView) findViewById(R.id.username);
        mEmailView = (TextView) findViewById(R.id.email_field);
        mSignout = (RelativeLayout) findViewById(R.id.signout_view);
        mChangeEmail = (RelativeLayout) findViewById(R.id.email_view);
        mChangePassword = (RelativeLayout) findViewById(R.id.password_view);
        currentUser = new User();

        // getting username and email
        mUsername = getIntent().getStringExtra("username");
        mEmail = getIntent().getStringExtra("email");
        mPassword = getIntent().getStringExtra("oldpassword");
        mOldEmail = getIntent().getStringExtra("oldemail");
        mNewPassword = getIntent().getStringExtra("email");
        String change = getIntent().getStringExtra("change");

        mUsernameView.setText(mUsername);
        if (change == null || change != null && change.equals("email"))mEmailView.setText(mEmail);
        else mEmailView.setText(mOldEmail);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();

        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mUser = user;
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        mChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailchange = new Intent(ProfileActivity.this, ChangeProfileActivity.class);
                emailchange.putExtra("change", "email");
                emailchange.putExtra("oldemail", mEmail);
                emailchange.putExtra("username", mUsername);
                startActivity(emailchange);
            }
        });

        mChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent passwordchange = new Intent(ProfileActivity.this, ChangeProfileActivity.class);
                passwordchange.putExtra("oldemail", mEmail);
                passwordchange.putExtra("change", "password");
                passwordchange.putExtra("username", mUsername);
                startActivity(passwordchange);
            }
        });

        mSignout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        if (change !=null) reauthenticateUser(change);


    }

    public void reauthenticateUser(final String change){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    // Get auth credentials from the user for re-authentication. The example below shows
        AuthCredential credential = EmailAuthProvider
                .getCredential(mOldEmail, mPassword);

    // Prompt the user to re-provide their sign-in credentials
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("", "User re-authenticated.");
                        mDatabase = FirebaseDatabase.getInstance().getReference();
                        if (change != null && change.equals("password")) {
                            updatePasswordInfo();
                        }
                        if (change != null && change.equals("email")) {
                            updateEmailInfo();
                        }

                    }
                });
    }

    private void updatePasswordInfo() {
        // update authentication
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && !mNewPassword.equals("")) {
                user.updatePassword(mNewPassword)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ProfileActivity.this, "Password is updated", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(ProfileActivity.this, "Failed to update password!", Toast.LENGTH_LONG).show();
                                    Log.w("Except", task.getException());
                                }
                            }
                        });
        }
    }

    private void updateEmailInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && !mEmail.equals("")) {
            user.updateEmail(mEmail)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ProfileActivity.this, "Email address is updated", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(ProfileActivity.this, "Failed to update email!", Toast.LENGTH_LONG).show();
                                Log.w("Except", task.getException());
                            }
                        }
                    });
        }

        // update database
        User new_user_entry = new User(mUsername, mEmail);
        mDatabase.child(USERS).child(user.getUid()).child(USERINFO).setValue(new_user_entry);
    }


    //sign out method
    public void signOut() {
        auth.signOut();
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }


    @Override
    public void onDatabaseResultReceived(ArrayList<Creation> creations) {

    }

    @Override
    public void onUserReceived(User user) {
        currentUser = user;
    }
}



