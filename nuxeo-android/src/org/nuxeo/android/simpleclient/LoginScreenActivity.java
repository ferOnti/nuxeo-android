package org.nuxeo.android.simpleclient;

import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.smartnsoft.droid4me.app.AppPublics;
import com.smartnsoft.droid4me.app.SmartActivity;
import com.smartnsoft.droid4me.app.AppPublics.BroadcastListener;
import com.smartnsoft.droid4me.framework.LifeCycle.BusinessObjectUnavailableException;

public class LoginScreenActivity extends
        SmartActivity<NuxeoAndroidApplication.TitleBarAggregate> implements
        AppPublics.SendLoadingIntent, AppPublics.BroadcastListenerProvider,
        View.OnClickListener {

    private EditText editPassword;

    private EditText editLogin;

    private EditText editServerUrl;

    private Button buttonLogin;

    public BroadcastListener getBroadcastListener() {
        return new AppPublics.LoadingBroadcastListener(this, true) {
            @Override
            protected void onLoading(boolean isLoading) {
                getAggregate().getAttributes().toggleRefresh(isLoading);
            }
        };
    }

    @Override
    public void onRetrieveDisplayObjects() {
        setContentView(R.layout.login_screen);

        editServerUrl = (EditText) findViewById(R.id.editServerUrl);
        editLogin = (EditText) findViewById(R.id.editLogin);
        editPassword = (EditText) findViewById(R.id.editPassword);
        buttonLogin = (Button) findViewById(R.id.loginButton);
    }

    @Override
    public void onRetrieveBusinessObjects()
            throws BusinessObjectUnavailableException {

    }

    @Override
    public void onFulfillDisplayObjects() {
        editLogin.setText(getPreferences().getString(
                SettingsActivity.PREF_LOGIN, ""));
        editPassword.setText(getPreferences().getString(
                SettingsActivity.PREF_PASSWORD, ""));
        editServerUrl.setText(getPreferences().getString(
                SettingsActivity.PREF_SERVER_URL, ""));
        buttonLogin.setOnClickListener(this);
    }

    @Override
    public void onSynchronizeDisplayObjects() {
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.loginButton:
            final Editor editor = getPreferences().edit();

            editor.putString(SettingsActivity.PREF_LOGIN,
                    editLogin.getText().toString());
            editor.putString(SettingsActivity.PREF_PASSWORD,
                    editPassword.getText().toString());
            editor.putString(SettingsActivity.PREF_SERVER_URL,
                    editServerUrl.getText().toString());
            editor.commit();

            startActivity(new Intent(this, HomeActivity.class));
            finish();
            break;

        default:
            break;
        }
    }

}