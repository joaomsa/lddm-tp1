package com.joaomsa.tp1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookRequestError;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class ConnectActivity extends AppCompatActivity {

    private CallbackManager callbackManager;
    private boolean isFbLoggedIn;

    TextView fbLoginStatus;
    LoginButton fbLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        callbackManager = CallbackManager.Factory.create();

        fbLoginButton = findViewById(R.id.fb_login_button);
        fbLoginStatus = findViewById(R.id.fb_login_status);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AccessToken fbAccessToken = AccessToken.getCurrentAccessToken();
        isFbLoggedIn = fbAccessToken != null && !fbAccessToken.isExpired();

        if (!isFbLoggedIn)
        {
            fbLoginStatus.setText("Não conectado ao Facebook");
            setupFbLogin();
        }
        else
            updateFbLoginStatus();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    protected void updateFbLoginStatus()
    {
        //fbLoginButton.setVisibility(View.GONE);
        fbLoginStatus.setText("Conferindo conexão com Facebook");

        final AccessToken fbAccessToken = AccessToken.getCurrentAccessToken();
        Bundle params = new Bundle();
        params.putString("fields", "email");

        GraphRequest request = new GraphRequest(fbAccessToken, "/me", params, HttpMethod.GET);
        request.setCallback(new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                JSONObject userObj = response.getJSONObject();
                if (userObj != null)
                    try {
                        String email = userObj.getString("email");
                        fbLoginStatus.setText("Conectado ao Facebook como: " + email);
                        return;
                    }
                    catch (JSONException e) { }

                FacebookRequestError fbError = response.getError();
                if (fbError != null)
                    fbLoginStatus.setText("Facebook respondeu com erro: " + fbError.getErrorRecoveryMessage());
                else
                    fbLoginStatus.setText("Houve um erro com a resposta do Facebook");

                setupFbLogin();
            }
        });

        request.executeAsync();
    }

    protected void setupFbLogin()
    {
        //fbLoginButton.setVisibility(View.VISIBLE);

        // Set the initial permissions to request from the user while logging in
        fbLoginButton.setReadPermissions(Arrays.asList("email"));
        fbLoginButton.setAuthType("rerequest");

        // Register a callback to respond to the user
        //loginButton.registerCallback(callbackManager, this);
        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                setResult(RESULT_OK);
                AccessToken.refreshCurrentAccessTokenAsync(new AccessToken.AccessTokenRefreshCallback() {
                    @Override
                    public void OnTokenRefreshed(AccessToken accessToken) {
                        updateFbLoginStatus();
                    }

                    @Override
                    public void OnTokenRefreshFailed(FacebookException exception) {
                    }
                });
            }

            @Override
            public void onCancel() {
                setResult(RESULT_CANCELED);
            }

            @Override
            public void onError(FacebookException e) {
                // Handle exception
            }
        });
    }
}
