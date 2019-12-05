package com.energyict.mdc.engine.offline.gui.security;

import javax.security.auth.callback.*;
import java.io.Serializable;


public class LoginCallbackHandler implements CallbackHandler, Serializable {

    private String userName;
    private String password;
    private String newPassword;

    public LoginCallbackHandler(String userName, String password, String newPassword) {
        this.userName = userName;
        this.password = password;
        this.newPassword = newPassword;
    }

    public LoginCallbackHandler(String userName, String password) {
        this(userName, password, null);
    }

    public LoginCallbackHandler() {
        this(null, null, null);
    }

    public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            handle(callbacks[i]);
        }
    }

    private void handle(Callback callback) throws UnsupportedCallbackException {
        if (callback instanceof NameCallback) {
            NameCallback nameCallback = (NameCallback) callback;
            nameCallback.setName(userName);
        } else if (callback instanceof PasswordCallback) {
            PasswordCallback passwordCallback = (PasswordCallback) callback;
            passwordCallback.setPassword(password.toCharArray());
        } else if (callback instanceof NewPasswordCallback) {
            NewPasswordCallback newPasswordCallback = (NewPasswordCallback) callback;
            newPasswordCallback.setNewPassword(newPassword);
        } else {
            throw new UnsupportedCallbackException(callback);
        }
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
