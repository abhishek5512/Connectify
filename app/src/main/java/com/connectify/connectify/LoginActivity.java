package com.connectify.connectify;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.identity.*;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private ImageView eyeIcon;
    private TextView tvRegister, tvForgotPassword;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private final String TAG = "GOOGLE_LOGIN";
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        progressBar = findViewById(R.id.progressBar);
        eyeIcon = findViewById(R.id.btnTogglePassword);

        // Toggle password visibility
        eyeIcon.setOnClickListener(view -> {
            isPasswordVisible = !isPasswordVisible;
            etPassword.setTransformationMethod(isPasswordVisible ? null : new PasswordTransformationMethod());
            eyeIcon.setImageResource(isPasswordVisible ? R.drawable.ic_visibility : R.drawable.ic_visibility_off);
        });

        // Email/Password Login
        btnLogin.setOnClickListener(view -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Email required");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Password required");
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    openDashboard(email);
                } else {
                    Toast.makeText(this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Register redirect
        tvRegister.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        // Forgot Password
        tvForgotPassword.setOnClickListener(view -> {
            String email = etEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Enter your email to reset password");
                return;
            }
            mAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(unused -> Toast.makeText(this, "Reset link sent to your email", Toast.LENGTH_LONG).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
        });

        setupGoogleSignIn();
        findViewById(R.id.btnGoogleSignIn).setOnClickListener(view -> oneTapSignIn());
    }

    private void setupGoogleSignIn() {
        oneTapClient = Identity.getSignInClient(this);
        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.default_web_client_id))
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .build();
    }

    private void oneTapSignIn() {
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(result -> {
                    IntentSenderRequest intentSenderRequest =
                            new IntentSenderRequest.Builder(result.getPendingIntent().getIntentSender()).build();
                    googleSignInLauncher.launch(intentSenderRequest);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "One Tap failed", e);
                    Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show();
                });
    }

    private final ActivityResultLauncher<IntentSenderRequest> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    try {
                        SignInCredential credential = Identity.getSignInClient(this)
                                .getSignInCredentialFromIntent(result.getData());
                        String idToken = credential.getGoogleIdToken();
                        String email = credential.getId();
                        String name = credential.getDisplayName();

                        if (idToken != null) {
                            AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                            mAuth.signInWithCredential(firebaseCredential).addOnSuccessListener(authResult -> {
                                checkOrCreateUser(email, name);
                            }).addOnFailureListener(e ->
                                    Toast.makeText(this, "Firebase Auth Failed", Toast.LENGTH_SHORT).show());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing Google credentials", e);
                    }
                }
            });

    private void checkOrCreateUser(String email, String name) {
        firestore.collection("users").document(email).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        openDashboard(email);
                    } else {
                        // New Google Sign-In → Ask role first
                        Intent intent = new Intent(this, ChooseRoleActivity.class);
                        intent.putExtra("email", email);
                        intent.putExtra("name", name);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    private void openDashboard(String email) {
        firestore.collection("users").document(email).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String role = doc.getString("role");
                        Intent intent = role.equals("Employer")
                                ? new Intent(this, EmployerDashboardActivity.class)
                                : new Intent(this, JobSeekerDashboard.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }
}
