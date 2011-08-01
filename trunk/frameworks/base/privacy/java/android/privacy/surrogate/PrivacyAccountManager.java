package android.privacy.surrogate;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.IAccountManager;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.privacy.PrivacySettings;
import android.privacy.PrivacySettingsManager;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public final class PrivacyAccountManager extends AccountManager {
    
    private final String TAG = "PrivacyAccountManager";
    private Context mContext;
    private PrivacySettingsManager mPrivSetManager;    

    public PrivacyAccountManager(Context context, IAccountManager service) {
        super(context, service);
        mContext = context;
        mPrivSetManager = (PrivacySettingsManager) context.getSystemService("privacy");  
    }

    public PrivacyAccountManager(Context context, IAccountManager service, Handler handler) {
        super(context, service, handler);
        mContext = context;
        mPrivSetManager = (PrivacySettingsManager) context.getSystemService("privacy");
    }

    /**
     * GET_ACCOUNTS
     */
    
    @Override
    public Account[] getAccounts() {
        PrivacySettings pSet = mPrivSetManager.getSettings(mContext.getPackageName(), Binder.getCallingUid());
        String output_label;
        Account[] output;
        
        if (pSet != null && pSet.getAccountsSetting() != PrivacySettings.REAL) {
            output_label = "[empty accounts list]";
            output = new Account[0];
        } else {
            output_label = "[real value]";
            output = super.getAccounts(); 
        }
        
        Log.d(TAG, "getAccounts - " + mContext.getPackageName() + " (" + Binder.getCallingUid() + ") output: " + output_label);        
        return output;
    }

    @Override
    public Account[] getAccountsByType(String type) {
        PrivacySettings pSet = mPrivSetManager.getSettings(mContext.getPackageName(), Binder.getCallingUid());
        String output_label;
        Account[] output;
        
        if (pSet != null && pSet.getAccountsSetting() != PrivacySettings.REAL) {
            output_label = "[empty accounts list]";
            output = new Account[0];
        } else {
            output_label = "[real value]";
            output = super.getAccountsByType(type);
        }
        
        Log.d(TAG, "getAccountsByType - " + mContext.getPackageName() + " (" + Binder.getCallingUid() + ") output: " + output_label);        
        return output;
    }
    
    @Override
    public AccountManagerFuture<Boolean> hasFeatures(Account account, String[] features,
            AccountManagerCallback<Boolean> callback, Handler handler) {
        PrivacySettings pSet = mPrivSetManager.getSettings(mContext.getPackageName(), Binder.getCallingUid());
        String output_label;
        AccountManagerFuture<Boolean> output;
        
        if (pSet != null && pSet.getAccountsSetting() != PrivacySettings.REAL) {
            output_label = "[false]";
            output = new PrivacyAccountManagerFuture<Boolean>(false);
        } else {
            output_label = "[real value]";
            output = super.hasFeatures(account, features, callback, handler);
        }
        
        Log.d(TAG, "hasFeatures - " + mContext.getPackageName() + " (" + Binder.getCallingUid() + ") output: " + output_label);        
        return output;
    }

    @Override
    public AccountManagerFuture<Account[]> getAccountsByTypeAndFeatures(String type, String[] features,
            AccountManagerCallback<Account[]> callback, Handler handler) {
        PrivacySettings pSet = mPrivSetManager.getSettings(mContext.getPackageName(), Binder.getCallingUid());
        String output_label;
        AccountManagerFuture<Account[]> output;
        
        if (pSet != null && pSet.getAccountsSetting() != PrivacySettings.REAL) {
            output_label = "[false]";
            output = new PrivacyAccountManagerFuture<Account[]>(new Account[0]);
        } else {
            output_label = "[real value]";
            output = super.getAccountsByTypeAndFeatures(type, features, callback, handler);
        }
        
        Log.d(TAG, "getAccountsByTypeAndFeatures - " + mContext.getPackageName() + " (" + Binder.getCallingUid() + ") output: " + output_label);           
        return output;
    }
    
    /**
     * USE_CREDENTIALS
     */
    
    @Override
    public String blockingGetAuthToken(Account account, String authTokenType, boolean notifyAuthFailure)
            throws OperationCanceledException, IOException, AuthenticatorException {
        PrivacySettings pSet = mPrivSetManager.getSettings(mContext.getPackageName(), Binder.getCallingUid());
        String output;
        
        if (pSet != null && pSet.getAccountsAuthTokensSetting() != PrivacySettings.REAL) {
            output = null;
        } else {
            output = super.blockingGetAuthToken(account, authTokenType, notifyAuthFailure);
        }
        
        Log.d(TAG, "blockingGetAuthToken - " + mContext.getPackageName() + " (" + Binder.getCallingUid() + ") output: " 
                + (output == null ? "[null]" : output));
        return output;
    }

    @Override
    public AccountManagerFuture<Bundle> getAuthToken(Account account, String authTokenType, boolean notifyAuthFailure,
            AccountManagerCallback<Bundle> callback, Handler handler) {
        PrivacySettings pSet = mPrivSetManager.getSettings(mContext.getPackageName(), Binder.getCallingUid());
        String output_label;
        AccountManagerFuture<Bundle> output;
        
        if (pSet != null && pSet.getAccountsAuthTokensSetting() != PrivacySettings.REAL) {
            output_label = "[empty]";
            output = new PrivacyAccountManagerFuture<Bundle>(new Bundle());
        } else {
            output_label = "[real value]";
            output = super.getAuthToken(account, authTokenType, notifyAuthFailure, callback, handler);
        }
        
        Log.d(TAG, "getAuthToken - " + mContext.getPackageName() + " (" + Binder.getCallingUid() + ") output: " + output_label);           
        return output;
    }

    @Override
    public AccountManagerFuture<Bundle> getAuthToken(Account account, String authTokenType, Bundle options,
            Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        PrivacySettings pSet = mPrivSetManager.getSettings(mContext.getPackageName(), Binder.getCallingUid());
        String output_label;
        AccountManagerFuture<Bundle> output;
        
        if (pSet != null && pSet.getAccountsAuthTokensSetting() != PrivacySettings.REAL) {
            output_label = "[empty]";
            output = new PrivacyAccountManagerFuture<Bundle>(new Bundle());
        } else {
            output_label = "[real value]";
            output = super.getAuthToken(account, authTokenType, options, activity, callback, handler);
        }
        
        Log.d(TAG, "getAuthToken - " + mContext.getPackageName() + " (" + Binder.getCallingUid() + ") output: " + output_label);           
        return output;
    }

    /**
     * MANAGE_ACCOUNTS
     */
    
    @Override
    public AccountManagerFuture<Bundle> getAuthTokenByFeatures(String accountType, String authTokenType,
            String[] features, Activity activity, Bundle addAccountOptions, Bundle getAuthTokenOptions,
            AccountManagerCallback<Bundle> callback, Handler handler) {
        PrivacySettings pSet = mPrivSetManager.getSettings(mContext.getPackageName(), Binder.getCallingUid());
        String output_label;
        AccountManagerFuture<Bundle> output;
        
        if (pSet != null && pSet.getAccountsAuthTokensSetting() != PrivacySettings.REAL) {
            output_label = "[empty]";
            output = new PrivacyAccountManagerFuture<Bundle>(new Bundle());
        } else {
            output_label = "[real value]";
            output = super.getAuthTokenByFeatures(accountType, authTokenType, features, activity, addAccountOptions,
                    getAuthTokenOptions, callback, handler);
        }
        
        Log.d(TAG, "getAuthTokenByFeatures - " + mContext.getPackageName() + " (" + Binder.getCallingUid() + ") output: " + output_label);           
        return output;
    }
    
    /**
     * Helper class. Used for returning custom values to AccountManager callers.
     */
    private class PrivacyAccountManagerFuture<V> implements AccountManagerFuture<V> {
        
        private V result;
        
        public PrivacyAccountManagerFuture(V result) {
            this.result = result;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public V getResult() throws OperationCanceledException, IOException, AuthenticatorException {
            return result;
        }

        @Override
        public V getResult(long timeout, TimeUnit unit) throws OperationCanceledException, IOException,
                AuthenticatorException {
            return result;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }
        
    }
}
