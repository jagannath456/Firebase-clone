package com.example.firebasedemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.firebasedemo.databinding.ActivityMainBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding ;

    FirebaseAuth firebaseAuth ;
    String userID;
    private GoogleSignInClient mGoogleSignInClient;

    private Uri filePath;
    FirebaseStorage storage;
    FirebaseFirestore texter;
    StorageReference storageReference;
    String st, ke;
//    SectionPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main);

        firebaseAuth =FirebaseAuth.getInstance();
        texter = FirebaseFirestore.getInstance();
        userID = firebaseAuth.getCurrentUser().getUid();


        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        binding.logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                mGoogleSignInClient.signOut();
                startActivity(new Intent(getApplicationContext() , LoginActivity.class));

            }
        });

//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken("387128153732-1dfiorjbfnrcq33md5dqfem77kfbc2i2.apps.googleusercontent.com")
//                .requestEmail()
//                .build();
//
//        mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);
//        firebaseAuth = FirebaseAuth.getInstance();

        binding.googleProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser account = FirebaseAuth.getInstance().getCurrentUser();

                if(account != null){
                    String PersonName = account.getDisplayName();
                    String PersonEmail = account.getEmail();
                    Uri PersonPhoto = account.getPhotoUrl();


                    binding.mEmail.setText(PersonEmail);
                    binding.mName.setText(PersonName);
                    binding.profileImage.setImageURI(PersonPhoto);
//            txtProfileInfo.setText(profileInfo);
                }
            }
        });

        DocumentReference documentReference = texter.collection("users").document(userID);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                binding.mEmail.setText(value.getString("email"));
                binding.mName.setText(value.getString("name"));

            }
        });

        StorageReference reference=storageReference.child("users/"+firebaseAuth.getCurrentUser().getUid()+"/profile.img");
        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(binding.profileImage);

            }
        });


        binding.upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                uploadToFirebase(filePath);
            }
        });
        ActivityResultLauncher<String> launcher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                binding.profileImage.setImageURI(result);

                binding.upload.setVisibility(View.VISIBLE);
                filePath = result;
            }
        });

        binding.profileImage.setOnClickListener(vi-> launcher.launch("image/*"));


//        pagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());
//        binding.viewPager.setAdapter(pagerAdapter);
//
//        binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//
//            }
//        });

    }
    private void uploadToFirebase(Uri imageUri) {
        if (filePath != null) {
            ProgressDialog progressDialog
                    = new ProgressDialog(getApplicationContext());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();


            final StorageReference fileRef=storageReference.child("users/"+firebaseAuth.getCurrentUser().getUid()+"/profile.img");
            fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            progressDialog.dismiss();
                            Picasso.get().load(uri).into(binding.profileImage);
                            Toast.makeText(getApplicationContext(), "Uploaded Successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress
                            = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                    progressDialog.setMessage("Uploaded " + (int)progress + "%");

                    binding.upload.setVisibility(View.GONE);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    binding.upload.setVisibility(View.GONE);
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Uploading Failed !!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
//    public class SectionPagerAdapter extends FragmentPagerAdapter {
//        public SectionPagerAdapter(FragmentManager supportFragmentManager) {
//            super(supportFragmentManager);
//        }
//
//        @NonNull
//        @Override
//        public Fragment getItem(int position) {
//            Fragment fragment = null;
//            switch (position) {
//                case 0:
//                    fragment = new Profile_fragement();
//
//                    break;
//
//            }
//            return fragment;
//        }
//
//        @Override
//        public int getCount() {
//            return 1;
//        }
//
//    }

}