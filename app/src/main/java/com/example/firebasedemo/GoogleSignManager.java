package com.example.firebasedemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class GoogleSignManager {
    private static GoogleSignManager instance = null;
    private Context context;
    private Activity activity;
    private GoogleSignInClient mGoogleSignInClient;
    public final int GOOGLE_SIGN_IN = 100;
    private FirebaseAuth mAuth;

    private GoogleSignManager(){

    }

    public static GoogleSignManager getInstance(Context context){
        if(instance == null){
            instance = new GoogleSignManager();
        }
        instance.init(context);
        return instance;
    }
    private void init(Context context){
        this.context = context;
        this.activity = (Activity) context;
    }

    public void setUpGoogleSignInOption(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("387128153732-1dfiorjbfnrcq33md5dqfem77kfbc2i2.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
        mAuth = FirebaseAuth.getInstance();
    }

    public boolean isUserAlreadySignIn(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            return true;
        }else {
            return false;
        }
    }

    public void signIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        activity.startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
        Log.d("TAG", "begin Google Sign In");
    }

    public void signOut(){
        FirebaseAuth.getInstance().signOut();
        mGoogleSignInClient.signOut();
        Toast.makeText(context, "Signed Out", Toast.LENGTH_SHORT).show();
    }

    public FirebaseUser getProfileInfo(){
        FirebaseUser account = FirebaseAuth.getInstance().getCurrentUser();
        if(account != null){

        }else {
            Toast.makeText(context, "No Account Info Found", Toast.LENGTH_SHORT).show();
        }
        return account;
    }

    public void handleSignInResult(Intent data){

        try {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            GoogleSignInAccount account = task.getResult(ApiException.class);
            firebaseAuthWithGoogle(account.getIdToken());
        }catch (ApiException e){
            Log.w("TAg", "SignInResult:Failed code = " + e.getStatusCode());
            Toast.makeText(context, "SignInFailed", Toast.LENGTH_SHORT).show();
            Log.d("TAG", "SignInFailed");
        }
    }

    public void firebaseAuthWithGoogle(String idToken){
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Log.d("TAG", "SignInWithCredential : Success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(context, "Signed In Successfully", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(context, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                            Log.d("TAG", "SignInWithCredential : Failure");
                        }
                    }
                });
    }

}
