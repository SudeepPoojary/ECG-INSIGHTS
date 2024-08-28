package com.example.ecg_app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private TextView nameTextView, emailTextView;
    private ImageView profilePhotoImageView;
    private Button logoutButton, addPatientButton, viewPatientButton, mapButton;
    private FirebaseDatabase database;
    private FirebaseAuth firebaseAuth;
    private long pressTime;
    private boolean doublePressToExit = false;

    @Override
    public void onBackPressed() {
        if(pressTime + 2000 > System.currentTimeMillis()){
            super.onBackPressed();
            finishAffinity();
        } else {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
        pressTime = System.currentTimeMillis();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if user is signed in (on start of activity)
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            // User is not signed in, redirect to login activity
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        database = FirebaseDatabase.getInstance("https://ecg-app-94475-default-rtdb.asia-southeast1.firebasedatabase.app");

        profilePhotoImageView = (ImageView) findViewById(R.id.profilePhoto);
        nameTextView = (TextView) findViewById(R.id.name);
        emailTextView = (TextView) findViewById(R.id.email);
        logoutButton = (Button) findViewById(R.id.logout_button);
        addPatientButton = (Button) findViewById(R.id.add_patient);
        viewPatientButton = (Button) findViewById(R.id.view_patient);
        mapButton = (Button) findViewById(R.id.map_button);

        firebaseAuth = FirebaseAuth.getInstance();

        //Getting current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
//        googleSignInClient = GoogleSignIn.getClient(MainActivity.this, GoogleSignInOptions.DEFAULT_SIGN_IN);

        if(user == null){
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        } else {
            database.getReference().child("users").child(user.getUid()).get()
                            .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    if(task.isSuccessful()){
                                        if(task.getResult().exists()){
                                            DataSnapshot dataSnapshot = task.getResult();
                                            String name = String.valueOf(dataSnapshot.child("name").getValue());
                                            String email = String.valueOf(dataSnapshot.child("email").getValue());
                                            String profilePhotoUrl = String.valueOf(dataSnapshot.child("profile").getValue());

                                            nameTextView.setText(name);
                                            emailTextView.setText(email);
                                            if(profilePhotoUrl.equals("None")){
                                                Glide.with(MainActivity.this)
                                                        .load(R.drawable.default_profile_photo)
                                                        .circleCrop()
                                                        .into(profilePhotoImageView);
                                            } else {
                                                Glide.with(MainActivity.this)
                                                        .load(profilePhotoUrl)
                                                        .circleCrop()
                                                        .into(profilePhotoImageView);
                                            }
                                        }
                                    }
                                }
                            });
        }

        //Logout from the activity
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        //Add patient details - Go to patient adding page
        addPatientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddPatientActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //View all patients data from the database
        viewPatientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ViewPatientsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Build the Google Search URL with user location
                String url = "https://www.google.com/search?q=nearest+hospitals";

                // Use an Intent to open the URL in a web browser
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });
    }

    private void signOut() {
        firebaseAuth.signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}