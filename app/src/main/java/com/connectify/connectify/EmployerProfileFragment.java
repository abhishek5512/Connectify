package com.connectify.connectify;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EmployerProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1001;

    private ImageView imageCompanyLogo;
    private EditText editCompanyName, editHrName, editEmail, editContact, editAbout, editAddress, editWebsite;
    private Button btnUploadLogo, btnSaveProfile, btnRemoveLogo;
    private TextView tvUploadStatus;

    private Uri selectedImageUri;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private StorageReference storageReference;

    private String currentUserEmail, currentLogoUrl;

    public EmployerProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_employer_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageCompanyLogo = view.findViewById(R.id.imageCompanyLogo);
        editCompanyName = view.findViewById(R.id.editCompanyName);
        editHrName = view.findViewById(R.id.editHrName);
        editEmail = view.findViewById(R.id.editEmail);
        editContact = view.findViewById(R.id.editContact);
        editAbout = view.findViewById(R.id.editAbout);
        editAddress = view.findViewById(R.id.editAddress);
        editWebsite = view.findViewById(R.id.editWebsite);
        btnUploadLogo = view.findViewById(R.id.btnUploadLogo);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnRemoveLogo = view.findViewById(R.id.btnRemoveLogo);
        tvUploadStatus = view.findViewById(R.id.tvUploadStatus);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("employer_logos");
        currentUserEmail = auth.getCurrentUser().getEmail();

        editEmail.setText(currentUserEmail);
        loadProfile();

        btnUploadLogo.setOnClickListener(v -> openImagePicker());
        btnSaveProfile.setOnClickListener(v -> saveProfile());
        btnRemoveLogo.setOnClickListener(v -> removeLogo());
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Company Logo"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), selectedImageUri);
                imageCompanyLogo.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveProfile() {
        String companyName = editCompanyName.getText().toString().trim();
        String hrName = editHrName.getText().toString().trim();
        String contact = editContact.getText().toString().trim();
        String about = editAbout.getText().toString().trim();
        String address = editAddress.getText().toString().trim();
        String website = editWebsite.getText().toString().trim();

        if (TextUtils.isEmpty(companyName) || TextUtils.isEmpty(hrName) || TextUtils.isEmpty(contact) || TextUtils.isEmpty(about)) {
            Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri != null) {
            StorageReference fileRef = storageReference.child(currentUserEmail + ".jpg");
            UploadTask uploadTask = fileRef.putFile(selectedImageUri);

            uploadTask.addOnProgressListener(taskSnapshot -> {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                tvUploadStatus.setText("Uploading: " + (int) progress + "%");
            });

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    tvUploadStatus.setText("Upload Complete!");
                    saveDataToFirestore(companyName, hrName, contact, about, imageUrl, address, website);
                });
            });

            uploadTask.addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
                tvUploadStatus.setText("Upload Failed!");
            });

        } else {
            saveDataToFirestore(companyName, hrName, contact, about, currentLogoUrl, address, website);
        }
    }

    private void saveDataToFirestore(String companyName, String hrName, String contact, String about, String logoUrl, String address, String website) {
        Map<String, Object> data = new HashMap<>();
        data.put("companyName", companyName);
        data.put("hrName", hrName);
        data.put("contact", contact);
        data.put("about", about);
        if (logoUrl != null) data.put("logoUrl", logoUrl);
        if (!TextUtils.isEmpty(address)) data.put("address", address);
        if (!TextUtils.isEmpty(website)) data.put("website", website);

        firestore.collection("employers").document(currentUserEmail)
                .set(data)
                .addOnSuccessListener(unused -> Toast.makeText(getContext(), "Profile saved", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error saving profile", Toast.LENGTH_SHORT).show());
    }

    private void loadProfile() {
        firestore.collection("employers").document(currentUserEmail).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        editCompanyName.setText(documentSnapshot.getString("companyName"));
                        String hrName = documentSnapshot.getString("hrName");

                        if (hrName != null && !hrName.isEmpty()) {
                            editHrName.setText(hrName);
                        } else {
                            firestore.collection("users").document(currentUserEmail)
                                    .get()
                                    .addOnSuccessListener(userSnap -> {
                                        if (userSnap.exists()) {
                                            String registeredName = userSnap.getString("name");
                                            if (registeredName != null && !registeredName.isEmpty()) {
                                                editHrName.setText(registeredName);
                                            }
                                        }
                                    });
                        }

                        editContact.setText(documentSnapshot.getString("contact"));
                        editAbout.setText(documentSnapshot.getString("about"));
                        editAddress.setText(documentSnapshot.getString("address"));
                        editWebsite.setText(documentSnapshot.getString("website"));
                        currentLogoUrl = documentSnapshot.getString("logoUrl");

                        if (isAdded() && currentLogoUrl != null && !currentLogoUrl.isEmpty()) {
                            Glide.with(requireContext())
                                    .load(currentLogoUrl)
                                    .transform(new CircleCrop())
                                    .placeholder(R.drawable.company_avatar)
                                    .into(imageCompanyLogo);
                        }
                    }
                });
    }

    private void removeLogo() {
        if (currentLogoUrl != null && !currentLogoUrl.isEmpty()) {
            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(currentLogoUrl);

            photoRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        firestore.collection("employers").document(currentUserEmail)
                                .update("logoUrl", null)
                                .addOnSuccessListener(unused -> {
                                    imageCompanyLogo.setImageResource(R.drawable.company_avatar);
                                    tvUploadStatus.setText("Logo removed successfully!");
                                    Toast.makeText(getContext(), "Logo removed", Toast.LENGTH_SHORT).show();
                                    currentLogoUrl = null;
                                });
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to remove logo!", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(getContext(), "No logo to remove", Toast.LENGTH_SHORT).show();
        }
    }
}
