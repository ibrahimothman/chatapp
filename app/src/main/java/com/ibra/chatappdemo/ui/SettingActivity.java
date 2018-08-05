package com.ibra.chatappdemo.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ibra.chatappdemo.R;
import com.ibra.chatappdemo.dialog.ProgressDialoge;
import com.ibra.chatappdemo.model.User;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = SettingActivity.class.getCanonicalName();
    public static final String CHANGE_TYPE_EXTRA = "change_type_extra";
    private static final int PICK_IMAGE = 50;
    private TextView mUserNameText,mUserStatusText,mUserMobileText;
    private FloatingActionButton chanfeImageFAB;
    private Toolbar mToolbar;
    private ImageView changeUserName, changeStatus, changeMobile;
    private DatabaseReference mDatabase;
    private StorageReference mainImageRef;
    private CircleImageView profileImage;
    ProgressDialoge mProgressDialoge;
    String uid,type;
    private StorageReference thumbRef;
    private String thumbLink,mainImageLink;
    private Bitmap thumbnail_bitmap;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);



        // setup toolbar
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.setting_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // setup ui views
        profileImage = (CircleImageView)findViewById(R.id.image_profile);
        mUserNameText = (TextView)findViewById(R.id.username_text_profile);
        mUserStatusText = (TextView)findViewById(R.id.status_text_profile);
        mUserMobileText = (TextView)findViewById(R.id.usermobile_text_profile);
        chanfeImageFAB = (FloatingActionButton) findViewById(R.id.change_image_fab);
        changeUserName = (ImageView) findViewById(R.id.change_name_profile);
        changeStatus = (ImageView) findViewById(R.id.change_status_profile);
        changeMobile = (ImageView) findViewById(R.id.change_mobile_profile);
        changeStatus.setOnClickListener(this);
        changeUserName.setOnClickListener(this);
        changeMobile.setOnClickListener(this);
        chanfeImageFAB.setOnClickListener(this);





        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.users_table)).child(uid);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(User.class) != null) {
                    User user = dataSnapshot.getValue(User.class);
                    String name = user.getuName();
                    String status = user.getuStatus();
                    String image = user.getuImage();
                    String mobile = user.getuPhone();
                    updateUi(name, image, status, mobile);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    // update ui views
    private void updateUi(String name, String image, String status, String mobile) {
        mUserNameText.setText(name);
        mUserStatusText.setText(status);
        mUserMobileText.setText(mobile);

        if(image != null &&!image.equals("default")) {
            Log.d(TAG,"imageis "+image);
            Picasso.get().load(image).placeholder(R.drawable.main_default_image).into(profileImage);
        }
    }




    @Override
    public void onClick(View view) {
        // change status
        if(view != chanfeImageFAB) {
            if (view == changeStatus) {
                type = getString(R.string.change_status);
            } else if (view == changeUserName) type = getString(R.string.change_name);
            else if (view == changeMobile) type = getString(R.string.change_mobile);

            Intent intent = new Intent(this, ChangeInfoActivity.class);
            if (!TextUtils.isEmpty(type))
                intent.putExtra(CHANGE_TYPE_EXTRA, type);

            // launch change info activity
            Log.d(TAG, "typeis " + type);
            startActivity(intent);
        }

        // cahnge profile image
        else{
            // pick an image from gallery
            Intent galleryIntent = new Intent();
            galleryIntent.setType("image/*");
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(galleryIntent, getString(R.string.select_image)), PICK_IMAGE);
        }
    }


    // when image picked and return to app
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mProgressDialoge = new ProgressDialoge(this);
                mProgressDialoge.showProgressDialoge(getString(R.string.waiting));

                Uri resultUri = result.getUri();
                File thumbnail_file = new File(resultUri.getPath());
                try {
                    thumbnail_bitmap = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(75)
                            .compressToBitmap(thumbnail_file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                saveImageProfile(resultUri);

            }

        }
    }

    // save image link into firebase mainImageRef
    private void saveImageProfile(Uri resultUri) {
        mainImageRef = FirebaseStorage.getInstance().getReference().child(getString(R.string.profile_images_file))
                .child(uid+".jpg");
        mainImageRef.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()) {
                    mainImageLink = task.getResult().getDownloadUrl().toString();
                    Log.d(TAG,"imagelink "+mainImageLink);
                    savethumbImage(thumbnail_bitmap);

                }
                else Toast.makeText(SettingActivity.this,getString(R.string.error_msg), Toast.LENGTH_SHORT).show();
            }
        });
    }



    // save thumbnail image
    private void savethumbImage(Bitmap thumb){

        // bitmap to bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        thumb.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] thumb_byte = baos.toByteArray();

        // upload bytes to firebase mainImageRef
        thumbRef = FirebaseStorage.getInstance().getReference().child(getString(R.string.profile_images_file))
                .child(getString(R.string.thumbs_file)).child(uid+".jpg");
        UploadTask uploadTask = thumbRef.putBytes(thumb_byte);
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    thumbLink = task.getResult().getDownloadUrl().toString();
                    Log.d(TAG,"thumbnail image is uploaded "+thumbLink);
                    saveImageIntoDB();
                }
            }
        });
    }
    // save image in database
    private void saveImageIntoDB() {

        Map<String,Object> imageMap = new HashMap<>();
        imageMap.put(getString(R.string.main_image_key),mainImageLink);
        imageMap.put(getString(R.string.thumb_image_key),thumbLink);


        mDatabase.updateChildren(imageMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mProgressDialoge.dismissProgressDialoge();
                            Toast.makeText(SettingActivity.this, getString(R.string.image_has_changed), Toast.LENGTH_SHORT).show();
                        }else Toast.makeText(SettingActivity.this, getString(R.string.error_msg), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }



}
