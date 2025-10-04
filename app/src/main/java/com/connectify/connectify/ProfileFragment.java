package com.connectify.connectify;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import com.google.firebase.storage.*;

import java.io.IOException;
import java.util.*;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_RESUME_REQUEST = 2;

    private ImageView profileImageView;
    private EditText etName, etAge, etQualification, etWorkExperience, etSkillInput;
    private Spinner spinnerGender;
    private Button btnEditPhoto, btnRemovePhoto, btnAddSkill, btnSaveProfile, btnUploadResume, btnRemoveResume;
    private TextView tvResumeName, tvUploadImageStatus, tvUploadResumeStatus;
    private LinearLayout skillsContainer;

    private Uri imageUri, resumeUri;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private List<String> skillsList = new ArrayList<>();
    private String profileImageUrl, resumeUrl, userEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        if (user != null) userEmail = user.getEmail();

        profileImageView = view.findViewById(R.id.profileImageView);
        etName = view.findViewById(R.id.etName);
        etAge = view.findViewById(R.id.etAge);
        etQualification = view.findViewById(R.id.etQualification);
        etWorkExperience = view.findViewById(R.id.etWorkExperience);
        etSkillInput = view.findViewById(R.id.etSkillInput);
        spinnerGender = view.findViewById(R.id.spinnerGender);
        skillsContainer = view.findViewById(R.id.skillsContainer);
        btnEditPhoto = view.findViewById(R.id.btnEditPhoto);
        btnRemovePhoto = view.findViewById(R.id.btnRemovePhoto);
        btnAddSkill = view.findViewById(R.id.btnAddSkill);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnUploadResume = view.findViewById(R.id.btnUploadResume);
        btnRemoveResume = view.findViewById(R.id.btnRemoveResume);
        tvResumeName = view.findViewById(R.id.tvResumeName);
        tvUploadImageStatus = view.findViewById(R.id.tvUploadImageStatus);
        tvUploadResumeStatus = view.findViewById(R.id.tvUploadResumeStatus);

        setupGenderSpinner();
        loadUserProfile();

        profileImageView.setOnClickListener(v -> openGallery());
        btnEditPhoto.setOnClickListener(v -> openGallery());
        btnRemovePhoto.setOnClickListener(v -> removeProfilePhoto());
        btnAddSkill.setOnClickListener(v -> addSkill());
        btnSaveProfile.setOnClickListener(v -> saveProfile());
        btnUploadResume.setOnClickListener(v -> openResumePicker());
        btnRemoveResume.setOnClickListener(v -> removeResume());

        return view;
    }

    private void setupGenderSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Select Gender", "Male", "Female", "Other"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void openResumePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(Intent.createChooser(intent, "Select Resume"), PICK_RESUME_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                imageUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                    profileImageView.setImageBitmap(bitmap);
                    uploadImageToFirebase();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == PICK_RESUME_REQUEST) {
                resumeUri = data.getData();
                uploadResumeToFirebase();
            }
        }
    }

    private void uploadImageToFirebase() {
        if (imageUri != null && userEmail != null) {
            String fileName = "profile_images/" + userEmail + "_" + UUID.randomUUID();
            StorageReference fileRef = storageReference.child(fileName);

            fileRef.putFile(imageUri)
                    .addOnProgressListener(snapshot -> {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        tvUploadImageStatus.setText("Uploading Photo: " + (int) progress + "%");
                    })
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        profileImageUrl = uri.toString();
                        tvUploadImageStatus.setText("Upload Complete!");
                        saveImageUrlToFirestore(profileImageUrl);
                    }))
                    .addOnFailureListener(e -> tvUploadImageStatus.setText("Upload Failed!"));
        }
    }

    private void uploadResumeToFirebase() {
        if (resumeUri != null && userEmail != null) {
            String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(requireActivity().getContentResolver().getType(resumeUri));
            String fileName = "resumes/" + userEmail + "_resume." + extension;
            StorageReference fileRef = storageReference.child(fileName);

            fileRef.putFile(resumeUri)
                    .addOnProgressListener(snapshot -> {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        tvUploadResumeStatus.setText("Uploading Resume: " + (int) progress + "%");
                    })
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        resumeUrl = uri.toString();
                        tvResumeName.setText("Uploaded: " + fileName.substring(fileName.lastIndexOf('/') + 1));
                        tvUploadResumeStatus.setText("Upload Complete!");
                        saveResumeUrlToFirestore(resumeUrl);
                        toggleResumeButtons(true);
                    }))
                    .addOnFailureListener(e -> tvUploadResumeStatus.setText("Upload Failed!"));
        }
    }

    private void removeResume() {
        if (resumeUrl != null) {
            StorageReference resumeRef = storage.getReferenceFromUrl(resumeUrl);
            resumeRef.delete()
                    .addOnSuccessListener(unused -> {
                        db.collection("users").document(userEmail).update("resumeUrl", FieldValue.delete());
                        resumeUrl = null;
                        tvResumeName.setText("No file selected");
                        tvUploadResumeStatus.setText("");
                        toggleResumeButtons(false);
                        Toast.makeText(getContext(), "Resume removed", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to remove resume", Toast.LENGTH_SHORT).show());
        }
    }

    private void toggleResumeButtons(boolean uploaded) {
        btnUploadResume.setVisibility(uploaded ? View.GONE : View.VISIBLE);
        btnRemoveResume.setVisibility(uploaded ? View.VISIBLE : View.GONE);
    }

    private void saveImageUrlToFirestore(String imageUrl) {
        db.collection("users").document(userEmail)
                .set(Collections.singletonMap("profileImage", imageUrl), SetOptions.merge());
    }

    private void saveResumeUrlToFirestore(String resumeUrl) {
        db.collection("users").document(userEmail)
                .set(Collections.singletonMap("resumeUrl", resumeUrl), SetOptions.merge());
    }

    private void removeProfilePhoto() {
        if (profileImageUrl != null) {
            StorageReference imageRef = storage.getReferenceFromUrl(profileImageUrl);
            imageRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        db.collection("users").document(userEmail).update("profileImage", FieldValue.delete());
                        profileImageUrl = null;
                        profileImageView.setImageResource(R.drawable.ic_default_profile);
                        tvUploadImageStatus.setText("");
                        Toast.makeText(getContext(), "Profile photo removed!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to remove photo!", Toast.LENGTH_SHORT).show());
        }
    }

    private void addSkill() {
        String skill = etSkillInput.getText().toString().trim();
        if (!skill.isEmpty() && skillsList.size() < 5) {
            skillsList.add(skill);
            displaySkills();
            etSkillInput.setText("");
        } else {
            Toast.makeText(getContext(), "Max 5 skills allowed!", Toast.LENGTH_SHORT).show();
        }
    }

    private void displaySkills() {
        skillsContainer.removeAllViews();
        for (String skill : skillsList) {
            View skillView = getLayoutInflater().inflate(R.layout.skill_item, skillsContainer, false);
            TextView skillText = skillView.findViewById(R.id.skillText);
            ImageView deleteSkill = skillView.findViewById(R.id.btnDeleteSkill);

            skillText.setText(skill);
            deleteSkill.setOnClickListener(v -> {
                skillsList.remove(skill);
                displaySkills();
            });

            skillsContainer.addView(skillView);
        }
    }

    private void saveProfile() {
        if (userEmail == null) return;

        String gender = spinnerGender.getSelectedItem().toString();
        if (gender.equals("Select Gender")) {
            Toast.makeText(getContext(), "Please select a gender", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("name", etName.getText().toString().trim());
        userProfile.put("age", etAge.getText().toString().trim());
        userProfile.put("qualification", etQualification.getText().toString().trim());
        userProfile.put("workExperience", etWorkExperience.getText().toString().trim());
        userProfile.put("skills", skillsList);
        userProfile.put("gender", gender);

        db.collection("users").document(userEmail).set(userProfile, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Profile saved!", Toast.LENGTH_SHORT).show());
    }

    private void loadUserProfile() {
        if (userEmail == null || !isAdded()) return;

        db.collection("users").document(userEmail).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded()) return;

                    if (documentSnapshot.exists()) {
                        profileImageUrl = documentSnapshot.getString("profileImage");
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(requireContext()).load(profileImageUrl).transform(new CircleCrop()).into(profileImageView);
                        }

                        etName.setText(documentSnapshot.getString("name"));
                        etAge.setText(documentSnapshot.getString("age"));
                        etQualification.setText(documentSnapshot.getString("qualification"));
                        etWorkExperience.setText(documentSnapshot.getString("workExperience"));

                        resumeUrl = documentSnapshot.getString("resumeUrl");
                        if (resumeUrl != null) {
                            tvResumeName.setText("Uploaded");
                            toggleResumeButtons(true);
                        } else {
                            toggleResumeButtons(false);
                        }

                        String gender = documentSnapshot.getString("gender");
                        if (gender != null) {
                            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerGender.getAdapter();
                            int position = adapter.getPosition(gender);
                            spinnerGender.setSelection(position);
                        }

                        skillsList = (List<String>) documentSnapshot.get("skills");
                        if (skillsList == null) skillsList = new ArrayList<>();
                        displaySkills();
                    }
                });
    }
}
