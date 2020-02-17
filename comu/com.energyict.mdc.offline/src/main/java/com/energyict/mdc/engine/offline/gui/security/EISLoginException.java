package com.energyict.mdc.engine.offline.gui.security;

import javax.security.auth.login.LoginException;

/**
 * Base class for all login exception in EIServer.
 *
 * @author alex
 */
public final class EISLoginException extends LoginException {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -1698960692854158105L;

    /**
     * The types of error conditions.
     */
    public enum Type {
        UNKNOWN_USERNAME("login.unknown.user"), ACCOUNT_EXPIRED("login.account.expired"), INVALID_PASSWORD("login.invalid.password"), USER_NOT_AUTHORIZED("login.user.not.authorized");

        /**
         * The key of the error message resource.
         */
        private final String resourceKey;

        /**
         * Creates a new instance of the type enum.
         *
         * @param resourceKey The resource key of the error message.
         */
        private Type(final String resourceKey) {
            this.resourceKey = resourceKey;
        }
    }

    /**
     * The error type.
     */
    private final Type errorType;

    /**
     * The user name.
     */
    private final String username;

    /**
     * Creates a new instance using the given front end context.
     *
     * @param type
     * @param username
     */
    public EISLoginException(final Type type, final String username) {
        this.errorType = type;
        this.username = username;
    }

    /**
     * @return the errorType
     */
    public final Type getErrorType() {
        return errorType;
    }

    /**
     * @return the username
     */
    public final String getUsername() {
        return username;
    }
}
