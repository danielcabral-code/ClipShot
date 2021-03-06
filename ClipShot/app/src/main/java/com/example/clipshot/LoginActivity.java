package com.example.clipshot;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements
        View.OnClickListener {

    // Declaring variables from Firebase and Google SignIn and progress bar
    private static final int RC_SIGN_IN = 123;
    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    ProgressBar progressBar;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Call Login TopBar
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.login_action_bar_layout);

        // Set Elevation to TopBar
        getSupportActionBar().setElevation(20f); // Float == px

        findViewById(R.id.sign_in_button).setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progress_circular);

        // Configure sign-in to request the user's ID, email address, and basic profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Applying Button SignIn style
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setColorScheme(SignInButton.COLOR_LIGHT);

        // If an account is logged in with Firebase the user will skip the Login Page and go to the Feed Page (Main Activity/Feed Fragment)
        if (mAuth.getCurrentUser() != null) {
            FirebaseUser user = mAuth.getCurrentUser();
            updateUI(user);
        }
    }

    // Sign In function that will open dialog to choose account
    private void signIn() {
        progressBar.setVisibility(View.VISIBLE);
        signOut();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

   @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                // Firebase Authentication with Google Account
                GoogleSignInAccount account = task.getResult(ApiException.class);

                if (account != null) {
                    firebaseAuthWithGoogle(account);
                } else{
                    Toast.makeText(LoginActivity.this, "Sign-in failed, try again later.", Toast.LENGTH_LONG).show();
                }
            } catch (ApiException e) {
                Toast.makeText(LoginActivity.this, "Sign-in failed, try again later.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Firebase Google Authentication Method
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this,task -> {

            if (task.isSuccessful()) {
                progressBar.setVisibility(View.INVISIBLE);

                boolean newuser = Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getAdditionalUserInfo()).isNewUser();

                if (newuser) {

                    // If it is a new user it will appear the Welcome Page
                    Intent intent = new Intent(LoginActivity.this, WelcomeActivity.class);
                    startActivity(intent);

                }else {

                    // If it is an existing user it will appear the Feed Page
                    FirebaseUser user = mAuth.getCurrentUser();
                    updateUI(user);
                }
            }
            else {
                // If Login fails it will appear an toast
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(this, "SignIn Failed!", Toast.LENGTH_SHORT).show();
                updateUI(null);
            }
        });
    }

    // Method to go to Feed Page
    private void updateUI(FirebaseUser user) {

        if (user != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    // Sign In Button onClick Method
    public void onClick(View v) {

        if (v.getId() == R.id.sign_in_button) {
            signIn();
        }
    }

    // Sign Out Method
    public void signOut() {

        // Firebase sign out
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                task -> updateUI(null));
    }

    // Not calling **super**, disables back button in current screen.
    @Override
    public void onBackPressed() {
        // super.onBackPressed();
    }
}