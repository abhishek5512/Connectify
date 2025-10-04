package com.connectify.connectify;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String email = currentUser.getEmail();
            firestore.collection("users").document(email).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String role = doc.getString("role");
                            Intent intent = role.equals("Employer")
                                    ? new Intent(this, EmployerDashboardActivity.class)
                                    : new Intent(this, JobSeekerDashboard.class);
                            startActivity(intent);
                            finish();
                        } else {
                            mAuth.signOut();
                            redirectToLogin();
                        }
                    }).addOnFailureListener(e -> {
                        mAuth.signOut();
                        redirectToLogin();
                    });
        } else {
            redirectToLogin();
        }
    }

    private void redirectToLogin() {
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }
}
