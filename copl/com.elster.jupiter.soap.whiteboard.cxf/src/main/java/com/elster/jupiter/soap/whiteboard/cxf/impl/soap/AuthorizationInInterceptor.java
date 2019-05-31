/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl.soap;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import org.apache.cxf.common.util.Base64Utility;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.HttpURLConnection;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Authentication interceptor for Apache CXF. Will verify credentials and assert the user has the role as configured on the endpoint.
 * Credentials are stored in the http session.
 * <p>
 * Created by bvn on 6/21/16.
 */
public class AuthorizationInInterceptor extends AbstractPhaseInterceptor<Message> {

    static final String USERPRINCIPAL = "com.elster.jupiter.userprincipal";

    private final UserService userService;
    private InboundEndPointConfiguration endPointConfiguration;
    private final TransactionService transactionService;
    private final ThreadPrincipalService threadPrincipalService;

    @Inject
    public AuthorizationInInterceptor(UserService userService, TransactionService transactionService, ThreadPrincipalService threadPrincipalService) {
        super(Phase.PRE_INVOKE);
        this.userService = userService;
        this.transactionService = transactionService;
        this.threadPrincipalService = threadPrincipalService;
    }

    public void handleMessage(Message message) throws Fault {
        HttpServletRequest request = (HttpServletRequest) message.get("HTTP.REQUEST");
        HttpSession httpSession = request.getSession();
        boolean newSession = false;

        String userName = null;
        String password = null;
        AuthorizationPolicy policy = message.get(AuthorizationPolicy.class);
        if (policy != null) {
            userName = policy.getUserName();
            password = policy.getPassword();
            newSession = true;
        } else {
            fail("Authentication required", "Authentication required", HttpURLConnection.HTTP_UNAUTHORIZED);
        }
        try {
            this.userService.findUser(userName).ifPresent(threadPrincipalService::set);
            Optional<User> user = userService.authenticateBase64(Base64Utility.encode((userName + ":" + password).getBytes()), request
                    .getRemoteAddr());
            if (!user.isPresent()) {
                fail("Not authorized", "User " + userName + " denied access: invalid credentials", HttpURLConnection.HTTP_FORBIDDEN);
            }
            if (endPointConfiguration.getGroup().isPresent()) {
                if (!user.get().isMemberOf(endPointConfiguration.getGroup().get())) {
                    fail("Not authorized", "User " + userName + " denied access: not in role", HttpURLConnection.HTTP_FORBIDDEN);
                }
            }
            request.setAttribute(USERPRINCIPAL, user.get());
        } catch (Fault e) {
            throw e;
        } catch (Exception e) {
            fail("Not authorized", "Exception while logging in " + userName + ":", e, HttpURLConnection.HTTP_FORBIDDEN);
        }
    }

    private void fail(String message, String detailedMessage, int statusCode) {
        endPointConfiguration.log(LogLevel.WARNING, detailedMessage);
        // TODO: create & fail occurrence + issue
        doFail(message, statusCode);
    }

    private void fail(String message, String detailedMessage, Exception e, int statusCode) {
        endPointConfiguration.log(detailedMessage, e);
        // TODO: create & fail occurrence + issue
        doFail(message, statusCode);
    }


    private void doFail(String message, int statusCode) {
        Fault fault = new Fault(message, Logger.getGlobal());
        fault.setStatusCode(statusCode);
        throw fault;
    }

    /**
     * Initialization of this particular interceptor
     *
     * @param endPointConfiguration The endpoint from which this interceptor will get the required configuration parameters
     */
    Interceptor<? extends Message> init(InboundEndPointConfiguration endPointConfiguration) {
        this.endPointConfiguration = endPointConfiguration;
        return this;
    }
}
