package com.connectify.connectify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ChooseRoleActivity extends AppCompatActivity {

    private Button btnSeeker, btnEmployer;
    private String email, name;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_role);

        btnSeeker = findViewById(R.id.btnSeeker);
        btnEmployer = findViewById(R.id.btnEmployer);

        firestore = FirebaseFirestore.getInstance();
        email = getIntent().getStringExtra("email");
        name = getIntent().getStringExtra("name");

        btnSeeker.setOnClickListener(v -> saveUserAndRedirect("Seeker"));
        btnEmployer.setOnClickListener(v -> saveUserAndRedirect("Employer"));
    }

    private void saveUserAndRedirect(String role) {
        if (email == null || name == null) {
            Toast.makeText(this, "Invalid user info", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("name", name);
        userData.put("role", role);

        firestore.collection("users").document(email).set(userData)
                .addOnSuccessListener(unused -> {
                    Intent intent = role.equals("Employer")
                            ? new Intent(this, EmployerDashboardActivity.class)
                            : new Intent(this, JobSeekerDashboard.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save role: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
