package com.example.ecg_app;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddPatientActivity extends AppCompatActivity {

    private Button uploadDetailsButton, scanImageButton;
    private EditText patientNameEditText, ageEditText, mobileNumberEditText, placeEditText;
    private TextView predictionTextView;
    private DatabaseReference database;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    String imagePath;

    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(AddPatientActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_patient);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        database = FirebaseDatabase.getInstance("https://ecg-app-94475-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();

        patientNameEditText = (EditText) findViewById(R.id.patient_name);
        ageEditText = (EditText) findViewById(R.id.age);
        mobileNumberEditText = (EditText) findViewById(R.id.mobile_number);
        placeEditText = (EditText) findViewById(R.id.place); 
        scanImageButton = (Button) findViewById(R.id.scan_image); 
        uploadDetailsButton = (Button) findViewById(R.id.upload_details);
        predictionTextView = (TextView) findViewById(R.id.prediction);
        
        scanImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
            }
        });
        
        uploadDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String patientName, age, mobileNumber, place, prediction;
                patientName = String.valueOf(patientNameEditText.getText());
                age = String.valueOf(ageEditText.getText());
                mobileNumber = String.valueOf(mobileNumberEditText.getText());
                place = String.valueOf(placeEditText.getText());
                prediction = String.valueOf(predictionTextView.getText());

                //Check Name is empty or not
                if(TextUtils.isEmpty(patientName)){
                    Toast.makeText(AddPatientActivity.this, "Enter Patient Name", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Check Age is empty or not
                if(TextUtils.isEmpty(age)){
                    Toast.makeText(AddPatientActivity.this, "Enter Age", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Check Mobile Number is empty or not
                if(TextUtils.isEmpty(mobileNumber)){
                    Toast.makeText(AddPatientActivity.this, "Enter Mobile Number", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Check Place is empty or not
                if(TextUtils.isEmpty(place)){
                    Toast.makeText(AddPatientActivity.this, "Enter Place", Toast.LENGTH_SHORT).show();
                    return;
                }

                Patient patient = new Patient(patientName, age, mobileNumber, place, prediction);

                database.child("patients").child(mobileNumber).setValue(patient)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(AddPatientActivity.this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(AddPatientActivity.this, "Uploading Details Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        scanImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageSourceDialog();
            }
        });
    }

    private void showImageSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image Source");
        builder.setItems(new CharSequence[]{"Camera", "Gallery"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        dispatchTakePictureIntent();
                        break;
                    case 1:
                        dispatchPickImageIntent();
                        break;
                }
            }
        });
        builder.show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void dispatchPickImageIntent() {
        Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickImageIntent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            Uri imageUri = saveImageToGallery(imageBitmap);
            if (imageUri != null) {
                // Display the captured image in the ImageView
                // Store the image path for future use
                imagePath = imageUri.toString();
                if (imagePath != null) {
                    new PredictTask().execute(imagePath);
                } else {
                    Toast.makeText(AddPatientActivity.this, "Please select an image first", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(this, imagePath, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    imagePath = getRealPathFromURI(selectedImageUri);
                    if (imagePath != null) {
                        new PredictTask().execute(imagePath);
                    } else {
                        Toast.makeText(AddPatientActivity.this, "Please select an image first", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private Uri saveImageToGallery(Bitmap imageBitmap) {
        // Save the image to the MediaStore
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Captured Image");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image captured using camera");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        ContentResolver resolver = getContentResolver();
        Uri uri = null;
        try {
            uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                OutputStream outputStream = resolver.openOutputStream(uri);
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (uri != null) {
                resolver.delete(uri, null, null);
                uri = null;
            }
        }
        return uri;
    }


    private String getRealPathFromURI(Uri contentUri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null);
        if (cursor == null) return null;
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(columnIndex);
        cursor.close();
        return path;
    }

    private class PredictTask extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog = new ProgressDialog(AddPatientActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Predicting...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... imagePath) {
            if (imagePath.length == 0) {
                return null;
            }

            OkHttpClient client = new OkHttpClient();

            File imageFile = new File(imagePath[0]);
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", imageFile.getName(),
                            RequestBody.create(MediaType.parse("image/jpeg"), imageFile))
                    .build();

            Request request = new Request.Builder()
                    .url("https://fc9d-2401-4900-33d7-1d1e-7827-b2f7-6fdb-be9e.ngrok-free.app/predict")
                    .post(requestBody)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    return response.body().string();
                }
            } catch (IOException e) {
                Log.e("PredictTask", "Error predicting", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            if (result != null) {
                predictionTextView.setText(result);
            } else {
                predictionTextView.setText("Prediction failed");
            }
        }
    }
}