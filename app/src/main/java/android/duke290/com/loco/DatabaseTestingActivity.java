package android.duke290.com.loco;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DatabaseTestingActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private Button uploadButton, getButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_testing);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        uploadButton = (Button) findViewById(R.id.Database_testing_upload_button);
        getButton = (Button) findViewById(R.id.Database_testing_get_button);
    }





    protected void databaseTestingUploadClick(View button) {

    }

    protected void databaseTestingGetClick(View button) {

    }
}
