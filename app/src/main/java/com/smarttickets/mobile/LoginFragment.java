package com.smarttickets.mobile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;


public class LoginFragment extends Fragment {

    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private TextView textViewFPassword, textViewSignUp;
    private static final String TAG = "LoginActivity";

    private String uUsername;
    private String uPassword;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View vi =  inflater.inflate(R.layout.fragment_login, container, false);

        Log.v("LoginActivity", "PREFERENCE FOR LOG IN + " + SaveSharedPreference.getUserLoggedInStatus(getActivity()));
        if(SaveSharedPreference.getUserLoggedInStatus(getActivity()) == true)
        {
            uUsername = SaveSharedPreference.getLoggedInUserUserName(getActivity());
            uPassword = SaveSharedPreference.getLoggedInUserPassword(getActivity());
            Intent i = new Intent(getActivity(), DashboardActivity.class);
            startActivity(i);
            getActivity().finish();
        }

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) vi.findViewById(R.id.email);
        textViewFPassword = (TextView) vi.findViewById(R.id.txtForgotPassword);
        textViewSignUp = (TextView) vi.findViewById(R.id.txtSignUp);

        textViewFPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pChangeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://smartkts.com/accounts/forgot-password/"));
                startActivity(pChangeIntent);
            }
        });
        textViewSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pChangeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://smartkts.com/accounts/register/"));
                startActivity(pChangeIntent);
            }
        });

        mPasswordView = (EditText) vi.findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) vi.findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = vi.findViewById(R.id.login_form);
        mProgressView = vi.findViewById(R.id.login_progress);

        return vi;
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

            JSONObject post_dict = new JSONObject();

            try {
                post_dict.put("username" , Base64.encodeToString(email.getBytes(), Base64.DEFAULT));
                post_dict.put("password", Base64.encodeToString(password.getBytes(), Base64.DEFAULT));
                post_dict.put("type", "login");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            showProgress(true);
            mAuthTask = new UserLoginTask();
            mAuthTask.execute(String.valueOf(post_dict));
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public void loadHome(String response){
        try {
            JSONObject obj = new JSONObject(response);
            Log.d(TAG, obj.toString());
            String jsonStatus = obj.getString("status").toString();

            if(jsonStatus.equals("GOOD")){
                getActivity().finish();
                showProgress(false);
                SaveSharedPreference.setLoggedInUserUserName(getActivity(), Base64.encodeToString(mEmailView.getText().toString().getBytes(), Base64.DEFAULT));
                SaveSharedPreference.setLoggedInUserPassword(getActivity(), Base64.encodeToString(mPasswordView.getText().toString().getBytes(), Base64.DEFAULT));
                SaveSharedPreference.setUserLoggedInStatus(getActivity(), true);
                Intent intent = new Intent(getActivity(), DashboardActivity.class);
                intent.putExtra("DATA", obj.getString("data").toString());
                startActivity(intent);
                getActivity().finish();

            }else{
                showProgress(false);
                System.out.println("Bad response >>> " + jsonStatus + " " + response);
                Toast.makeText(getActivity(), "Error " + obj.getString("data").toString(), Toast.LENGTH_LONG);
                mEmailView.requestFocus();
                myDialog(obj.getString("data").toString());
            }
        } catch (Throwable t) {
            Log.e(TAG, "Could not parse malformed JSON: \"" + response + "\"");
            Config.noInternetDialog(getActivity());
        }
        showProgress(false);
    }

    private void myDialog(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Warning");
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<String, String, String> {

        /*** private final String mEmail;
         private final String mPassword;

         private final String url = Config.URL;


         UserLoginTask(String email, String password) {
         mEmail = email;
         mPassword = password;
         }**/

        String system_url = Config.URL;

        @Override
        protected String doInBackground(String... params) {
            String JsonResponse = null;
            String JsonDATA = params[0];

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(system_url);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                // is output buffer writter
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                //set headers and method
                Writer writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
                writer.write(JsonDATA);
                // json data
                writer.close();
                InputStream inputStream = urlConnection.getInputStream();
                //input stream
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String inputLine;
                while ((inputLine = reader.readLine()) != null)
                    buffer.append(inputLine + "\n");
                if (buffer.length() == 0) {
                    // Stream was empty. No point in parsing.
                    return null;
                }
                JsonResponse = buffer.toString();
                //response data
                Log.i(TAG,JsonResponse);
                //send to post execute
                return JsonResponse;
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            mAuthTask = null;
            System.out.println("Haaaaleluya>>> "+ response);
            loadHome(response);

            /**if (success=="") {
             loadHome();
             finish();
             } else {
             mPasswordView.setError(getString(R.string.error_incorrect_password));
             mPasswordView.requestFocus();
             }**/
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}
