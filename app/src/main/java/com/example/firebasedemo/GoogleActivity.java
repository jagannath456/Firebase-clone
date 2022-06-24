package com.example.firebasedemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class GoogleActivity extends AppCompatActivity  {
    private SignInButton signInButton;
    private AppCompatButton btnSignOut, btnGetProfileInfo;
    private TextView txtProfileInfo;
    private GoogleSignManager googleSignInManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google);

        signInButton = findViewById(R.id.btnGoogleSignIn);
        btnSignOut = findViewById(R.id.btnSignOut);
        btnGetProfileInfo = findViewById(R.id.btnGetProfileInfo);
        txtProfileInfo = findViewById(R.id.txtProfileInfo);

        googleSignInManager = GoogleSignManager.getInstance(this);
        googleSignInManager.setUpGoogleSignInOption();

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignInManager.signIn();
            }
        });

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignInManager.signOut();
                txtProfileInfo.setText("");
            }
        });

        btnGetProfileInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser account = googleSignInManager.getProfileInfo();

                if(account != null){
                    String PersonName = account.getDisplayName();
                    String PersonEmail = account.getEmail();
                    Uri PersonPhoto = account.getPhotoUrl();

                    String profileInfo = "Name : " + PersonName + "\n" + "Email : " + "\n" + PersonEmail + "Photo : " + "\n" + PersonPhoto;

                    txtProfileInfo.setText(profileInfo);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(googleSignInManager.isUserAlreadySignIn()){
            Toast.makeText(this, "Already Signed In.", Toast.LENGTH_SHORT).show();
        }else {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == googleSignInManager.GOOGLE_SIGN_IN){
            googleSignInManager.handleSignInResult(data);
        }
    }



}