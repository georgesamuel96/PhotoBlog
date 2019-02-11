package com.example.georgesamuel.photoblog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText regEmail;
    private EditText regPass;
    private EditText regConPass;
    private Button regBtn;
    private Button regLoginBtn;
    private ProgressBar regProcess;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        regEmail = findViewById(R.id.reg_email);
        regPass = findViewById(R.id.reg_pass);
        regConPass = findViewById(R.id.reg_confirm_pass);
        regBtn = findViewById(R.id.reg_btn);
        regLoginBtn = findViewById(R.id.reg_login_btn);
        regProcess = findViewById(R.id.reg_progress);

        mAuth = FirebaseAuth.getInstance();

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = regEmail.getText().toString().trim();
                String pass = regPass.getText().toString().trim();
                String conPass = regConPass.getText().toString().trim();

                if(!email.equals("") && !pass.equals("") && !conPass.equals("")){
                    if(pass.equals(conPass)){
                        regProcess.setVisibility(View.VISIBLE);
                        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    startActivity(new Intent(RegisterActivity.this, SetupActivity.class));
                                    finish();
                                }
                                else{
                                    String errorMessage = task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                        regProcess.setVisibility(View.INVISIBLE);
                    }
                    else{
                        Toast.makeText(RegisterActivity.this, "Confirm password and Password doesn't match", Toast.LENGTH_LONG)
                                .show();
                        regPass.setText("");
                        regConPass.setText("");
                    }
                }
                else{

                }
            }
        });

        regLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            sendToMain();
        }

    }

    private void sendToMain() {

        Intent i = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(i);
        finish();

    }

}
