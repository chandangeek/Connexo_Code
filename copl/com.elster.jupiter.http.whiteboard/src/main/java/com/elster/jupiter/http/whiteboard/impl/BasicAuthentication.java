/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.http.whiteboard.CSRFFilterService;
import com.elster.jupiter.http.whiteboard.HttpAuthenticationService;
import com.elster.jupiter.http.whiteboard.SamlRequestService;
import com.elster.jupiter.http.whiteboard.TokenService;
import com.elster.jupiter.http.whiteboard.TokenValidation;
import com.elster.jupiter.http.whiteboard.UserJWT;
import com.elster.jupiter.http.whiteboard.impl.saml.SAMLUtilities;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.blacklist.BlackListTokenService;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.saml2.ecp.RelayState;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpContext;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.MessageInterpolator;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.NoSuchObjectException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static com.elster.jupiter.orm.Version.version;
import static com.elster.jupiter.util.Checks.is;

@Component(name = "com.elster.jupiter.http.whiteboard.HttpAutenticationService",
        property = {
                "name=" + BasicAuthentication.COMPONENT_NAME,
                "osgi.command.scope=jupiter",
                "osgi.command.function=createNewTokenKey",
                "osgi.command.function=updateExistingTokenKey"
        },
        immediate = true,
        service = {HttpAuthenticationService.class})
public final class BasicAuthentication implements HttpAuthenticationService {

    public static final String COMPONENT_NAME = "HTW";
    private static final String ACCOUNT_LOCKED = "AccountLocked";
    private static final String TIMEOUT = "com.elster.jupiter.timeout";
    private static final String TOKEN_REFRESH_MAX_COUNT = "com.elster.jupiter.token.refresh.maxcount";
    private static final String TOKEN_EXPIRATION_TIME = "com.elster.jupiter.token.expirationtime";
    private static final String LOGIN_URI = "/apps/login/index.html";
    // Resources used by the login page so access is required before authenticating
    private static final String[] RESOURCES_NOT_SECURED = {
            // Anything below will only be used in development.
            "/apps/sky/",
            "/apps/uni/",
            "/apps/ext/",
            "/api/apps/security/acs",
            "/api/apps/saml/v2/logout"
    };

    // No caching for index.html files, so that authentication will be verified first;
    // Note that resources used in these files are still cached
    private static final String[] RESOURCES_NOT_CACHED = {
            "index.html",
            "index-dev.html"
    };

    // Rest resources return UNAUTHORIZED rather than redirecting to the login page
    private static final String[] RESOURCES_UNAUTHORIZED = {
            "/api/",
            "/public/api/"
    };

    private static final String SSO_ENABLED_PROPERTY = "sso.enabled";
    private static final String SSO_IDP_ENDPOINT_PROPERTY = "sso.idp.endpoint";
    private static final String SSO_SP_ISSUER_ID = "sso.sp.issuer.id";
    private static final String SSO_X509_CERTIFICATE_PROPERTY = "sso.x509.certificate";
    private static final String SSO_ACS_ENDPOINT_PROPERTY = "sso.acs.endpoint";
    private static final String SSO_ADMIN_USER_PROPERTY = "sso.admin.user";
    public static final String LOGIN_URL = "/apps/login/";

    private final String TOKEN_COOKIE_NAME = "X-CONNEXO-TOKEN";
    private final String USER_SESSIONID = "X-SESSIONID";


    private volatile UserService userService;
    private volatile DataVaultService dataVaultService;
    private volatile SecurityTokenImpl securityToken;
    private volatile DataModel dataModel;
    private volatile TransactionService transactionService;
    private volatile UpgradeService upgradeService;
    private volatile BpmService bpmService;
    private volatile EventService eventService;
    private volatile MessageService messageService;
    private volatile SamlRequestService samlRequestService;
    private volatile TokenService<UserJWT> tokenService;
    private volatile BlackListTokenService blackListTokenService;
    private volatile Thesaurus thesaurus;
    private volatile CSRFFilterService csrfFilterService;

