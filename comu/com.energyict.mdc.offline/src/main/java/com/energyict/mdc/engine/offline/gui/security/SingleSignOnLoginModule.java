package com.energyict.mdc.engine.offline.gui.security;

import com.elster.jupiter.users.User;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.util.Map;

public class SingleSignOnLoginModule implements LoginModule {

    public static String WEB_CLIENT_USER = "Web client user ";
    public static String FULL_CLIENT_USER = "Desktop client usr ";

    private Subject subject;
    private String name;

    public SingleSignOnLoginModule() {
    }

    public boolean abort() throws LoginException {
        clearCache();
        return true;
    }

    public boolean commit() throws LoginException {
        if (name == null) {
            return false;
        } else {
            subject.getPrincipals().add(new NamedPrincipal(name));
            doCommit();
            return true;
        }
    }

    protected void doCommit() {
//        try {
//            User user = MeteringWarehouse.getCurrent().getUser();
//            user.updateLastLogin();
//            logSuccessfulLoginAttempt(user);
//            user.resetUnsuccessfulTries();
//        } catch (SQLException ex) {
//            throw new DatabaseException(ex);
//        } catch (BusinessException ex) {
//            throw new ApplicationException(ex);
//        } finally {
//            FormatProvider.instance.get().closeConnection();
//        }
    }

    private void logSuccessfulLoginAttempt(User user) {
//        MeteringWarehouse.getCurrent().journal(SystemLogEntry.EISERVER, Level.INFO, getLogMessageHeader() + user.getName() + " : logged on using version " + MeteringWarehouse.getCurrent().getVersion());
    }

    protected String getLogMessageHeader() {
//        if (MeteringWarehouse.getCurrent().getProvider().getExecutionMode() == MeteringWarehouseProvider.ExecutionMode.WEB) {
//            return WEB_CLIENT_USER;
//        } else {
            return FULL_CLIENT_USER;
//        }
    }

    public void initialize(Subject subject, CallbackHandler callbackHandler,
                           Map<String, ?> sharedState,
                           Map<String, ?> options) {

        this.subject = subject;
    }

    public boolean login() throws LoginException {
        name = System.getProperty("user.name");

//        User user = MeteringWarehouse.getCurrent().getUserFactory().find(name);
//        if (user == null) {
//            throw new FailedLoginException("User '" + name + "' not found!");
//        }
//        try {
//            MeteringWarehouse.getCurrent().privilegedLogon(name);
            return true;
//        } catch (SecurityException ex) {
//            throw new LoginException(ex.getMessage());
//        }
    }

    public boolean logout() throws LoginException {
        clearCache();
        return true;
    }

    private void clearCache() {
        this.subject = null;
        this.name = null;
    }

}