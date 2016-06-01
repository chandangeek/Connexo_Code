package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.http.whiteboard.HttpAuthenticationService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.ExceptionCatcher;

import com.google.inject.AbstractModule;
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
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.elster.jupiter.util.Checks.is;

@Component(name = "com.elster.jupiter.http.whiteboard.HttpAutenticationService",
        property = {"name=HTW", "osgi.command.scope=jupiter", "osgi.command.function=createNewTokenKey"},
        immediate = true, service = {HttpAuthenticationService.class, InstallService.class})
public class BasicAuthentication implements HttpAuthenticationService, InstallService {

    private final static String TIMEOUT = "com.elster.jupiter.timeout";
    private final static String TOKEN_REFRESH_MAX_COUNT = "com.elster.jupiter.token.refresh.maxcount";
    private final static String TOKEN_EXPIRATION_TIME = "com.elster.jupiter.token.expirationtime";
    private static final String LOGIN_URI = "/apps/login/index.html";
    // Resources used by the login page so access is required before authenticating
    private static final String[] RESOURCES_NOT_SECURED = {
            "/apps/login/",
            // Anything below will only be used in development.
            "/apps/sky/",
            "/apps/uni/",
            "/apps/ext/"
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

    private final String TOKEN_COOKIE_NAME = "X-CONNEXO-TOKEN";

    private volatile UserService userService;
    private volatile DataVaultService dataVaultService;
    private volatile SecurityTokenImpl securityToken;
    private volatile DataModel dataModel;
    private volatile TransactionService transactionService;
    private int timeout;
    private int tokenRefreshMaxCount;
    private int tokenExpTime;
    private String installDir;


    @Inject
    BasicAuthentication(UserService userService, OrmService ormService, DataVaultService dataVaultService) throws
            InvalidKeySpecException,
            NoSuchAlgorithmException {
        setUserService(userService);
        setOrmService(ormService);
        setDataVaultService(dataVaultService);
        activate(null);
        if (!dataModel.isInstalled()) {
            install();
        }
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
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Reference
    public void setDataVaultService(DataVaultService dataVaultService) {
        this.dataVaultService = dataVaultService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Activate
    public void activate(BundleContext context) throws InvalidKeySpecException, NoSuchAlgorithmException {

        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(UserService.class).toInstance(userService);
                bind(DataVaultService.class).toInstance(dataVaultService);
                bind(DataModel.class).toInstance(dataModel);
            }
        });
        timeout = getIntParameter(TIMEOUT, context, 300);
        tokenRefreshMaxCount = getIntParameter(TOKEN_REFRESH_MAX_COUNT, context, 100);
        tokenExpTime = getIntParameter(TOKEN_EXPIRATION_TIME, context, 300);
        installDir = context.getProperty("install.dir");
        if (dataModel.isInstalled()) {
            initSecurityTokenImpl();
        }
    }

    private void initSecurityTokenImpl() {
        Optional<KeyStoreImpl> keyStore = getKeyPair();
        if (keyStore.isPresent()) {

            try {
                securityToken = new SecurityTokenImpl(dataVaultService.decrypt(keyStore.get().getPublicKey()),
                        dataVaultService.decrypt(keyStore.get().getPrivateKey()),
                        tokenExpTime, tokenRefreshMaxCount, timeout);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new RuntimeException(e);
            }
        }
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

    @Override
    public void install() {
        ExceptionCatcher.executing(this::installDataModel, this::createKeys, this::initSecurityTokenImpl, this::dumpPublicKey)
                .andHandleExceptionsWith(Throwable::printStackTrace)
                .execute();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "EVT", "DVA");
    }

    private void installDataModel() {
        dataModel.install(true, true);
    }

