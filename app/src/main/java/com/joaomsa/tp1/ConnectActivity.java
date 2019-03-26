package com.joaomsa.tp1;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

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

    private RequestQueue requestQueue;
    private CallbackManager callbackManager;
    private boolean isFbLoggedIn;

    TextView fbLoginStatus;
    LoginButton fbLoginButton;

    private AuthenticationDialog igAuthenticationDialog = null;

    TextView igLoginStatus;
    Button igLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        requestQueue = Volley.newRequestQueue(this);
        callbackManager = CallbackManager.Factory.create();

        fbLoginStatus = findViewById(R.id.fb_login_status);
        fbLoginButton = findViewById(R.id.fb_login_button);

        igLoginStatus = findViewById(R.id.ig_login_status);
        igLoginButton = findViewById(R.id.ig_login_button);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AccessToken fbAccessToken = AccessToken.getCurrentAccessToken();
        isFbLoggedIn = fbAccessToken != null && !fbAccessToken.isExpired();


        onResumeFacebook();
        onResumeInstagram();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    protected void onResumeFacebook()
    {
        if (!isFbLoggedIn)
        {
            fbLoginStatus.setText("Não conectado ao Facebook");
            setupFacebookLogin();
        }
        else
            updateFacebookLoginStatus();
    }

    protected void onResumeInstagram()
    {
        igLoginStatus.setText("Não conectado ao Instagram");
        setupIgLogin();
    }

    protected void updateFacebookLoginStatus()
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

                setupFacebookLogin();
            }
        });

        request.executeAsync();
    }

    protected void setupFacebookLogin()
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
                        updateFacebookLoginStatus();
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

    protected void setupIgLogin()
    {
        final Context self = this;
        igLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                igAuthenticationDialog = new AuthenticationDialog(self, new IAuthenticationListener() {
                    @Override
                    public void onTokenReceived(String auth_token) {
                        updateInstagramLoginStatus(auth_token);
                    }
                });
                igAuthenticationDialog.setCancelable(true);
                igAuthenticationDialog.show();
            }
        });
    }

    protected void updateInstagramLoginStatus(String auth_token)
    {
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET,
                getResources().getString(R.string.ig_base_url) + "v1/users/self/?access_token=" + auth_token,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject data = response.getJSONObject("data");
                            if (data.has("username")){
                                String username = data.getString("username");
                                igLoginStatus.setText("Conectado ao Instagram como: " + username);
                            }
                        } catch (JSONException jex) {
                            jex.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                igLoginStatus.setText("Houve um erro com a resposta do Instagram");
            }
        });

        requestQueue.add(req);
    }
}