    private int timeoutFrameToRefreshToken;
    private int tokenRefreshMaxCount;
    private int tokenExpTime;
    private String installDir;
    private Optional<String> host;
    private Optional<Integer> port;
    private Optional<String> scheme;
    private boolean ssoEnabled;
    private Optional<String> idpEndpoint;
    private Optional<String> issuerId;
    private Optional<String> acsEndpoint;
    private Optional<String> x509Certificate;
    private Optional<String> ssoAdminUser;

    private static final SAMLUtilities samlUtilities = SAMLUtilities.getInstance();

    @Inject
    BasicAuthentication(UserService userService, OrmService ormService, DataVaultService dataVaultService, UpgradeService upgradeService,
                        BpmService bpmService, BundleContext context, BlackListTokenService blackListTokenService, TokenService tokenService, CSRFFilterService csrfFilterService) throws
            InvalidKeySpecException,
            NoSuchAlgorithmException {
        setUserService(userService);
        setOrmService(ormService);
        setDataVaultService(dataVaultService);
        setUpgradeService(upgradeService);
        setBpmService(bpmService);
        setBlackListdTokenService(blackListTokenService);
        setTokenService(tokenService);
        setCSRFFilterService(csrfFilterService);
        activate(context);
    }

    public BasicAuthentication() {

    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(WhiteBoardImpl.COMPONENTNAME, "HTTP Whiteboard");
        TableSpecs.HTW_KEYSTORE.addTo(dataModel);
    }

    @Reference
    public void setDataVaultService(DataVaultService dataVaultService) {
        this.dataVaultService = dataVaultService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setBpmService(BpmService bpmService) {
        this.bpmService = bpmService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setSamlRequestService(SamlRequestService samlRequestService) {
        this.samlRequestService = samlRequestService;
    }

    @Reference
    public void setCSRFFilterService(CSRFFilterService csrfFilterService) {
        this.csrfFilterService = csrfFilterService;
    }

    @Reference
    public void setBlackListdTokenService(BlackListTokenService blackListdTokenService) {
        this.blackListTokenService = blackListdTokenService;
    }

    @Reference
    public void setTokenService(TokenService<UserJWT> tokenService) {
        this.tokenService = tokenService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.SERVICE);
    }

    @Activate
    public void activate(BundleContext context) throws InvalidKeySpecException, NoSuchAlgorithmException {

        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(UserService.class).toInstance(userService);
                bind(DataVaultService.class).toInstance(dataVaultService);
                bind(DataModel.class).toInstance(dataModel);
                bind(EventService.class).toInstance(eventService);
                bind(MessageService.class).toInstance(messageService);
                bind(BlackListTokenService.class).toInstance(blackListTokenService);
                bind(CSRFFilterService.class).toInstance(csrfFilterService);
                bind(BasicAuthentication.class).toInstance(BasicAuthentication.this);
                bind(TokenService.class).toInstance(tokenService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
            }
        });
        timeoutFrameToRefreshToken = getIntParameter(TIMEOUT, context, 300);
        tokenRefreshMaxCount = getIntParameter(TOKEN_REFRESH_MAX_COUNT, context, 100);
        tokenExpTime = getIntParameter(TOKEN_EXPIRATION_TIME, context, 300);
        installDir = context.getProperty("install.dir");
        ssoAdminUser = getOptionalStringProperty(SSO_ADMIN_USER_PROPERTY, context);
        ssoEnabled = Boolean.parseBoolean(context.getProperty(SSO_ENABLED_PROPERTY));
        idpEndpoint = getOptionalStringProperty(SSO_IDP_ENDPOINT_PROPERTY, context);
        issuerId = getOptionalStringProperty(SSO_SP_ISSUER_ID, context);
        acsEndpoint = getOptionalStringProperty(SSO_ACS_ENDPOINT_PROPERTY, context);
        x509Certificate = getOptionalStringProperty(SSO_X509_CERTIFICATE_PROPERTY, context);
        upgradeService.register(
                InstallIdentifier.identifier("Pulse", "HTP"),
                dataModel,
                Installer.class,
                ImmutableMap.of(
                        version(10, 4), UpgraderV10_4_1.class,
                        version(10, 4, 1), UpgraderV10_4_2.class,
                        version(10, 8), UpgraderV10_8.class
                )
        );

