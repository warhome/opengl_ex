package com.example.misaka.opengl_ex;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener,
        Switch.OnCheckedChangeListener,
        OpenImageDialogFragment.OpenImageDialogCommunicator, GLSurfaceView.OnTouchListener {

    // Requests and tags
    private static final int CAMERA_REQUEST = 4321;
    private static final int GALLERY_REQUEST = 1234;
    private static final int PERMISSION_REQUEST = 1111;
    private static final String SHOW_IMAGE_OPEN_DIALOG_TAG = "OPEN_IMAGE";
    private static final String SHOW_IMAGE_INFO_DIALOG_TAG = "IMAGE_INFO";
    public static final String SHOW_IMAGE_DATA_DIALOG_TAG = "IMAGE_DATA";

    // OpenGl
    // private boolean rendererSet = false;
    private boolean isGlSurfaceViewSet = false;
    private GLRenderer glRenderer;
    private GLSurfaceView glSurfaceView;
    private List<Filter> filters = new ArrayList<>();
    private FilterHelper filterHelper = new FilterHelper();
    private Bitmap in_image;
    private RelativeLayout rl;
    private ResizeUtils resizeUtils;

    private Switch autofix;
    private SeekBar bright;
    private SeekBar contrast;
    private SeekBar fillight;
    private SeekBar fishEye;
    private SeekBar temperature;
    private Switch cross;
    private Switch documentary;
    private Switch negative;
    private Switch blackWhite;
    private Switch posterize;
    private Switch sepia;
    private Switch gray;
    private SeekBar black;
    private SeekBar white;
    private Button resetButton;
    private Button infoButton;

    private LinearLayout bottomSheet;

    OpenImageDialogFragment mOpenImageDialogFragment;
    ImageInfoDialogFragment mImageInfoDialogCommunicator;

    private ScrollView scrollView;
    private FloatingActionButton chackmarkButton;
    private FloatingActionButton shareButton;
    private FloatingActionButton saveButton;
    private boolean isFabMenuOpen = false;
    private String in_image_real_path;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkSupportES2();

        //ResizeUtils init
        resizeUtils = new ResizeUtils();

        // Toolbar
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        // Layout for GLSurfaceView
        rl = findViewById(R.id.gl_layout);

        // ScrollView (filters)
        scrollView = findViewById(R.id.scrollView);
        bottomSheet = findViewById(R.id.bottom_sheet);

        // Open file from gallery/camera (dialog)
        Button open = toolbar.findViewById(R.id.button);
        open.setOnClickListener(view -> {
            mOpenImageDialogFragment = new OpenImageDialogFragment();
            mOpenImageDialogFragment.show(getFragmentManager(), SHOW_IMAGE_OPEN_DIALOG_TAG);
        });

        // Check button
        chackmarkButton = findViewById(R.id.chackmarkActionButton);
        chackmarkButton.setOnClickListener(v -> {
            if(!isFabMenuOpen) { showFabMenu();}
            else closeFabMenu();
        });

        // Save button
        saveButton = findViewById(R.id.saveActionButton);
        saveButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") == 0) {
                saveImage(glRenderer.getBmp());
            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, "android.permission.WRITE_EXTERNAL_STORAGE")) {
                ActivityCompat.requestPermissions(this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, PERMISSION_REQUEST);
            }
        });

        // Share button
        shareButton = findViewById(R.id.shareActionButton);
        shareButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") == 0) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/*");
                String stringBuilder = Environment.getExternalStorageDirectory().getPath() + "/" +
                        saveImage(glRenderer.getBmp());
                shareIntent.putExtra("android.intent.extra.STREAM", Uri.parse(stringBuilder));
                startActivity(Intent.createChooser(shareIntent, "Share it"));
            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, "android.permission.WRITE_EXTERNAL_STORAGE")) {
                ActivityCompat.requestPermissions(this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, PERMISSION_REQUEST);
            }
        });

        // Reset button
        resetButton = toolbar.findViewById(R.id.reset_button);
        resetButton.setOnClickListener(v -> {
            resetFiltersUI();
            glSurfaceView.queueEvent(() -> {
                glRenderer.setFilters(new ArrayList<>());
                glSurfaceView.requestRender();
            });
        });

        // Info button
        infoButton = toolbar.findViewById(R.id.info_image_button);
        infoButton.setOnClickListener(v -> {
            try {
                ExifInterface exifInterface = new ExifInterface(in_image_real_path);
                Bundle bundle = new Bundle();
                bundle.putStringArrayList(SHOW_IMAGE_DATA_DIALOG_TAG, new ArrayList<>(Arrays.asList(
                        exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH),
                        exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH),
                        exifInterface.getAttribute(ExifInterface.TAG_GPS_DATESTAMP),
                        exifInterface.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP))));

                mImageInfoDialogCommunicator = new ImageInfoDialogFragment();
                mImageInfoDialogCommunicator.setArguments(bundle);
                mImageInfoDialogCommunicator.show(getFragmentManager(), SHOW_IMAGE_INFO_DIALOG_TAG);
            } catch (IOException e) {
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Filters
        autofix = findViewById(R.id.autofixSwitch);
        bright = findViewById(R.id.seekBarBright);
        contrast = findViewById(R.id.seekBarContrast);
        fillight = findViewById(R.id.seekBarFilllight);
        fishEye = findViewById(R.id.seekBarFishEye);
        temperature = findViewById(R.id.seekBarTemperature);
        black = findViewById(R.id.seekBarBlack);
        white = findViewById(R.id.seekBarWhite);

        cross = findViewById(R.id.crossprocessSwitch);
        documentary = findViewById(R.id.documentarySwitch);
        negative = findViewById(R.id.negativeSwitch);
        blackWhite = findViewById(R.id.BlackWhiteSwitch);
        posterize = findViewById(R.id.posterizeSwitch);
        sepia = findViewById(R.id.sepiaSwitch);
        gray = findViewById(R.id.grayscaleSwitch);

        bright.setOnSeekBarChangeListener(this);
        contrast.setOnSeekBarChangeListener(this);
        fillight.setOnSeekBarChangeListener(this);
        fishEye.setOnSeekBarChangeListener(this);
        black.setOnSeekBarChangeListener(this);
        white.setOnSeekBarChangeListener(this);
        temperature.setOnSeekBarChangeListener(this);

        autofix.setOnCheckedChangeListener(this);
        cross.setOnCheckedChangeListener(this);
        documentary.setOnCheckedChangeListener(this);
        negative.setOnCheckedChangeListener(this);
        blackWhite.setOnCheckedChangeListener(this);
        sepia.setOnCheckedChangeListener(this);
        gray.setOnCheckedChangeListener(this);
        posterize.setOnCheckedChangeListener(this);

    }

    private void closeFabMenu() {
        isFabMenuOpen = false;
        saveButton.animate().translationY(0);
        shareButton.animate().translationY(0);
    }

    private void showFabMenu() {
        isFabMenuOpen = true;
        saveButton.animate().translationY(-getResources().getDimension(R.dimen.standard_60));
        shareButton.animate().translationY(-getResources().getDimension(R.dimen.standard_120));
    }

    void openCamera() {
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

    void openImage() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
    }

    String saveImage(Bitmap finalBitmap) {
        File mDir = new File(Environment.getExternalStorageDirectory().getPath());
        mDir.mkdirs();
        String fname = "Image-" + UUID.randomUUID() + ".jpg";
        File file = new File(mDir, fname);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            Toast.makeText(this, "Saved in " + Environment.getExternalStorageDirectory().getPath() + "/" + fname, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
        return fname;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case GALLERY_REQUEST:
                    try {
                        in_image = (MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData()));
                        // Just get the "real" image path
//                        Uri tempUri = getImageUri(getApplicationContext(), in_image);
//                        in_image_real_path = getRealPathFromURI(tempUri);
                    } catch (IOException e) {
                        Log.i("TAG", "Some exception " + e);
                    }
                    break;
                case CAMERA_REQUEST:
                    in_image = ((Bitmap) Objects.requireNonNull(data.getExtras()).get("data"));
            }
            scrollView.setVisibility(View.VISIBLE);
            resetButton.setVisibility(View.VISIBLE);
            createGLSurfaceView();
            resetFiltersUI();
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

    private void checkSupportES2() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = Objects.requireNonNull(activityManager).getDeviceConfigurationInfo();
        if(!(configurationInfo.reqGlEsVersion >= 0x20000
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86"))){
            Toast.makeText(this, "OpenGL ES 2.0 is not supported ):", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void createGLSurfaceView() {
        if (isGlSurfaceViewSet) glSurfaceView.setVisibility(View.GONE);
        else isGlSurfaceViewSet = true;

        glSurfaceView = new GLSurfaceView(getApplicationContext());
        glSurfaceView.setEGLContextClientVersion(2);
        glRenderer = new GLRenderer(this);
        glRenderer.setBitmap(in_image);
        glSurfaceView.setRenderer(glRenderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        glSurfaceView.setOnTouchListener(this);

        resizeUtils.resizeView(rl, in_image.getHeight(), in_image.getWidth(), getApplicationContext());
        rl.addView(glSurfaceView);

        chackmarkButton.show();
        shareButton.show();
        saveButton.show();
        resetButton.setVisibility(View.VISIBLE);
        infoButton.setVisibility(View.VISIBLE);
        bottomSheet.setVisibility(View.VISIBLE);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.seekBarBright:
                if (filterHelper.isContains(filters, "br")) {
                    List<Float> params = new ArrayList<>();
                    params.add((float) progress / 50f);
                    filterHelper.findFilterId(filters, "br").setParams(params);
                } else filters.add(new Filter("br", (float) progress / 50f));
                break;
            case R.id.seekBarContrast:
                if (filterHelper.isContains(filters, "ct")) {
                    List<Float> params = new ArrayList<>();
                    params.add((float) progress / 50f);
                    filterHelper.findFilterId(filters, "ct").setParams(params);
                } else filters.add(new Filter("ct", (float) progress / 50f));
                break;
            case R.id.seekBarFilllight:
                if (filterHelper.isContains(filters, "fl")) {
                    List<Float> params = new ArrayList<>();
                    params.add((float) progress / 50f);
                    filterHelper.findFilterId(filters, "fl").setParams(params);
                } else filters.add(new Filter("fl", (float) progress / 50f));
                break;

            case R.id.seekBarFishEye:
                if (filterHelper.isContains(filters, "fe")) {
                    List<Float> params = new ArrayList<>();
                    params.add((float) progress / 50f);
                    filterHelper.findFilterId(filters, "fe").setParams(params);
                } else filters.add(new Filter("fe", (float) progress / 50f));
                break;

            case R.id.seekBarBlack:
                if (filterHelper.isContains(filters, "bw")) {
                    filterHelper.findFilterId(filters, "bw").params.set(0, (float) progress / 50f);
                }
                break;

            case R.id.seekBarWhite:
                if (filterHelper.isContains(filters, "bw")) {
                    filterHelper.findFilterId(filters, "bw").params.set(1, (float) progress / 50f);
                }
                break;

            case R.id.seekBarTemperature:
                if (filterHelper.isContains(filters, "tr")) {
                    List<Float> params = new ArrayList<>();
                    params.add((float) progress / 50f);
                    filterHelper.findFilterId(filters, "tr").setParams(params);
                } else filters.add(new Filter("tr", (float) progress / 50f));
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
            case R.id.sepiaSwitch:
                if (isChecked)
                    filters.add(new Filter("sa"));
                else
                    filterHelper.deleteElement(filters, "sa");
                break;
            case R.id.grayscaleSwitch:
                if (isChecked)
                    filters.add(new Filter("gs"));
                else
                    filterHelper.deleteElement(filters, "gs");
                break;
            case R.id.posterizeSwitch:
                if (isChecked)
                    filters.add(new Filter("pr"));
                else
                    filterHelper.deleteElement(filters, "pr");
                break;
            case R.id.BlackWhiteSwitch:
                if (isChecked) {
                    filters.add(new Filter("bw"));
                    filterHelper.findFilterId(filters, "bw").params.set(0, (float) black.getProgress() / 50f);
                    filterHelper.findFilterId(filters, "bw").params.set(0, (float) white.getProgress() / 50f);
                } else
                    filterHelper.deleteElement(filters, "bw");
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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case 0:
                this.glSurfaceView.queueEvent(() -> {
                    glRenderer.setFilters(new ArrayList<>());
                    glSurfaceView.requestRender();
                });
                break;
            case 1:
                this.glSurfaceView.queueEvent(() -> {
                    glRenderer.setFilters(filters);
                    glSurfaceView.requestRender();
                });
                break;
            default:
                break;
        }
        return true;
    }

    public void resetFiltersUI(){
        bright.setProgress(50);
        contrast.setProgress(50);
        fillight.setProgress(50);
        fishEye.setProgress(50);
        temperature.setProgress(50);
        autofix.setChecked(false);
        black.setProgress(50);
        white.setProgress(50);
        cross.setChecked(false);
        documentary.setChecked(false);
        negative.setChecked(false);
        blackWhite.setChecked(false);
        sepia.setChecked(false);
        gray.setChecked(false);
        posterize.setChecked(false);
        if(!filters.isEmpty()) filters = new ArrayList<>();
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        Toast.makeText(this, cursor.getString(idx), Toast.LENGTH_SHORT).show();
        return cursor.getString(idx);
    }
}