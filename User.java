package com.example.alexk_000.ampapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;



public class User extends AppCompatActivity {
    private static Button logout_button;
    private static Button start_button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        LogoutButton();
        StartButton();
    }
    public void LogoutButton(){
        logout_button = (Button)findViewById(R.id.button_Logout);
        logout_button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(User.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }
        );
    }
    public void StartButton() {
        start_button = (Button)findViewById(R.id.button_Start);
        start_button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(User.this, connect_BT.class);
                        startActivity(intent);
                    }
                }
        );
    }
}
