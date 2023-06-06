package com.github.aakumykov.yandex_auth_helper_demo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.aakumykov.yandex_auth_helper.YandexAuthHelper;
import com.github.aakumykov.yandex_auth_helper_demo.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements YandexAuthHelper.Callbacks {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int AUTH_REQUEST_CODE = 10;
    private ActivityMainBinding mBinding;
    private YandexAuthHelper mYandexAuthHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mYandexAuthHelper = new YandexAuthHelper(this, AUTH_REQUEST_CODE, this);

        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mBinding.authButton.setOnClickListener(v -> onAuthButtonClicked());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTH_REQUEST_CODE)
            mYandexAuthHelper.processAuthResult(requestCode, resultCode, data);
    }

    private void onAuthButtonClicked() {
        showProgressBar();
        mYandexAuthHelper.beginAuthorization();
    }


    @Override
    public void onYandexAuthSuccess(@NonNull String authToken) {
        hideProgressBar();
        mBinding.authTokenView.setText(authToken);
        Log.d(TAG, "onYandexAuthSuccess: "+authToken);
    }

    @Override
    public void onYandexAuthFailed(@NonNull String errorMsg) {
        hideProgressBar();
        mBinding.errorView.setText(errorMsg);
        Log.e(TAG, "onYandexAuthFailed: "+errorMsg);
    }

    private void showProgressBar() {
        mBinding.progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        mBinding.progressBar.setVisibility(View.GONE);
    }
}