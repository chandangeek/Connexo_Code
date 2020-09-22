/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.LdapUser;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Pair;

import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.ExtendedRequest;
import javax.naming.ldap.ExtendedResponse;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.elster.jupiter.util.Checks.is;

final class ActiveDirectoryImpl extends AbstractSecurableLdapDirectoryImpl {

    private static final String OBJECTCLASS_PERSON = "(objectclass=person)";
    private static final String OBJECT_CLASS_GROUP = "(objectClass=group)";
    private static final String USERACCOUNTCONTROL = "useraccountcontrol";
    private static final String S_AM_ACCOUNT_NAME = "sAMAccountName";
    private static final String[] ARRAY_MEMBER_OF = { "memberOf" };
    private static final Pattern REAL_GROUP_NAME_PATTERN = Pattern.compile("(.+?)=([^,]*).*");
    static final String TYPE_IDENTIFIER = "ACD";
    private static final Logger LOGGER = Logger.getLogger(ActiveDirectoryImpl.class.getSimpleName());

    @Inject
    ActiveDirectoryImpl(DataModel dataModel, UserService userService, BundleContext context) {
        super(dataModel, userService, context);
        setType(TYPE_IDENTIFIER);
    }

    static ActiveDirectoryImpl from(DataModel dataModel, String domain) {
        return dataModel.getInstance(ActiveDirectoryImpl.class).init(domain);
    }

    ActiveDirectoryImpl init(String domain) {
        setDomain(domain);
        return this;
    }

    @Override
    protected List<Group> doGetGroups(DirContext context, Object... args) throws NamingException {
        User user = (User) args[0];
        List<Group> groupList = new ArrayList<>();
        SearchControls controls = new SearchControls(getScopeForUserSearch(), 0, 0, ARRAY_MEMBER_OF, true, true);
        NamingEnumeration<SearchResult> answer = context.search(getNameForUserSearch(),
                "(&(objectClass=person)(sAMAccountName=" + user.getName() + "))", controls);
        if (answer.hasMoreElements()) {
            Attributes attrs = answer.nextElement().getAttributes();
            NamingEnumeration<? extends Attribute> e = attrs.getAll();
            while (e.hasMoreElements()) {
                Attribute attr = e.nextElement();
                for (int i = 0; i < attr.size(); ++i) {
                    userService.findGroup(getRealGroupName(attr.get(i).toString())).ifPresent(groupList::add);
                }
            }
        }
        return groupList;
    }

    @Override
    public Optional<User> authenticate(String name, String password) {
        List<String> urls = getUrls();
        LOGGER.info("AUTH: Autheticating ActiveDirectory user\n");
        if (getSecurity() == null || getSecurity().toUpperCase().contains(NONE)) {
            return authenticateSimple(name, password, urls);
        } else if (getSecurity().toUpperCase().contains(SSL)) {
            return authenticateSSL(name, password, urls, getSslSecurityProperties());
        } else if (getSecurity().toUpperCase().contains(TLS)) {
            return authenticateTLS(name, password, urls, getSslSecurityProperties());
        } else {
            return Optional.empty();
        }
    }

    private Optional<User> authenticateSimple(String name, String password, List<String> urls) {
        LOGGER.info("AUTH: No security applied\n");
        try {
            DirContext context = new InitialDirContext(createEnvironment(urls.get(0), getUserNameForAuthentication(name), password));
            context.close();
            return findUser(name);
        } catch (NumberFormatException | NamingException e) {
            LOGGER.severe("AUTH: Simple authetication failed\n");
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
            if (urls.size() > 1) {
                return authenticateSimple(name, password, urls.subList(1, urls.size()));
            } else {
                return Optional.empty();
            }
        }
    }

    private String getUserNameForAuthentication(String name) {
        String base = getGroupName() == null ? getBaseUser() : getGroupName();
        return name + "@" + getRealDomain(base);
    }

