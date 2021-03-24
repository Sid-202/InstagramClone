 package com.codepath.sid_instagram_clone.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.codepath.sid_instagram_clone.LoginActivity;
import com.codepath.sid_instagram_clone.MainActivity;
import com.codepath.sid_instagram_clone.Post;
import com.codepath.sid_instagram_clone.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.util.List;

import static android.app.Activity.RESULT_OK;

 /**
 * A simple {@link Fragment} subclass.
 */
public class ComposeFragment extends Fragment {

    public static final String TAG = "ComposeFragment";
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 43;
    private EditText etDescription;
    private ImageView ivPostImage;
    private Button btnCaptureImage;
    private Button btnSubmit;
    private Button btnLogout;
    private ProgressBar pb;

    private File photoFile;
    private String photoFileName = "photo.jpg";


     public ComposeFragment() {
        // Required empty public constructor
    }

    // The onCreateView method is called when Fragment should create its View object hierarchy.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_compose, container, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etDescription = view.findViewById(R.id.etDescription);
        ivPostImage = view.findViewById(R.id.ivPostImage);
        btnCaptureImage = view.findViewById(R.id.btnCaptureImage);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        btnLogout = view.findViewById(R.id.btnLogout);
        pb = (ProgressBar) view.findViewById(R.id.pbLoading);

        btnCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera();
            }
        });

        //    queryPosts();
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = etDescription.getText().toString();
                if (description.isEmpty()) {
                    Toast.makeText(getContext(), "Description cannot be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (photoFile == null || ivPostImage.getDrawable() == null) {
                    Toast.makeText(getContext(), "There is no image!", Toast.LENGTH_SHORT).show();
                    return;
                }
                pb.setVisibility(ProgressBar.VISIBLE);
                ParseUser currentUser = ParseUser.getCurrentUser();
                new CountDownTimer(2000, 1000) {
                    public void onFinish() {
                        // When timer is finished
                        // Execute your code here
                        savePost(description, currentUser, photoFile);
                        pb.setVisibility(ProgressBar.INVISIBLE);
                    }

                    public void onTick(long millisUntilFinished) {
                        // millisUntilFinished    The amount of time until finished.
                    }
                }.start();


            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.logOut();
                startActivity(new Intent(getContext(), LoginActivity.class));
                getActivity().finish();
            }
        });

    }

     private void launchCamera() {
         // create Intent to take a picture and return control to the calling application
         Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
         // Create a File reference for future access
         photoFile = getPhotoFileUri(photoFileName);

         // wrap File object into a content provider
         // required for API >= 24
         // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
         Uri fileProvider = FileProvider.getUriForFile(getContext(), "com.codepath.fileprovider", photoFile);
         intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

         // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
         // So as long as the result is not null, it's safe to use the intent.
         if (intent.resolveActivity(getContext().getPackageManager()) != null) {
             // Start the image capture intent to take photo
             startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
         }
     }

     @Override
     public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
             if (resultCode == RESULT_OK) {
                 try {
                     // by this point we have the camera photo on disk
                     Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                     // RESIZE BITMAP, see section below

                     // Load the taken image into a preview
                     ivPostImage.setImageBitmap(takenImage);
                 } catch (Exception e) {
                     Log.e(TAG, "Resize picture Scale!", e);
                 }


             } else { // Result was a failure
                 Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
             }
         }
     }

     // Returns the File for a photo stored on disk given the fileName
     public File getPhotoFileUri(String fileName) {
         // Get safe storage directory for photos
         // Use `getExternalFilesDir` on Context to access package-specific directories.
         // This way, we don't need to request external read/write runtime permissions.
         File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

         // Create the storage directory if it does not exist
         if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
             Log.d(TAG, "failed to create directory");
         }

         // Return the file target for the photo based on filename
         return new File(mediaStorageDir.getPath() + File.separator + fileName);
     }

     private void savePost(String description, ParseUser currentUser, File photoFile) {
         Post post = new Post();
         post.setDescription(description);
         post.setImage(new ParseFile(photoFile));
         post.setUser(currentUser);
         post.saveInBackground(new SaveCallback() {
             @Override
             public void done(ParseException e) {
                 if (e != null) {
                     Log.e(TAG, "Error while saving", e);
                     Toast.makeText(getContext(), "Error while saving!", Toast.LENGTH_SHORT).show();
                 }
                 Log.i(TAG, "Post save was successful!");
                 etDescription.setText("");
                 ivPostImage.setImageResource(0);
             }
         });
     }

}