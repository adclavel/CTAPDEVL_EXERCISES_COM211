package com.example.mainactivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void login (View v) {
        Intent i = new Intent(this, HomePageActivity.class);

        EditText t = findViewById(R.id.username);
        String name = t.getText().toString();
        i.putExtra("uname", name);

        TextView p = findViewById(R.id.password);
        String password = p.getText().toString();

        if (password.equals("password")) {
            startActivity(i);
        } else {
            Toast.makeText(this, "Wrong Password Entered", Toast.LENGTH_SHORT).show();
        }






    }
}