    private Optional<User> authenticateSSL(String name, String password, List<String> urls,
            SslSecurityProperties sslSecurityProperties) {
        LOGGER.info("AUTH: SSL applied\n");

        try {
            new InitialDirContext(createEnvironment(urls.get(0), getUserNameForAuthentication(name), password,
                    LOWERCASE_SSL, sslSecurityProperties));
            return findUser(name);
        } catch (NumberFormatException | NamingException e) {
            LOGGER.severe("AUTH: SSL authetication failed\n");
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
            if (urls.size() > 1) {
                return authenticateSSL(name, password, urls.subList(1, urls.size()), sslSecurityProperties);
            } else {
                return Optional.empty();
            }
        }
    }

    private Optional<User> authenticateTLS(String name, String password, List<String> urls,
            SslSecurityProperties sslSecurityProperties) {
        LOGGER.info("AUTH: TLS applied\n");

        StartTlsResponse tls = null;
        LdapContext ctx = null;
        try {
            ctx = new InitialLdapContext(
                    createEnvironment(urls.get(0), getUserNameForAuthentication(name), password), null);
            ExtendedRequest tlsRequest = new StartTlsRequest();
            ExtendedResponse tlsResponse = ctx.extendedOperation(tlsRequest);
            tls = (StartTlsResponse) tlsResponse;
            tls.negotiate(getSocketFactory(sslSecurityProperties, TLS));
            return findUser(name);
        } catch (NumberFormatException | IOException | NamingException e) {
            LOGGER.severe("AUTH: TLS authetication failed\n");
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
            if (urls.size() > 1) {
                return authenticateTLS(name, password, urls.subList(1, urls.size()), sslSecurityProperties);
            } else {
                return Optional.empty();
            }
        } finally {
            if(ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException ex) {
                    LOGGER.severe(ex.getMessage());
                    ex.printStackTrace();
                }
            }
            if (tls != null) {
                try {
                    tls.close();
                } catch (IOException e) {
                    throw new UnderlyingIOException(e);
                }
            }
        }
    }

    private String getRealDomain(String baseDN) {
        String normalizedDN = baseDN.toLowerCase();
        return normalizedDN.substring(normalizedDN.indexOf("dc=")).replace("dc=", "").replace(",", ".");
    }

    private String getRealGroupName(String rdn) {
        Matcher matcher = REAL_GROUP_NAME_PATTERN.matcher(rdn);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return rdn;
    }

    @Override
    public List<LdapUser> getLdapUsers() {
        return getSomethingFromLdap(this::getLdapUsersFromContext);
    }

    private List<LdapUser> getLdapUsersFromContext(DirContext ctx, Object... args) throws NamingException {
        List<LdapUser> ldapUsers = new ArrayList<>();
        SearchControls controls = new SearchControls();
        controls.setSearchScope(getScopeForUserSearch());
        String filter;
        if (getGroupName() == null) {
            filter = OBJECTCLASS_PERSON;
        } else {
            filter = "(&(objectclass=person)(memberOf=" + getGroupName() + "))";
        }
        NamingEnumeration<SearchResult> results = ctx.search(getNameForUserSearch(), filter, controls);
        while (results.hasMore()) {
            SearchResult searchResult = results.next();
            Attributes attributes = searchResult.getAttributes();
            if (attributes.get(S_AM_ACCOUNT_NAME) != null) {
                String userName = attributes.get(S_AM_ACCOUNT_NAME).get().toString();
                LdapUserImpl ldapUser = new LdapUserImpl();
                ldapUser.setUsername(userName);
                ldapUser.setDN(searchResult.getNameInNamespace());
                ldapUser.setStatus(true);
                if (attributes.get(USERACCOUNTCONTROL) != null) {
                    ldapUser.setStatus(isUserActive(attributes.get(USERACCOUNTCONTROL)));
                } else {
                    ldapUser.setStatus(true);
                }
                ldapUsers.add(ldapUser);
            }

        }
        return ldapUsers;
    }

    private int getScopeForUserSearch() {
        if (getGroupName() == null) {
            return SearchControls.ONELEVEL_SCOPE;
        }
        return SearchControls.SUBTREE_SCOPE;
    }

    private String getNameForUserSearch() {
        if (getGroupName() == null) {
            return getBaseUser();
        }
        int indexOfOu = getGroupName().toLowerCase().lastIndexOf(",ou=");
        if (indexOfOu >= 0) {
            return getGroupName().substring(indexOfOu + 1);
        }
        return getGroupName();
    }

    private boolean getUserStatusFromContext(DirContext context, Object... args) throws NamingException {
        String user = (String) args[0];
        SearchControls controls = new SearchControls();
        controls.setSearchScope(getScopeForUserSearch());
        NamingEnumeration<SearchResult> results = context.search(getNameForUserSearch(),
                "(sAMAccountName=" + user + ")", controls);
        while (results.hasMore()) {
            SearchResult searchResult = results.next();
            Attributes attributes = searchResult.getAttributes();
            if (attributes.get(USERACCOUNTCONTROL) != null) {
                return isUserActive(attributes.get(USERACCOUNTCONTROL));
            }
        }
        return false;
    }

    private boolean isUserActive(Attribute userAccountControl) throws NamingException {
        return (Integer.parseInt(userAccountControl.get().toString()) & 2) == 0;
    }

    @Override
    public boolean getLdapUserStatus(String userName) {
        return getSomethingFromLdap(this::getUserStatusFromContext, userName);
    }

    private Hashtable<String, Object> createEnvironment(String url, String username, String password) {
        return createEnvironment(url, username, password, null, null);
    }

    private Hashtable<String, Object> createEnvironment(String url, String username, String password,
            String securityProtocol, SslSecurityProperties sslSecurityProperties) {
        Hashtable<String, Object> env = new Hashtable<>();
        env.putAll(commonEnvLDAP);
        env.put(Context.PROVIDER_URL, url);
        env.put(Context.SECURITY_PRINCIPAL, username);
        env.put(Context.SECURITY_CREDENTIALS, password);
        if (sslSecurityProperties != null && securityProtocol != null && sslSecurityProperties.getTrustedStore() != null
                && !is(securityProtocol).emptyOrOnlyWhiteSpace()
                && securityProtocol.toLowerCase().contains(LOWERCASE_SSL)) {
            env.put("java.naming.ldap.factory.socket", ManagedSSLSocketFactory.class.getName());
            ManagedSSLSocketFactory.setSocketFactory(
                    new ManagedSSLSocketFactory(getSocketFactory(sslSecurityProperties, securityProtocol)));
            Thread.currentThread().setContextClassLoader(ManagedSSLSocketFactory.class.getClassLoader());
        }
        if (!is(securityProtocol).emptyOrOnlyWhiteSpace()) {
            env.put(Context.SECURITY_PROTOCOL, LOWERCASE_SSL);
        }
        return env;
    }

    @Override
    protected DirContext createDirContextSimple(String url) throws NamingException {
        return new InitialDirContext(createEnvironment(url, getDirectoryUser(), getPasswordDecrypt()));
    }

    @Override
    protected DirContext createDirContextSsl(String url, SslSecurityProperties sslSecurityProperties)
            throws NamingException {
        return new InitialDirContext(
                createEnvironment(url, getDirectoryUser(), getPasswordDecrypt(), LOWERCASE_SSL, sslSecurityProperties));
    }

    @Override
    protected Pair<LdapContext, StartTlsResponse> createDirContextTls(String url,
            SslSecurityProperties sslSecurityProperties) throws NamingException, IOException {
        LdapContext ctx = new InitialLdapContext(createEnvironment(url, getDirectoryUser(), getPasswordDecrypt()),
                null);
        ExtendedRequest tlsRequest = new StartTlsRequest();
        ExtendedResponse tlsResponse = ctx.extendedOperation(tlsRequest);
        StartTlsResponse tls = (StartTlsResponse) tlsResponse;
        try {
            tls.negotiate(getSocketFactory(sslSecurityProperties, TLS));
        } catch (IOException | RuntimeException e) {
            tls.close();
            throw e;
        }
        return Pair.of(ctx, tls);
    }

    @Override
    protected String getFilterForGroupNames() {
        return OBJECT_CLASS_GROUP;
    }
}