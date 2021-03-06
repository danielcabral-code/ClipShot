package com.example.clipshot;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.youtube.player.YouTubeIntents;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import static android.app.PendingIntent.getActivity;

public class MainActivity extends AppCompatActivity {

    // Global Variables
    private static final int PICK_VIDEO =2;
    Uri videoUri;
    String email;
    String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Google variable to detect the user that is signed
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);

        // Variables that will get the email and userId value from the user google account
        assert acct != null;
        email = acct.getEmail();

        // Call Feed TopBar
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.feed_action_bar_layout);

        // Set Elevation to TopBar and NavBar
        getSupportActionBar().setElevation(20f); // Float == px
        BottomAppBar bottomAppBar = findViewById(R.id.navigation);
        bottomAppBar.setElevation(20f); // Float == px

        // Opens Feed page by default
        openFragment(FeedFragment.newInstance("",""));
        // Opacity changes on Bottom Bar Icon depending on what page is selected
        AppCompatImageView iconHome = findViewById(R.id.iconHome);
        iconHome.setAlpha((float) 1.0);
        AppCompatImageView iconProfile = findViewById(R.id.iconProfile);
        iconProfile.setAlpha((float) 0.45);

        // Button to add video
        FloatingActionButton addVideo = findViewById(R.id.addClipButton);
        addVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickVideo();
            }
        });
    }

    @SuppressLint("WrongConstant")
    public void goToFollowing(View v) {

        openFragment(FollowingFragment.newInstance("",""));

        // Call Follow TopBar
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.follow_action_bar);
        getSupportActionBar().setElevation(0f); // Float == px

        // Firebase variables
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Document reference of user data that will read user data
        DocumentReference documentReference = db.collection("users").document(userUid);

        AppCompatTextView profileNameBar = findViewById(R.id.appBarTitle);

        // Access user document and if it exists set the topbar name with the user nickname
        documentReference.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()){

                        String userName  =documentSnapshot.getString("Username");
                        profileNameBar.setText(userName);
                    }
                });
    }

    @SuppressLint("WrongConstant")
    public void goToFollowers(View v) {

        openFragment(FollowersFragment.newInstance("",""));

        // Call Follow TopBar
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.follow_action_bar);
        getSupportActionBar().setElevation(0f); // Float == px

        // Firebase variables
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Document reference of user data that will read user data
        DocumentReference documentReference = db.collection("users").document(userUid);

        AppCompatTextView profileNameBar = findViewById(R.id.appBarTitle);

        // Access user document and if it exists set the topbar name with the user nickname
        documentReference.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()){
                        String userName  =documentSnapshot.getString("Username");
                        profileNameBar.setText(userName);
                    }
                });
    }

    // Go To Feed (NavBar Button)
    @SuppressLint("WrongConstant")
    public void goToFeed(View v) {

        // Call Feed TopBar
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.feed_action_bar_layout);
        getSupportActionBar().setElevation(20f); // Float == px

        openFragment(FeedFragment.newInstance("",""));
        // Opacity changes on Bottom Bar Icon depending on what page is selected
        AppCompatImageView iconHome = findViewById(R.id.iconHome);
        iconHome.setAlpha((float) 1.0);
        AppCompatImageView iconProfile = findViewById(R.id.iconProfile);
        iconProfile.setAlpha((float) 0.45);
    }

    // Go To Profile (NavBar Button)
    @SuppressLint("WrongConstant")
    public void goToProfile(View v) {

        Objects.requireNonNull(getSupportActionBar()).setElevation(20f); // Float == px

        // Google variable to detect the user that is signed
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        // Firebase variables
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference documentReference;

        // Variables that will get the userId value from the user google account
        String userUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // Document reference of user data that will read user data
        documentReference = db.collection("users").document(userUid);

        // Call Profile TopBar
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.profile_action_bar_layout);
        openFragment(ProfileFragment.newInstance("",""));

        // Opacity changes on Bottom Bar Icon depending on what page is selected
        AppCompatImageView iconProfile = findViewById(R.id.iconProfile);
        iconProfile.setAlpha((float) 1.0);
        AppCompatImageView iconHome = findViewById(R.id.iconHome);
        iconHome.setAlpha((float) 0.45);
        AppCompatTextView profileNameBar = findViewById(R.id.appBarTitle);

        // Access user document and if it exists set the topbar name with the user nickname
        documentReference.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()){
                        String userName = documentSnapshot.getString("Username");
                        profileNameBar.setText(userName);
                    }
                });
    }

    // Go To Settings (TopBar Button)
    @SuppressLint("WrongConstant")
    public void goToSettings(View v) {

        // Call Settings TopBar
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.settings_action_bar_layout);
        getSupportActionBar().setElevation(20f); // Float == px

        openFragment(SettingsFragment.newInstance("",""));
        // Opacity changes on Bottom Bar Icon depending on what page is selected
        AppCompatImageView iconProfile = findViewById(R.id.iconProfile);
        iconProfile.setAlpha((float) 1.0);
        AppCompatImageView iconHome = findViewById(R.id.iconHome);
        iconHome.setAlpha((float) 0.45);
    }

    // Utilitary method for opening fragments
    public void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // Method to pick video from smartphone
    private void pickVideo(){
        Intent gallery = new Intent(Intent.ACTION_PICK);
        gallery.setType("video/*");
        startActivityForResult(gallery, PICK_VIDEO);
    }

    // Recieves video from gallery and sends data to UploadActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_VIDEO && resultCode == RESULT_OK && data != null) {

            videoUri = data.getData();
            Intent upload = new Intent(this,UploadVideoActivity.class);
            upload.putExtra("video", videoUri.toString());
            upload.putExtra("userID",userUid);
            startActivity(upload);
        }
    }

    // Not calling **super**, disables back button in current screen.
    @Override
    public void onBackPressed() {
    // super.onBackPressed();
    }
}