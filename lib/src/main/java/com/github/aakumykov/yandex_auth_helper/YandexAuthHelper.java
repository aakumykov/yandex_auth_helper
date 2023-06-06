package com.github.aakumykov.yandex_auth_helper;

import static com.github.aakumykov.argument_utils.ArgumentUtils.checkNotNull;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils;
import com.yandex.authsdk.YandexAuthException;
import com.yandex.authsdk.YandexAuthLoginOptions;
import com.yandex.authsdk.YandexAuthOptions;
import com.yandex.authsdk.YandexAuthSdk;
import com.yandex.authsdk.YandexAuthToken;

public class YandexAuthHelper {

    public interface Callbacks {
        void onYandexAuthSuccess(@NonNull String authToken);
        void onYandexAuthFailed(@NonNull String errorMsg);
    }

    private static final String TAG = YandexAuthHelper.class.getSimpleName();
    private final int mLoginRequestCode;
    private YandexAuthSdk mYandexAuthSdk;
    private final Activity mActivity;
    private final Context mContext;
    @Nullable
    private final Callbacks mCallbacks;

    public YandexAuthHelper(@NonNull Activity activity,
                            int requestCode,
                            @Nullable Callbacks callbacks
    ) {
        mActivity = checkNotNull(activity);
        mLoginRequestCode = requestCode;
        mCallbacks = callbacks;

        mContext = activity;

        prepareYandexAuthSDK();
    }


    public void beginAuthorization() {

        final YandexAuthLoginOptions.Builder loginOptionsBuilder =  new YandexAuthLoginOptions.Builder();

        final Intent loginIntent = mYandexAuthSdk.createLoginIntent(loginOptionsBuilder.build());

        mActivity.startActivityForResult(loginIntent, mLoginRequestCode);
    }

    public void processAuthResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (mLoginRequestCode == requestCode)
            processYandexAuthorization(resultCode, data);
    }


    private void processYandexAuthorization(int resultCode, @Nullable Intent data) {
        try {
            final YandexAuthToken yandexAuthToken = mYandexAuthSdk.extractToken(resultCode, data);

            if (yandexAuthToken != null) {
                String authToken = yandexAuthToken.getValue();
                if (null != mCallbacks)
                    mCallbacks.onYandexAuthSuccess(authToken);
            }
        }
        catch (YandexAuthException e) {
            if (null != mCallbacks)
                mCallbacks.onYandexAuthFailed(ExceptionUtils.getErrorMessage(e));
            logErrors(e);
        }
    }

    private void prepareYandexAuthSDK() {
        mYandexAuthSdk = new YandexAuthSdk(mContext, yandexAuthOptions());
    }

    private YandexAuthOptions yandexAuthOptions() {
        return new YandexAuthOptions.Builder(mActivity)
                .enableLogging() // Only in testing builds
                .build();
    }

    private void logErrors(YandexAuthException yandexAuthException) {
        Log.e(TAG, "=== Ошибка авторизации в Яндекс ===");
        Log.e(TAG, ExceptionUtils.getErrorMessage(yandexAuthException));
        for (String eMsg : yandexAuthException.getErrors())
            Log.e(TAG, eMsg);
    }
}