    private void createKeys() {
        if (!getKeyPair().isPresent()) {
            try {
                dataModel.getInstance(KeyStoreImpl.class).init(dataVaultService);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void dumpPublicKey() {
        if (!is(installDir).emptyOrOnlyWhiteSpace()) {
            Path conf = FileSystems.getDefault().getPath(installDir).resolve("publicKey.txt");
            dumpToFile(conf);
        }
    }

    private void dumpToFile(Path conf) {
        try (OutputStreamWriter writer = new FileWriter(conf.toFile())) {
            writer.write(new String(dataVaultService.decrypt(getKeyPair().get().getPublicKey())));
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createNewTokenKey(String fileName) {
        try {
            transactionService.builder().run(() -> {
                Optional<KeyStoreImpl> keyPair = getKeyPair();
                if (keyPair.isPresent()) {
                    keyPair.get().delete();
                }
                createKeys();
                dumpToFile(FileSystems.getDefault().getPath(fileName));
                initSecurityTokenImpl();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Optional<KeyStoreImpl> getKeyPair() {
        List<KeyStoreImpl> keys = new ArrayList<>(dataModel.mapper(KeyStoreImpl.class).find());
        if (!keys.isEmpty()) {
            return Optional.of(keys.get(0));
        }
        return Optional.empty();
    }

    @Override
    public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Set no caching for specific resources regardless of the authentication type
        if (isCachedResource(request.getRequestURL().toString())) {
            response.setHeader("Cache-Control", "max-age=86400");
        } else {
            response.setHeader("Cache-Control", "no-cache");
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
            } else if (!shouldUnauthorize(request.getRequestURI())) {
                String server = request.getRequestURL().substring(0, request.getRequestURL().indexOf(request.getRequestURI()));
                response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
                response.sendRedirect(server + LOGIN_URI + "?" + "page=" + request.getRequestURL());
                return true;
            } else {
                // Rest resources send back UNAUTHORIZED HTTP response code
                // rather than redirecting to login
                return deny(request, response);
            }
        }
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // Invalidate token & server side session
        Optional<Cookie> tokenCookie = getTokenCookie(request);
        if (tokenCookie.isPresent()) {
            removeCookie(response, tokenCookie.get().getName());
            invalidateSession(request);
        }
    }


    private boolean doCookieAuthorization(Cookie tokenCookie, HttpServletRequest request, HttpServletResponse response) {
        SecurityTokenImpl.TokenValidation validation = securityToken.verifyToken(tokenCookie.getValue(), userService, request.getRemoteAddr());
        return handleTokenValidation(validation, tokenCookie.getValue(), request, response);
    }

    private boolean handleTokenValidation(SecurityTokenImpl.TokenValidation validation, String originalToken, HttpServletRequest request, HttpServletResponse response) {
        if (validation.isValid() && isAuthenticated(validation.getUser())) {
            if (!originalToken.equals(validation.getToken())) {
                response.addCookie(createTokenCookie(validation.getToken(), "/"));
            }
            return allow(request, response, validation.getUser().get(), validation.getToken());
        } else {
            return deny(request, response);
        }
    }

    private boolean doBearerAuthorization(HttpServletRequest request, HttpServletResponse response, String authentication) {
        String token = authentication.substring(authentication.lastIndexOf(" ") + 1);
        Optional<Cookie> xsrf = getTokenCookie(request);
        if (xsrf.isPresent()) {
            token = xsrf.get().getValue();
            if (!securityToken.compareTokens(token, authentication.substring(authentication.lastIndexOf(" ") + 1), request.getRemoteAddr())) {
                return deny(request, response);
            }
        }

        // Since the cookie value can be updated without updating the authorization header, it should be used here instead of the header
        // The check before ensures the header is also valid syntactically, but it may be expires if only the cookie was updated (Facts, Flow)
        SecurityTokenImpl.TokenValidation tokenValidation = securityToken.verifyToken(token, userService, request.getRemoteAddr());
        return handleTokenValidation(tokenValidation, token, request, response);
    }

    private boolean doBasicAuthentication(HttpServletRequest request, HttpServletResponse response, String authentication) {
        Optional<User> user = userService.authenticateBase64(authentication, request.getRemoteAddr());
        //securityToken.removeCookie(request, response);
        if (isAuthenticated(user)) {
            String token = securityToken.createToken(user.get(), 0, request.getRemoteAddr());
            response.addCookie(createTokenCookie(token, "/"));
            return allow(request, response, user.get(), token);
        } else {
            return deny(request, response);
        }
    }

    private boolean isAuthenticated(Optional<User> user) {
        return user.isPresent() && !user.get().getPrivileges().isEmpty();
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

    private boolean deny(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        Optional<Cookie> tokenCookie = getTokenCookie(request);
        if (tokenCookie.isPresent()) {
            removeCookie(response, tokenCookie.get().getName());
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
        return Stream.of(RESOURCES_NOT_SECURED)
                .filter(r -> uri.startsWith(r))
                .findAny().isPresent();
    }

    private boolean isCachedResource(String uri) {
        return !Stream.of(RESOURCES_NOT_CACHED)
                .filter(r -> uri.endsWith(r))
                .findAny().isPresent();
    }

    private boolean shouldUnauthorize(String uri) {
        return Stream.of(RESOURCES_UNAUTHORIZED)
                .filter(r -> uri.startsWith(r))
                .findAny().isPresent();
    }

    private Optional<Cookie> getTokenCookie(HttpServletRequest request) {
        return Arrays.stream(request.getCookies())
                .filter(cookie -> TOKEN_COOKIE_NAME.equals(cookie.getName()))
                .findFirst();
    }

    private Cookie createTokenCookie(String cookieValue, String cookiePath) {
        Cookie cookie = new Cookie(TOKEN_COOKIE_NAME, cookieValue);
        cookie.setPath(cookiePath);
        cookie.setMaxAge(securityToken.getCookieMaxAge());
        cookie.setHttpOnly(true);
        return cookie;
    }

}
