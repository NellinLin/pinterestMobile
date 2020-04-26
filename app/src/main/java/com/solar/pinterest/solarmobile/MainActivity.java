package com.solar.pinterest.solarmobile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import com.google.gson.GsonBuilder;
import com.solar.pinterest.solarmobile.network.Network;
import com.solar.pinterest.solarmobile.network.models.LoginData;
import com.solar.pinterest.solarmobile.network.models.LoginResponse;
import com.solar.pinterest.solarmobile.network.models.User;
import com.solar.pinterest.solarmobile.storage.DBSchema;
import com.solar.pinterest.solarmobile.storage.SolarDatabase;
import com.solar.pinterest.solarmobile.storage.StorageInterface;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements StorageInterface.Listener{
    Button toRegistrationBtn;
    Button loginBtn;

    TextInputLayout textInputEmail;
    TextInputLayout textInputPassword;
    TextView errorTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        textInputEmail = findViewById(R.id.login_view_email_layout);
        textInputPassword = findViewById(R.id.login_view_password_layout);
        errorTextView = findViewById(R.id.login_error_text_under_title);

        toRegistrationBtn = findViewById(R.id.login_to_registration_button);

        toRegistrationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, YourProfileActivity.class);
                startActivity(intent);
            }
        });
        loginBtn = findViewById(R.id.login_view_button);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!confirmInput(v)) {
                    return;
                }

                LoginData loginData = new LoginData(textInputEmail.getEditText().getText().toString(), textInputPassword.getEditText().getText().toString());

                Callback loginCallback = new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        errorTextView.setText("Сервер временно недоступен");
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        GsonBuilder builder = new GsonBuilder();
                        Gson gson = builder.create();
                        LoginResponse loginResponse = gson.fromJson(response.body().string(), LoginResponse.class);
                        User user = loginResponse.body.user;

                        SolarDatabase.get(getApplication()).putUser(
                                new DBSchema.User(user.id, user.username, user.name, user.surname,
                                        user.email, user.age, user.status, user.avatarDir,
                                        user.isActive, user.createdTime));

                        Intent intent = new Intent(MainActivity.this, YourProfileActivity.class);
                        startActivity(intent);
                    }
                };

                Network.getInstance().login(loginData, loginCallback);
            }
        });

/*        SolarDatabase.get(getApplication()).putUser(
                new DBSchema.User(129, "Tamerlanchik", "Name", "Sur",
                        "aaa@ss.er", 123, "Alive", "dwe/dwedwe.jpg",
                        true, "2019-12-14 15:21")
        );
        SolarDatabase.get(getApplication()).getUser(129, this);*/
    }

    private boolean emailValidation() {
        String emailInput = textInputEmail.getEditText().getText().toString().trim();

        if (emailInput.isEmpty()) {
            textInputEmail.setError("Поле должно быть заполнено");
            return false;
        } else {
            textInputEmail.setError(null);
            return true;
        }
    }

    private boolean passwordValidation() {
        String passwordInput = textInputPassword.getEditText().getText().toString().trim();

        if (passwordInput.isEmpty()) {
            textInputPassword.setError("Поле должно быть заполнено");
            return false;
        } else {
            textInputPassword.setError(null);
            return true;
        }
    }

    public boolean confirmInput(View v) {
        if (!emailValidation() | !passwordValidation()) {
            return false;
        }

        String input = textInputEmail.getEditText().getText().toString();
        input += "\n";
        input += textInputPassword.getEditText().getText().toString();



        Toast.makeText(this, input, Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onReadUser(DBSchema.User user) {
        DBSchema.User ds = user;
        Log.d("Solar", "Got values");
    }
}
