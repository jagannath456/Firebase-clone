package com.example.firebasedemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.firebasedemo.databinding.ActivityRegisterBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    ActivityRegisterBinding binding;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore fstore;

    GoogleSignInClient mGoogleSignInClient;
    String TAG = "MainActivity";
    int RC_SIGN_IN = 1;
    String userID;
    String name,email,password,cpassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();




        binding.register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 name = binding.enterName.getText().toString();
                email = binding.remail.getText().toString().trim();
                password = binding.rpassword.getText().toString().trim();
                cpassword = binding.conformPassword.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    binding.enterName.setError("Enter Name");
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    binding.remail.setError("Email is required");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    binding.rpassword.setError("Password is required");
                    return;

                }
                if (password.length() < 6) {
                    binding.rpassword.setError("Password Must be >= 6 Characters ");
                    return;
                }
                if (!password.equals(cpassword)) {
                    Toast.makeText(RegisterActivity.this, "Password doesn't match ...", Toast.LENGTH_SHORT).show();
                    return;

                }

                binding.progressbar.setVisibility(View.VISIBLE);

                firebaseAuth .createUserWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Toast.makeText(getApplicationContext(), "Registration successful..", Toast.LENGTH_SHORT).show();

                        userID = firebaseAuth.getCurrentUser().getUid();
                        DocumentReference documentReference = fstore.collection("users").document(userID);
                        Map<String,Object> userX = new HashMap<>();
                        userX.put("name", name);
                        userX.put("email", email);

                        documentReference.set(userX).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d(TAG,"onSuccess"+ userID);

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Log.d(TAG,"onFailure"+e.toString());
                            }
                        });

                        Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                        startActivity(intent);
                        binding.progressbar.setVisibility(View.GONE);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        binding.progressbar.setVisibility(View.GONE);

                        Toast.makeText(getApplicationContext(), "Please try again", Toast.LENGTH_SHORT).show();

                    }
                });


            }
        });
        binding.loginHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        initializations();

    }
    private void initializations() {
        firebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("387128153732-1dfiorjbfnrcq33md5dqfem77kfbc2i2.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);

        binding.signButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        // Check condition
        if(firebaseUser!=null)
        {
            // When user already sign in
            // redirect to profile activity
            startActivity(new Intent(RegisterActivity.this,GoogleActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,RC_SIGN_IN);
        Log.d(TAG, "begin Google Sign In");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check condition
        if(requestCode==RC_SIGN_IN)
        {
            // When request code is equal to 100
            // Initialize task
            Task<GoogleSignInAccount> signInAccountTask=GoogleSignIn
                    .getSignedInAccountFromIntent(data);

            // check condition
            if(signInAccountTask.isSuccessful()) {
                // When google sign in successful
                // Initialize string
                String s="Google sign in successful";
                // Display Toast
                displayToast(s);
                // Initialize sign in account
                try {
                    // Initialize sign in account
                    GoogleSignInAccount googleSignInAccount=signInAccountTask
                            .getResult(ApiException.class);
                    // Check condition
                    if(googleSignInAccount!=null)
                    {
                        // When sign in account is not equal to null
                        // Initialize auth credential
                        AuthCredential authCredential= GoogleAuthProvider
                                .getCredential(googleSignInAccount.getIdToken()
                                        ,null);
                        // Check credential
                        firebaseAuth.signInWithCredential(authCredential)
                                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        // Check condition
                                        if(task.isSuccessful())
                                        {
                                            // When task is successful
                                            // Redirect to profile activity
                                            startActivity(new Intent(RegisterActivity.this
                                                    ,MainActivity.class)
                                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                                            // Display Toast
                                            displayToast("Firebase authentication successful");
                                        }
                                        else
                                        {
                                            // When task is unsuccessful
                                            // Display Toast
                                            displayToast("Authentication Failed :"+task.getException()
                                                    .getMessage());
                                        }
                                    }
                                });

                    }
                }
                catch (ApiException e)
                {
                    e.printStackTrace();

                }
            }else {
                Log.d(TAG, "Error ...");
            }
        }
    }

    private void displayToast(String s) {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            sendUserToMainActivity();
        }
    }
    private void sendUserToMainActivity() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        this.finish();
    }
}