package com.example.misaka.opengl_ex;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener,
        Switch.OnCheckedChangeListener,
        OpenImageDialogFragment.OpenImageDialogCommunicator {

    // Requests
    private static final int PERMISSION_REQUEST = 1111;
    private static final int CAMERA_REQUEST = 4321;
    private static final int GALLERY_REQUEST = 1234;
    private static final String SHOW_DIALOG_TAG = "OPEN_IMAGE";

    // OpenGl
    private boolean rendererSet = false;
    private boolean isGlSurfaceViewSet = false;
    private GLRenderer glRenderer;
    private GLSurfaceView glSurfaceView;
    private List<Filter> filters = new ArrayList<>();
    private FilterHelper filterHelper = new FilterHelper();
    private Bitmap in_image;
    private RelativeLayout rl;

    OpenImageDialogFragment mOpenImageDialogFragment;
    private ScrollView scrollView;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!isSupportES2()) {
            Toast.makeText(this, "OpenGL ES 2.0 is not supported ):", Toast.LENGTH_LONG).show();
            finish();
        }

        // Toolbar
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        // Open file from gallery
        Button b = toolbar.findViewById(R.id.button);
        b.setOnClickListener(view -> {
            mOpenImageDialogFragment = new OpenImageDialogFragment();
            mOpenImageDialogFragment.show(getFragmentManager(), SHOW_DIALOG_TAG);
        });

        // Layout for GLSurfaceView
        rl = findViewById(R.id.gl_layout);

        // ScrollView
        scrollView = findViewById(R.id.scrollView);

        // Filters
        Switch autofix = findViewById(R.id.autofixSwitch);
        SeekBar bright = findViewById(R.id.seekBarBright);
        SeekBar contrast = findViewById(R.id.seekBarContrast);
        Switch cross = findViewById(R.id.crossprocessSwitch);
        Switch documentary = findViewById(R.id.documentarySwitch);
        Switch negative = findViewById(R.id.negativeSwitch);


        bright.setOnSeekBarChangeListener(this);
        contrast.setOnSeekBarChangeListener(this);
        autofix.setOnCheckedChangeListener(this);
        cross.setOnCheckedChangeListener(this);
        documentary.setOnCheckedChangeListener(this);
        negative.setOnCheckedChangeListener(this);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK) {
            in_image.recycle();
            switch (requestCode) {
                case GALLERY_REQUEST:
                    try {
                        in_image = (MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData()));
                    } catch (IOException e) {
                        Log.i("TAG", "Some exception " + e);
                    }
                    break;
                case CAMERA_REQUEST:
                    in_image = ((Bitmap) Objects.requireNonNull(data.getExtras()).get("data"));
            }
            scrollView.setVisibility(View.VISIBLE);
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
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86");
    }


    private void createGLSurfaceView() {
        if(isGlSurfaceViewSet) glSurfaceView.setVisibility(View.GONE);
        else isGlSurfaceViewSet = true;

        glSurfaceView = new GLSurfaceView(getApplicationContext());
        glSurfaceView.setEGLContextClientVersion(2);
        glRenderer = new GLRenderer(this);
        glRenderer.setBitmap(in_image);
        glSurfaceView.setRenderer(glRenderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        rl.addView(glSurfaceView);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.seekBarBright:
                if(filterHelper.isContains(filters, "br")) filterHelper.deleteElement(filters, "br");
                if(progress != 50) filters.add(new Filter("br", (float)progress / 50f));
                break;
            case R.id.seekBarContrast:
                if(filterHelper.isContains(filters, "ct")) filterHelper.deleteElement(filters, "ct");
                if(progress != 50) filters.add(new Filter("ct", (float)progress / 50f));
                break;
        }
        glSurfaceView.queueEvent(() -> {
            glRenderer.setFilters(filters);
            glSurfaceView.requestRender();
        });
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.autofixSwitch:
                if (isChecked)
                    filters.add(new Filter("af"));
                else
                    filterHelper.deleteElement(filters, "af");
            break;
            case R.id.crossprocessSwitch:
                if (isChecked)
                    filters.add(new Filter("cp"));
                else
                    filterHelper.deleteElement(filters, "cp");
            break;
            case R.id.documentarySwitch:
                if (isChecked)
                    filters.add(new Filter("dt"));
                else
                    filterHelper.deleteElement(filters, "dt");
            case R.id.negativeSwitch:
                if (isChecked)
                    filters.add(new Filter("ng"));
                else
                    filterHelper.deleteElement(filters, "ng");
            break;
        }

        glSurfaceView.queueEvent(() -> {
            glRenderer.setFilters(filters);
            glSurfaceView.requestRender();
        });
    }

    @Override
    public void onUpdateOption(int which, String tag) {
        switch (which) {
            case 0:
                openCamera();
                break;
            case 1:
                openImage();
                break;
            default:
                break;
        }
    }
}
