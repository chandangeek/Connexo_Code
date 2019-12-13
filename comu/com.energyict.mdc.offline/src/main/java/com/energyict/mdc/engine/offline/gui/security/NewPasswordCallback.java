package com.energyict.mdc.engine.offline.gui.security;

import javax.security.auth.callback.Callback;
import java.io.Serializable;

public class NewPasswordCallback implements Callback, Serializable {

    private String newPassword;

    public NewPasswordCallback() {

    }

    public NewPasswordCallback(String password) {
        this.newPassword = password;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
