package com.connectify.connectify;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.firestore.FirebaseFirestore;

public class CompanyProfileActivity extends AppCompatActivity {

    private ImageView ivCompanyLogo;
    private TextView tvCompanyName, tvHrName, tvCompanyEmail, tvCompanyPhone, tvCompanyAddress, tvCompanyWebsite;
    private ImageView btnBack;

    private FirebaseFirestore db;
    private String employerEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_profile);

        // 🔗 Bind views
        ivCompanyLogo = findViewById(R.id.ivCompanyLogo);
        tvCompanyName = findViewById(R.id.tvCompanyName);
        tvHrName = findViewById(R.id.tvHrName);
        tvCompanyEmail = findViewById(R.id.tvCompanyEmail);
        tvCompanyPhone = findViewById(R.id.tvCompanyPhone);
        tvCompanyAddress = findViewById(R.id.tvCompanyAddress);
        tvCompanyWebsite = findViewById(R.id.tvCompanyWebsite);
        btnBack = findViewById(R.id.btnBack);

        db = FirebaseFirestore.getInstance();
        employerEmail = getIntent().getStringExtra("employerEmail");

        // 🚫 If email is not provided, exit
        if (employerEmail == null || employerEmail.trim().isEmpty()) {
            Toast.makeText(this, "Invalid employer data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 🔁 Load profile
        loadCompanyDetails();

        // 🔙 Back button action
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadCompanyDetails() {
        db.collection("employers").document(employerEmail).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String logoUrl = documentSnapshot.getString("logoUrl");
                        String companyName = documentSnapshot.getString("companyName");
                        String hrName = documentSnapshot.getString("hrName");
                        String phone = documentSnapshot.getString("contact");
                        String address = documentSnapshot.getString("address");
                        String website = documentSnapshot.getString("website");

                        if (companyName != null) tvCompanyName.setText(companyName);
                        if (hrName != null) tvHrName.setText("HR: " + hrName);
                        tvCompanyEmail.setText("Email: " + employerEmail);
                        tvCompanyPhone.setText("Phone: " + (phone != null ? phone : "Not Available"));
                        tvCompanyAddress.setText("Address: " + (address != null ? address : "Not Available"));
                        tvCompanyWebsite.setText("Website: " + (website != null ? website : "Not Available"));

                        if (logoUrl != null && !logoUrl.trim().isEmpty()) {
                            Glide.with(this)
                                    .load(logoUrl)
                                    .transform(new CircleCrop())
                                    .placeholder(R.drawable.company_avatar)
                                    .error(R.drawable.company_avatar)
                                    .into(ivCompanyLogo);
                        } else {
                            ivCompanyLogo.setImageResource(R.drawable.company_avatar);
                        }
                    } else {
                        Toast.makeText(this, "Company profile not found!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}
