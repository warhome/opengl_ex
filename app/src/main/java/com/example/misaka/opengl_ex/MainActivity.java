package com.example.misaka.opengl_ex;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.effect.Effect;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    // TODO: Заблокировать orientation(?)

    private static final int PERMISSION_REQUEST = 1111;
    private static final int CAMERA_REQUEST = 4321;
    private static final int GALLERY_REQUEST = 1234;
    private boolean rendererSet = false;
    private boolean isSet = false;

    private FloatingActionButton openFab;
    private FloatingActionButton cameraFab;
    private CheckBox mAutofixCheckbox;
    private CheckBox mNegativeCheckbox;
    private SeekBar mSeekBar;

    Bitmap in_image;
    Bitmap sv_image;

    RelativeLayout rl;
    GLRenderer glRenderer;
    GLSurfaceView glSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!isSupportES2()) {
            Toast.makeText(this, "OpenGL ES 2.0 is not supported ):", Toast.LENGTH_LONG).show();
            finish();
        }

        // Layout for GLSurfaceView
        rl = findViewById(R.id.gl_layout);

        // Fab's
        // Open file from gallery
        openFab = findViewById(R.id.open_fab);
        openFab.setOnClickListener(view -> openImage());

        // Open camera
        cameraFab = findViewById(R.id.camera_fab);
        cameraFab.setOnClickListener(view -> openCamera());

        //
        mAutofixCheckbox = findViewById(R.id.checkBoxAutofix);
        mNegativeCheckbox = findViewById(R.id.checkBoxNegative);
        mSeekBar = findViewById(R.id.brightness_seekbar);
    }


    void openCamera (){
        // Check camera permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST);
            }
        } else {
            // Permission has already been granted
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        }
    }

    void openImage(){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
    }


    // TODO: Переписать этот метод
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK)
            switch (requestCode){
                case GALLERY_REQUEST:
                    Uri selectedImage = data.getData();
                    try {
                        in_image = (MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage));
                        createGLSurfaceView();

                    } catch (IOException e) {
                        Log.i("TAG", "Some exception " + e);
                    }
                    break;
                case CAMERA_REQUEST:
                    in_image = ((Bitmap) Objects.requireNonNull(data.getExtras()).get("data"));
                    createGLSurfaceView();
            }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //if (rendererSet) {
        //    glSurfaceView.onPause();
        //}
    }

    @Override
    protected void onResume() {
        super.onResume();
        //if (rendererSet) {
        //    glSurfaceView.onResume();
        // }
    }

    private boolean isSupportES2() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = Objects.requireNonNull(activityManager).getDeviceConfigurationInfo();
        return configurationInfo.reqGlEsVersion >= 0x20000
                || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 && (Build.FINGERPRINT.startsWith("generic")
                ||Build.FINGERPRINT.startsWith("unknown")
                ||Build.MODEL.contains("google_sdk")
                ||Build.MODEL.contains("Emulator")
                ||Build.MODEL.contains("Android SDK built for x86")));
    }


    private void createGLSurfaceView() {
        if(isSet) glSurfaceView.setVisibility(View.GONE);
        else isSet = true;

        glSurfaceView = new GLSurfaceView(getApplicationContext());
        glSurfaceView.setEGLContextClientVersion(2);
        glRenderer = new GLRenderer(this);
        List<String> effects = new ArrayList<>();
        effects.add("ng");
        glRenderer.setCurrEffect(effects);
        glRenderer.setBitmap(in_image);
        glSurfaceView.setRenderer(glRenderer);

        rl.addView(glSurfaceView);
    }

    private void setEffects(List<String> effects) {

    }
}