        initializeTokenService();

        host = getOptionalStringProperty("com.elster.jupiter.url.rewrite.host", context);
        Optional<String> portString = getOptionalStringProperty("com.elster.jupiter.url.rewrite.port", context);
        port = portString.map(port -> {
            try {
                return Optional.of(Integer.valueOf(port));
            } catch (NumberFormatException e) {
                return Optional.<Integer>empty();
            }
        }).orElse(Optional.<Integer>empty());
        scheme = getOptionalStringProperty("com.elster.jupiter.url.rewrite.scheme", context);
    }

    public void createNewTokenKey(String... args) {
        System.out.println("Usage : createNewTokenKey <fileName>");
    }

    public void updateExistingTokenKey() {
        try (TransactionContext transactionContext = transactionService.getContext()) {
            updateExistingTokenKeyWithoutTransaction();
            transactionContext.commit();
        }
    }

    public void updateExistingTokenKeyWithoutTransaction() {
        try {
            final Optional<KeyStoreImpl> keyStore = getKeyPair();

            if (keyStore.isPresent()) {
                final KeyStoreImpl store = keyStore.get();
                store.delete();
                dataModel.getInstance(KeyStoreImpl.class).init(dataVaultService);
                initializeTokenService();
            }

            Optional<User> processExecutor = userService.findUser("process executor");

            if (processExecutor.isPresent()) {
                final String connexoRootPath = System.getProperty("connexo.home");

                if (!Objects.isNull(connexoRootPath)) {
                    final String configPropertiesFilePath = connexoRootPath + "/conf/config.properties";

                    final List<String> allLines = Files.readAllLines(Paths.get(configPropertiesFilePath));
                    for (int i = 0; i < allLines.size(); i++) {
                        if (!allLines.get(i).contains("#")) {
                            if (allLines.get(i).contains("com.elster.jupiter.token=")) {
                                allLines.set(i, "com.elster.jupiter.token=" + tokenService.createPermamentSignedJWT(processExecutor.get()).serialize());
                            }
                            if (allLines.get(i).contains("com.elster.jupiter.sso.public.key")) {
                                allLines.set(i, "com.elster.jupiter.sso.public.key=" + new String(dataVaultService.decrypt(getKeyPair().get().getPublicKey())));
                            }
                        }
                    }

                    Files.write(Paths.get(configPropertiesFilePath), allLines, StandardOpenOption.WRITE);
                }
            } else {
                throw new NoSuchObjectException("\"Process Executor\" User is not present.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createNewTokenKey(String fileName) {
        transactionService.builder().run(() -> {
            try {
                tryCreateNewTokenKey(fileName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void tryCreateNewTokenKey(String fileName) throws NoSuchAlgorithmException {
        final Optional<KeyStoreImpl> keyStore = getKeyPair();

        if (keyStore.isPresent()) {
            final KeyStoreImpl store = keyStore.get();

            store.delete();

            dataModel.getInstance(KeyStoreImpl.class).init(dataVaultService);

            initializeTokenService();

            saveKeyToFile(FileSystems.getDefault().getPath(fileName));
        }
    }

    protected void saveKeyToFile(Path filePath) {
        try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(filePath))) {
            Optional<User> foundUser = userService.findUser("process executor");
            if (foundUser.isPresent()) {
                writer.write("\ncom.elster.jupiter.token=");
                writer.write(tokenService.createPermamentSignedJWT(foundUser.get()).serialize());
            }
            writer.write("\ncom.elster.jupiter.sso.public.key=");
            writer.write(new String(dataVaultService.decrypt(getKeyPair().get().getPublicKey())));
            writer.flush();
        } catch (IOException | JOSEException e) {
            e.printStackTrace();
        }
    }

    protected void initializeTokenService() {
        Optional<KeyStoreImpl> keyStore = getKeyPair();
        keyStore.ifPresent(store -> {
            tokenService.initialize(
                    dataVaultService.decrypt(store.getPublicKey()),
                    dataVaultService.decrypt(store.getPrivateKey()),
                    tokenExpTime,
                    tokenRefreshMaxCount,
                    timeoutFrameToRefreshToken
            );

            // TODO: move event service logic and event logging to TokenService impl
            try {
                securityToken = new SecurityTokenImpl(dataVaultService.decrypt(keyStore.get().getPublicKey()), dataVaultService.decrypt(keyStore.get().getPrivateKey()), tokenExpTime, tokenRefreshMaxCount, timeoutFrameToRefreshToken);
                securityToken.setEventService(eventService);
                securityToken.preventEventGeneration(false);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private int getIntParameter(String propertyName, BundleContext context, int defaultValue) {
        if (context != null) {
            String configParam = context.getProperty(propertyName);
            if (!is(configParam).emptyOrOnlyWhiteSpace()) {
                try {
                    return Integer.parseInt(configParam);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Cannot parse '" + configParam + "'", e);
                }
            }
        }
        return defaultValue;
    }

    private Optional<String> getOptionalStringProperty(String propertyName, BundleContext context) {
        if (context != null) {
            String property = context.getProperty(propertyName);
            if (!is(property).emptyOrOnlyWhiteSpace()) {
                return Optional.of(property.trim());
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    Optional<KeyStoreImpl> getKeyPair() {
        List<KeyStoreImpl> keys = new ArrayList<>(dataModel.mapper(KeyStoreImpl.class).find());
        if (!keys.isEmpty()) {
            return Optional.of(keys.get(0));
        }
        return Optional.empty();
    }

    String getInstallDir() {
        return installDir;
    }

    public Optional<String> getSsoAdminUser() {
        return ssoAdminUser;
    }

    public boolean isSsoEnabled() {
        return ssoEnabled;
    }

    @Override
    public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Set no caching for specific resources regardless of the authentication type
        String samlResponse = request.getParameter("SAMLResponse");
        if (isCachedResource(request.getRequestURL().toString())) {
            response.setHeader("Cache-Control", "max-age=86400");
        } else {
            // Proper way to ensure the page is not cached accross all browsers
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
            response.setHeader("Pragma", "no-cache"); // HTTP 1.0
            response.setDateHeader("Expires", 0); // Proxies
        }

        String authentication = request.getHeader("Authorization");
        if (authentication != null && authentication.startsWith("Basic ")) {
            return doBasicAuthentication(request, response, authentication.split(" ")[1]);
        } else if (authentication != null && authentication.startsWith("Bearer ")) {
            return doBearerAuthorization(request, response, authentication);
        } else {
            Optional<Cookie> tokenCookie = getTokenCookie(request);
            if (tokenCookie.isPresent()) {
                return doCookieAuthorization(tokenCookie.get(), request, response);
            } else if (unsecureAllowed(request.getRequestURI())) {
                response.setStatus(HttpServletResponse.SC_ACCEPTED);
                return true;
            } else if (ssoEnabled) {
                if (isNotAllowedForSsoAuthentication(request)) return ssoDeny(request, response);
                ssoAuthentication(request, response);
                return true;
            } else if (!shouldUnauthorize(request.getRequestURI())) {
                response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
                response.setHeader("Location", getLoginUrl(request));
                response.getOutputStream().flush();
                return true;
            } else {
                // Rest resources send back UNAUTHORIZED HTTP response code
                // rather than redirecting to login
                return deny(request, response);
            }
        }
    }

    private boolean isNotAllowedForSsoAuthentication(HttpServletRequest request) {
        return request.getRequestURI().startsWith(LOGIN_URL) &&
                (StringUtils.isEmpty(request.getParameter("page")) || request.getParameterMap().containsKey("logout"));
    }

    private void ssoAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Optional<String> ssoAuthenticationRequestOptional = samlRequestService.createSSOAuthenticationRequest(request, response, acsEndpoint.get(), issuerId.get());
        if (ssoAuthenticationRequestOptional.isPresent()) {
            String redirectUrl;
            if (StringUtils.isEmpty(request.getParameter("page"))) {
                redirectUrl = getSamlRequestUrl(ssoAuthenticationRequestOptional.get(), request.getRequestURL().toString());
            } else {
                redirectUrl = getSamlRequestUrl(ssoAuthenticationRequestOptional.get(), request.getParameter("page"));
            }
            response.sendRedirect(redirectUrl);
        }
    }

    private String getLoginUrl(HttpServletRequest request) {
        String loginUrl = (scheme.isPresent() ? scheme.get() : request.getScheme()) + "://" + (host.isPresent() ? host.get() : request.getServerName());
        int portNumber = (port.isPresent() ? port.get() : request.getServerPort());
        if (portNumber != 80 && portNumber != 443) {
            loginUrl = loginUrl + ":" + portNumber;
        }

        return loginUrl + LOGIN_URI + "?" + "page=" + loginUrl + request.getRequestURI();
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // Invalidate token & server side session
        Optional<Cookie> tokenCookie = getTokenCookie(request);
        Object logoutParameter = request.getUserPrincipal();
        if (tokenCookie.isPresent()) {
            removeCookie(response, tokenCookie.get().getName());
            invalidateSessionCookie(request, response);
            invalidateSession(request);
        }
        if (logoutParameter instanceof User) {
            //the eventService is sent ONLY if the Object is an instance of User class,
            postWhiteboardEvent(WhiteboardEvent.LOGOUT.topic(), new LocalEventUserSource((User) logoutParameter));
            tokenCookie.ifPresent(cookie -> blackListToken(((User) logoutParameter).getId(), cookie.getValue()));
        }
    }

    private void blackListToken(long userId, String cookieValue) {
        try (TransactionContext transactionContext = transactionService.getContext()) {
            BlackListTokenService.BlackListTokenBuilder blackListTokenBuilder = blackListTokenService.getBlackListTokenService();
            blackListTokenBuilder.setUerId(userId);
            blackListTokenBuilder.setToken(cookieValue);
            blackListTokenBuilder.save();
            transactionContext.commit();
        }
    }

    @Override
    public String createToken(User user, String ipAddress) throws JOSEException {
        return tokenService.createUserJWT(user, createCustomClaimsForUser(user, 0)).getToken();
    }

    @Override
    public String getSsoX509Certificate() {
        return x509Certificate.get();
    }

    @Override
    public Cookie createTokenCookie(String cookieValue, String cookiePath) {
        Cookie cookie = new Cookie(TOKEN_COOKIE_NAME, cookieValue);
        cookie.setPath(cookiePath);
        cookie.setMaxAge(tokenExpTime + timeoutFrameToRefreshToken);
        cookie.setHttpOnly(true);
        return cookie;
    }

    public Cookie createSessionCookie(String sessionId, String cookiePath) {
        Cookie sessionCookie = new Cookie(USER_SESSIONID, sessionId);
        sessionCookie.setPath(cookiePath);
        sessionCookie.setMaxAge(tokenExpTime + timeoutFrameToRefreshToken);
        sessionCookie.setHttpOnly(true);
        csrfFilterService.createCSRFToken(sessionId);
        return sessionCookie;
    }

    private boolean doCookieAuthorization(Cookie tokenCookie, HttpServletRequest request, HttpServletResponse response) {
        TokenValidation validation = null;
        try {
            try (TransactionContext transactionContext = transactionService.getContext()) {
                validation = tokenService.validateSignedJWT(SignedJWT.parse(tokenCookie.getValue()));
                transactionContext.commit();
            }
        } catch (JOSEException | ParseException e) {
            e.printStackTrace();
        }
        return handleTokenValidation(Objects.requireNonNull(validation), tokenCookie.getValue(), request, response);
    }

    private boolean handleTokenValidation(TokenValidation validation, String originalToken, HttpServletRequest request, HttpServletResponse response) {
        if (validation.isValid() && isAuthenticated(validation.getUser())) {
            if (!originalToken.equals(validation.getToken())) {
                response.addCookie(createTokenCookie(validation.getToken(), "/"));
            }
            return allow(request, response, validation.getUser().get(), validation.getToken());
        } else {
            if(Objects.nonNull(validation) && validation.getUser().isPresent()) {
                try (TransactionContext transactionContext = transactionService.getContext()) {
                    userService.resetUserRoleChangeStatus(validation.getUser().get().getId());
                    transactionContext.commit();
                }
                userService.removeLoggedUser(validation.getUser().get());
            }
            return deny(request, response);
        }
    }

    private boolean doBearerAuthorization(HttpServletRequest request, HttpServletResponse response, String authentication) {
        String token = authentication.substring(authentication.lastIndexOf(" ") + 1);
        Optional<Cookie> xsrf = getTokenCookie(request);
        if (xsrf.isPresent()) {
            token = xsrf.get().getValue();
            // TODO: Move implementation of token comparison to TokenService
            if (!securityToken.compareTokens(token, authentication.substring(authentication.lastIndexOf(" ") + 1), request
                    .getRemoteAddr())) {
                return deny(request, response);
            }
        }

        // Since the cookie value can be updated without updating the authorization header, it should be used here instead of the header
        // The check before ensures the header is also valid syntactically, but it may be expires if only the cookie was updated (Facts, Flow)
        TokenValidation tokenValidation = null;
        try {
            try (TransactionContext transactionContext = transactionService.getContext()) {
                tokenValidation = tokenService.validateSignedJWT(SignedJWT.parse(token));
                transactionContext.commit();
            }
        } catch (JOSEException | ParseException e) {
            e.printStackTrace();
        }
        return handleTokenValidation(Objects.requireNonNull(tokenValidation), token, request, response);
    }

    private boolean doBasicAuthentication(HttpServletRequest request, HttpServletResponse response, String authentication) {
        Optional<User> user = userService.authenticateBase64(authentication, request.getRemoteAddr());
        if (isUserLocked(user)) {
            return denyAccountLocked(request, response);
        } else if (isAuthenticated(user)) {
            User returnedUserByAuthentication = user.get();
            //required because user returned by auth has not yet lastSuccessfulLogin set.... This is a vamp. the login mechanism should be changed.
            User usr = userService.findUser(returnedUserByAuthentication.getName(), returnedUserByAuthentication.getDomain()).orElse(returnedUserByAuthentication);
            UserJWT userJWT = null;
            try {
                try (TransactionContext transactionContext = transactionService.getContext()) {
                    userJWT = tokenService.createUserJWT(usr, createCustomClaimsForUser(usr, 0));
                    transactionContext.commit();
                }
            } catch (JOSEException e) {
                e.printStackTrace();
            }
            String token = Objects.requireNonNull(userJWT).getToken();
            response.addCookie(createTokenCookie(token, "/"));
            response.addCookie(createSessionCookie(Base64.getUrlEncoder().encodeToString(UUID.randomUUID().toString().getBytes()), "/"));
            postWhiteboardEvent(WhiteboardEvent.LOGIN.topic(), new LocalEventUserSource(usr));
            return allow(request, response, usr, token);
        } else {
            LocalEventUserSource localEventUserSource = createLocalEventUserSource(authentication);
            postWhiteboardEvent(WhiteboardEvent.LOGIN_FAILED.topic(), localEventUserSource);
            return deny(request, response);
        }
    }

    private LocalEventUserSource createLocalEventUserSource(String authentication) {
        BasicAuthenticationCredentials credentials;
        try {
            credentials = new BasicAuthenticationCredentials(authentication);
        } catch (IllegalArgumentException e) {
            return new LocalEventUserSource("");
        }
        return new LocalEventUserSource(credentials.getUserName());
    }

    private boolean isAuthenticated(Optional<User> user) {
        return user.isPresent() && !user.get().getPrivileges().isEmpty();
    }

    private boolean isUserLocked(Optional<User> user) {
        return user.isPresent() && user.get().isUserLocked(userService.getLockingAccountSettings());
    }

    private boolean allow(HttpServletRequest request, HttpServletResponse response, User user, String token) {
        request.setAttribute(HttpContext.AUTHENTICATION_TYPE, HttpServletRequest.BASIC_AUTH);
        request.setAttribute(USERPRINCIPAL, user);
        request.setAttribute(HttpContext.REMOTE_USER, user.getName());
        userService.addLoggedInUser(user);
        response.setHeader("X-AUTH-TOKEN", token);
        response.setHeader("Authorization", "Bearer " + token);
        return true;
    }

    private boolean denyAccountLocked(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        try {
            response.getWriter().write(ACCOUNT_LOCKED);
            response.getWriter().flush();
            response.getWriter().close();
        } catch (IOException exception) {}
        Optional<Cookie> tokenCookie = getTokenCookie(request);
        if (tokenCookie.isPresent()) {
            removeCookie(response, tokenCookie.get().getName());
        }
        invalidateSession(request);

        return false;
    }

    private boolean deny(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        Optional<Cookie> tokenCookie = getTokenCookie(request);
        if (tokenCookie.isPresent()) {
            removeCookie(response, tokenCookie.get().getName());
            invalidateSessionCookie(request, response);
        }
        invalidateSession(request);
        return false;
    }

    private void invalidateSessionCookie(HttpServletRequest request, HttpServletResponse response) {
        Optional<Cookie> sessionCookie = getSessionCookie(request);
        if (sessionCookie.isPresent()) {
            csrfFilterService.removeUserSession(sessionCookie.get().getValue());
            removeCookie(response, sessionCookie.get().getName());
        }
    }

    private boolean ssoDeny(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        Optional<Cookie> tokenCookie = getTokenCookie(request);
        if (tokenCookie.isPresent()) {
            removeCookie(response, tokenCookie.get().getName());
            invalidateSessionCookie(request, response);
        }
        invalidateSession(request);
        return false;
    }

    private void removeCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    private void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    private boolean unsecureAllowed(String uri) {
        if (!ssoEnabled && uri.startsWith(LOGIN_URL)) return true;
        return Stream.of(RESOURCES_NOT_SECURED)
                .filter(uri::startsWith)
                .findAny().isPresent();
    }

    private boolean isCachedResource(String uri) {
        return !Stream.of(RESOURCES_NOT_CACHED)
                .filter(uri::endsWith)
                .findAny().isPresent();
    }

    private boolean shouldUnauthorize(String uri) {
        return Stream.of(RESOURCES_UNAUTHORIZED)
                .filter(uri::startsWith)
                .findAny().isPresent();
    }

    private Optional<Cookie> getTokenCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> TOKEN_COOKIE_NAME.equals(cookie.getName()))
                    .findFirst();
        }
        return Optional.empty();
    }

    private Optional<Cookie> getSessionCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> USER_SESSIONID.equals(cookie.getName()))
                    .findFirst();
        }
        return Optional.empty();
    }

    private void postWhiteboardEvent(String topic, Object user) {
        eventService.postEvent(topic, user);
    }

    private String getSamlRequestUrl(String ssoAuthnRequest, String requestUrl) throws UnsupportedEncodingException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(issuerId.get());
        stringBuilder.append("?");
        stringBuilder.append("SAMLRequest=");
        stringBuilder.append(URLEncoder.encode(ssoAuthnRequest, "UTF-8").trim());
        stringBuilder.append("&");
        stringBuilder.append(RelayState.DEFAULT_ELEMENT_LOCAL_NAME);
        stringBuilder.append("=");
        stringBuilder.append(URLEncoder.encode(requestUrl, "UTF-8").trim());

        return stringBuilder.toString();
    }

    public Map<String, Object> createCustomClaimsForUser(final User user, long count) {
        List<Group> userGroups = user.getGroups();
        List<RoleClaimInfo> roles = new ArrayList<>();
        List<String> privileges = new ArrayList<>();
        for (Group group : userGroups) {

            group.getPrivileges().forEach((key, value) -> {
                if (key.equals("BPM") || key.equals("YFN"))
                    value.forEach(p -> privileges.add(p.getName()));
            });

            privileges.add("privilege.public.api.rest");
            privileges.add("privilege.pulse.public.api.rest");
            privileges.add("privilege.view.userAndRole");

            roles.add(new RoleClaimInfo(group.getId(), group.getName()));
        }

        final HashMap<String, Object> result = new HashMap<>();
        result.put("username", user.getName());
        result.put("roles", roles);
        result.put("privileges", privileges);
        result.put("cnt", count);
        return result;
    }

}
