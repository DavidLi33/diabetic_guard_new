package com.example.diabeticguard;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class CameraHomePage extends AppCompatActivity implements View.OnClickListener{

    private ImageView banner, cameraLogo;
    private Button dictate;
    private TextView description;
    private Uri imageUri;
    private Text resultText;
    private MedicineDatabase medicineDatabase;
    private ImageView display;
    private ActivityResultLauncher launcher;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private TextToSpeech tts;
    private int rotation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_home_page);
        medicineDatabase = new MedicineDatabase();
        try {
            medicineDatabase.init(CameraHomePage.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR){
                    tts.setLanguage(Locale.US);
                    speak("Please take a picture by pressing the camera icon");
                }
            }
        });
        description = findViewById(R.id.cameraHomePageText);

        banner = findViewById(R.id.bannerLogo);
        banner.setOnClickListener(this);

        cameraLogo = findViewById(R.id.cameraHomePageImage);
        cameraLogo.setOnClickListener(this);

        dictate = findViewById(R.id.cameraHomePageDictate);
        dictate.setOnClickListener(this);

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
//                Toast.makeText(HomePage.this, "Hihi", Toast.LENGTH_SHORT).show();
                if (result.getResultCode() == RESULT_OK && result.getData() != null){
                    Bitmap thumbnail = null;
                    try {
                        thumbnail = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    assert thumbnail != null;
                    if (thumbnail.getWidth() <= thumbnail.getHeight()){
                        thumbnail = rotateBitmap(thumbnail, 270);
                    }
                    else{
                        thumbnail = rotateBitmap(thumbnail, 90);
                    }
                    display.setImageBitmap(thumbnail);
                    String path = getRealPathFromURI(imageUri);
                    InputImage image = null;
                    try {
                        image = InputImage.fromFilePath(getApplicationContext(), imageUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    File f = new File(path);
                    if (f.exists()){
                        f.delete();
                    }
                    TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
                    Task<Text> processedImage =
                            recognizer.process(image)
                                    .addOnSuccessListener(new OnSuccessListener<Text>() {
                                        @Override
                                        public void onSuccess(Text text) {
                                            try{
                                                resultText = text;
                                                tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                                                    @Override
                                                    public void onInit(int i) {
                                                        if (i != TextToSpeech.ERROR){
                                                            tts.setLanguage(Locale.US);
                                                            speak("Image has been processed");
                                                        }
                                                    }
                                                });
                                            }
                                            catch (Exception e){
                                                e.printStackTrace();
                                            }
                                            Toast.makeText(CameraHomePage.this, "Processed Image", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.cameraHomePageImage:
                takePictureAndDisplay();
                break;
            case R.id.cameraHomePageDictate:
                if (resultText != null){
                    description.setText(resultText.getText());
                    if (medicineDatabase.getMedicine(resultText) != null){
//                        Toast.makeText(HomePage.this, "Medicine: " + medicineDatabase.getMedicine(resultText), Toast.LENGTH_SHORT).show();
                        Intent displayInfo = new Intent(CameraHomePage.this, DisplayUserInfo.class);
                        displayInfo.putExtra("Text", resultText.getText());
                        startActivity(displayInfo);
                    }
                    else{
                        speak("Please take a more proper image");
                        Toast.makeText(CameraHomePage.this, "Please take a more proper image", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    speak("Please take an image");
                    Toast.makeText(CameraHomePage.this, "Please take an image", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.bannerLogo:
                startActivity(new Intent(CameraHomePage.this, HomePage.class));
                break;
        }
    }
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void takePictureAndDisplay(){
        if (ContextCompat.checkSelfPermission(CameraHomePage.this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, 225);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            //Check permissions for Android 6.0+
            if(!checkExternalStoragePermission()){
                return;
            }
        }
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "MyPicture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Photo taken on " + System.currentTimeMillis());
        imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePicture.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        launcher.launch(takePicture);
    }
    private static Bitmap rotateBitmap(Bitmap img, int degree){
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImage = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImage;
    }
    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    private boolean checkExternalStoragePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission not granted.");
            speak("You have not granted camera permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 225);
        } else {
            Log.i(TAG, "You already have permission!");
            return true;
        }

        return false;
    }
    public void speak(String s){
        tts.speak(s, TextToSpeech.QUEUE_FLUSH, null, CameraHomePage.this.hashCode()+"");
    }
    public void onPause(){
        if (tts != null){
            tts.stop();
            tts.shutdown();
        }
        super.onPause();
    }
//    private static File getOutputMediaFile(){
//        String fileName = "temp";
//        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Medicine";
//        File temp = new File(path,fileName+".jpg");
//
//        return temp;
//    }
}