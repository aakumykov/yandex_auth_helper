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
import com.yandex.authsdk.YandexAuthResult;
import com.yandex.authsdk.YandexAuthSdkContract;

public class YandexAuthHelper {

    public interface Callbacks {
        void onYandexAuthSuccess(@NonNull String authToken);
        void onYandexAuthFailed(@NonNull String errorMsg);
    }

    private static final String TAG = YandexAuthHelper.class.getSimpleName();
    private final int mLoginRequestCode;
    private YandexAuthSdkContract mYandexAuthSdkContract;
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

        final YandexAuthLoginOptions yandexAuthLoginOptions = new YandexAuthLoginOptions();

        final Intent loginIntent = mYandexAuthSdkContract.createIntent(mContext, yandexAuthLoginOptions);

        mActivity.startActivityForResult(loginIntent, mLoginRequestCode);
    }

    public void processAuthResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (mLoginRequestCode == requestCode)
            processYandexAuthorization(resultCode, data);
    }


    private void processYandexAuthorization(int resultCode, @Nullable Intent data) {
        try {
//            final YandexAuthToken yandexAuthToken = mYandexAuthSdk.extractToken(resultCode, data);
            final YandexAuthResult yandexAuthResult = mYandexAuthSdkContract.parseResult(resultCode, data);

            if (yandexAuthResult instanceof YandexAuthResult.Success) {
                final YandexAuthResult.Success sr = (YandexAuthResult.Success) yandexAuthResult;
                String authToken = sr.getToken().getValue();
                if (null != mCallbacks)
                    mCallbacks.onYandexAuthSuccess(authToken);
            }
            else if (yandexAuthResult instanceof YandexAuthResult.Failure) {
                final YandexAuthResult.Failure fr = (YandexAuthResult.Failure) yandexAuthResult;
                throw new YandexAuthException(ExceptionUtils.getErrorMessage(fr.getException()));
            }
            else if (yandexAuthResult instanceof YandexAuthResult.Cancelled) {
                final YandexAuthResult.Cancelled cr = (YandexAuthResult.Cancelled) yandexAuthResult;
                throw new YandexAuthException("Auth was cancelled.");
            }
        }
        catch (YandexAuthException e) {
            if (null != mCallbacks)
                mCallbacks.onYandexAuthFailed(ExceptionUtils.getErrorMessage(e));
            logErrors(e);
        }
    }

    private void prepareYandexAuthSDK() {
        mYandexAuthSdkContract = new YandexAuthSdkContract(yandexAuthOptions());
    }

    private YandexAuthOptions yandexAuthOptions() {
        return new YandexAuthOptions(mActivity);
    }

    private void logErrors(YandexAuthException yandexAuthException) {
        Log.e(TAG, "=== Ошибка авторизации в Яндекс ===");
        Log.e(TAG, ExceptionUtils.getErrorMessage(yandexAuthException));
        for (String eMsg : yandexAuthException.getErrors())
            Log.e(TAG, eMsg);
    }
}