/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl.soap;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.EventType;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.WebService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.soap.whiteboard.cxf.impl.MessageUtils;
import com.elster.jupiter.soap.whiteboard.cxf.security.Privileges;
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
import java.net.HttpURLConnection;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Authentication interceptor for Apache CXF. Will verify credentials and assert the user has the role as configured on the endpoint.
 * Credentials are stored in the http session.
 * <p>
 * Created by bvn on 6/21/16.
 */
public class AuthorizationInInterceptor extends AbstractPhaseInterceptor<Message> {

    public static final String NOT_AUTHORIZED = "Not authorized";
    static final String USERPRINCIPAL = "com.elster.jupiter.userprincipal";

    private final UserService userService;
    private InboundEndPointConfiguration endPointConfiguration;
    private final WebServicesService webServicesService;
    private final WebServiceCallOccurrenceService webServiceCallOccurrenceService;
    private final ThreadPrincipalService threadPrincipalService;
    private final EventService eventService;

    @Inject
    public AuthorizationInInterceptor(UserService userService,
                                      WebServicesService webServicesService,
                                      WebServiceCallOccurrenceService webServiceCallOccurrenceService,
                                      ThreadPrincipalService threadPrincipalService,
                                      EventService eventService) {
        super(Phase.PRE_STREAM);
        this.userService = userService;
        this.webServicesService = webServicesService;
        this.webServiceCallOccurrenceService = webServiceCallOccurrenceService;
        this.threadPrincipalService = threadPrincipalService;
        this.eventService = eventService;
    }

    public void handleMessage(Message message) throws Fault {
        HttpServletRequest request = (HttpServletRequest) message.get("HTTP.REQUEST");
        request.getSession();

        String userName = null;
        String password = null;
        AuthorizationPolicy policy = message.get(AuthorizationPolicy.class);
        if (policy != null) {
            userName = policy.getUserName();
            password = policy.getPassword();
        } else {
            fail(message, "Authentication required",
                    "Authentication required", HttpURLConnection.HTTP_UNAUTHORIZED);
        }
        try {
            this.userService.findUser(userName).ifPresent(threadPrincipalService::set);
            Optional<User> user = userService.authenticateBase64(Base64Utility.encode((userName + ":" + password).getBytes()), request
                    .getRemoteAddr());
            if (!user.isPresent()) {
                fail(message, NOT_AUTHORIZED,
                        "User " + userName + " denied access: invalid credentials", HttpURLConnection.HTTP_FORBIDDEN);
            }
            if (endPointConfiguration.getGroup().isPresent()) {
                if (!user.get().isMemberOf(endPointConfiguration.getGroup().get())) {
                    fail(message, NOT_AUTHORIZED,
                            "User " + userName + " denied access: not in role", HttpURLConnection.HTTP_FORBIDDEN);
                }
            }
            String applicationName = webServicesService.getWebService(endPointConfiguration.getWebServiceName())
                    .map(WebService::getApplicationName)
                    .orElse(ApplicationSpecific.WebServiceApplicationName.UNDEFINED.getName());
            if (!hasInvokePrivileges(applicationName, user.get())) {
                // needs privilege Web services / Invoke
                fail(message, NOT_AUTHORIZED,
                        "User " + userName + " denied access: no privileges", HttpURLConnection.HTTP_FORBIDDEN);
            }
            request.setAttribute(USERPRINCIPAL, user.get());
        } catch (Fault e) {
            throw e;
        } catch (Exception e) {
            fail(message, NOT_AUTHORIZED,
                    "Exception while logging in " + userName + ": " + e.getLocalizedMessage(), e, HttpURLConnection.HTTP_FORBIDDEN);
        }
    }

    private void fail(Message request, String message, String detailedMessage, int statusCode) {
        MessageUtils.findOccurrenceId(request).ifPresent(id -> {
            WebServiceCallOccurrence occurrence = webServiceCallOccurrenceService.failOccurrence(id, detailedMessage);
            eventService.postEvent(EventType.INBOUND_AUTH_FAILURE.topic(), occurrence);
        });
        doFail(message, statusCode);
    }

    private boolean hasInvokePrivileges(String appName, User user) {
        return mapApplicationName(appName).stream()
                .anyMatch(app -> user.hasPrivilege(app, Privileges.Constants.INVOKE_WEB_SERVICES));
    }

    private static Set<String> mapApplicationName(String appName) {
        return ApplicationSpecific.WebServiceApplicationName.fromName(appName).getApplicationCodes();
    }

    private void fail(Message request, String message, String detailedMessage, Exception e, int statusCode) {
        MessageUtils.findOccurrenceId(request).ifPresent(id -> {
            WebServiceCallOccurrence occurrence = webServiceCallOccurrenceService.failOccurrence(id, new Exception(detailedMessage, e));
            eventService.postEvent(EventType.INBOUND_AUTH_FAILURE.topic(), occurrence);
        });
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
