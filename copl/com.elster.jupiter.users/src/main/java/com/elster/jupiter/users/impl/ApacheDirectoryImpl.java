/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.LdapUser;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Checks;
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

final class ApacheDirectoryImpl extends AbstractSecurableLdapDirectoryImpl {
    static final String TYPE_IDENTIFIER = "APD";
    private static final Logger LOGGER = Logger.getLogger(ApacheDirectoryImpl.class.getSimpleName());
    private static final String[] CN_ARRAY = {CN};

    @Inject
    ApacheDirectoryImpl(DataModel dataModel, UserService userService, BundleContext context) {
        super(dataModel, userService, context);
        setType(TYPE_IDENTIFIER);
    }

    static ApacheDirectoryImpl from(DataModel dataModel, String domain) {
        return dataModel.getInstance(ApacheDirectoryImpl.class).init(domain);
    }

    ApacheDirectoryImpl init(String domain) {
        setDomain(domain);
        return this;
    }

    @Override
    protected List<Group> doGetGroups(DirContext context, Object... args) throws NamingException {
        List<Group> groupList = new ArrayList<>();
        // https://issues.apache.org/jira/browse/DIRSERVER-1844 ApacheDS does not support memberOf virtual attribute
        // so we cannot find user and check their groups. Instead we find groups which have the user as member
        User user = (User) args[0];
        SearchResult userSearchResult = findLDAPUser(context, user.getName());
        if (userSearchResult == null) {
            throw new NamingException(); // no need to provide message, the exception is caught in getSomethingXXX and next URL is used
        }
        String parentDnSuffix = getParentDnSuffix(userSearchResult);
        String groupFilter = new StringBuilder("(&(objectClass=groupOfNames)(member=uid=").append(user.getName())
                .append(parentDnSuffix).append("))").toString();
        String name;
        int scope;
        if (Checks.is(getBaseGroup()).emptyOrOnlyWhiteSpace()) {
            name = "";
            scope = SearchControls.SUBTREE_SCOPE;
        } else {
            name = getBaseGroup();
            scope = SearchControls.ONELEVEL_SCOPE;
        }
        SearchControls controls = new SearchControls(scope, 0, 0, CN_ARRAY, true, true);
        NamingEnumeration<SearchResult> answer = context.search(name, groupFilter, controls);
        processCn(answer, (String cn, Attributes attributes) -> userService.findGroup(cn).ifPresent(groupList::add));
        return groupList;
    }

    private String getParentDnSuffix(SearchResult userSearchResult) {
        String fullDn = userSearchResult.getNameInNamespace();
        int indexOfComma = fullDn.indexOf(',');
        if (indexOfComma > 0) {
            return fullDn.substring(indexOfComma);
        }
        return "";
    }

    private SearchResult findLDAPUser(DirContext context, String userName) throws NamingException {
        SearchControls controls = new SearchControls();
        String name;
        if (getGroupName() == null) {
            name = getBaseUser();
            controls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        } else {
            name = "";
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        }
        NamingEnumeration<SearchResult> answer = context.search(name, escapeCharactersInFilter("(&(objectClass=person)(uid=" + userName + "))"),
                controls);
        if (answer.hasMore()) {
            return answer.next();
        }
        return null;
    }

    //As per OWASP recommendations
    public static final String escapeCharactersInFilter(String filter) {
        StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < filter.length(); i++) {
            char currentChar = filter.charAt(i);
            switch (currentChar) {
                case '\\':
                    strBuilder.append("\\5c");
                    break;
                case '*':
                    strBuilder.append("\\2a");
                    break;
                case '(':
                    strBuilder.append("\\28");
                    break;
                case ')':
                    strBuilder.append("\\29");
                    break;
                case '\u0000':
                    strBuilder.append("\\00");
                    break;
                default:
                    strBuilder.append(currentChar);
            }
        }
        return strBuilder.toString();
    }

    @Override
    public Optional<User> authenticate(String name, String password) {
        List<String> urls = getUrls();
        LOGGER.info("AUTH: Autheticating LDAP user\n");
        if (getSecurity() == null || getSecurity().toUpperCase().contains(NONE)) {
            return authenticateSimple(name, password, urls, false, name);
        } else if (getSecurity().toUpperCase().contains(SSL)) {
            return authenticateSSL(name, password, urls, getSslSecurityProperties(), false, name);
        } else if (getSecurity().toUpperCase().contains(TLS)) {
            return authenticateTLS(name, password, urls, getSslSecurityProperties(), false, name);
        } else {
            return Optional.empty();
        }
    }

    private boolean shouldUseDirectoryUserToFindUserDN() {
        return getGroupName() != null;
    }

    private Optional<User> authenticateSimple(String name, String password, List<String> urls, boolean isDn,
                                              String userName) {
        LOGGER.info("AUTH: No security applied\n");
        Hashtable<String, Object> env = new Hashtable<>();
        env.putAll(commonEnvLDAP);
        env.put(Context.PROVIDER_URL, urls.get(0));
        putSecurityPrincipal(name, password, isDn, env);
        try {
            DirContext context = new InitialDirContext(env);
            if (!isDn && shouldUseDirectoryUserToFindUserDN()) {
                String userDn = findLDAPUserDn(name, context);
                if (userDn == null) {
                    return Optional.empty();
                }
                return authenticateSimple(userDn, password, urls, true, userName);
            }
            return findUser(userName);
        } catch (NumberFormatException | NamingException e) {
            LOGGER.severe("AUTH: Simple authetication failed\n");
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
            if (urls.size() > 1) {
                urls.remove(0);
                return authenticateSimple(userName, password, urls, false, userName);
            } else {
                return Optional.empty();
            }
        }
    }

    private void putSecurityPrincipal(String name, String password, boolean isDn, Hashtable<String, Object> env) {
        if (isDn || !shouldUseDirectoryUserToFindUserDN()) {
            putSecurityPrincipal(name, env, isDn);
            env.put(Context.SECURITY_CREDENTIALS, password);
        } else {
            putSecurityPrincipal(getDirectoryUser(), env, false);
            env.put(Context.SECURITY_CREDENTIALS, getPasswordDecrypt());
        }
    }

    private String findLDAPUserDn(String name, DirContext context) throws NamingException {
        SearchResult ldapUser = findLDAPUser(context, name);
        if (ldapUser != null) {
            return ldapUser.getNameInNamespace();
        }
        return null;
    }

    private Optional<User> authenticateSSL(String name, String password, List<String> urls,
                                           SslSecurityProperties sslSecurityProperties, boolean isDn, String userName) {
        LOGGER.info("AUTH: SSL applied\n");
        Hashtable<String, Object> env = new Hashtable<>();
        env.putAll(commonEnvLDAP);
        env.put(Context.PROVIDER_URL, urls.get(0));
        putSecurityPrincipal(name, password, isDn, env);
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put(Context.SECURITY_PROTOCOL, LOWERCASE_SSL);
        env.put("java.naming.ldap.factory.socket", ManagedSSLSocketFactory.class.getName());
        ManagedSSLSocketFactory
                .setSocketFactory(new ManagedSSLSocketFactory(getSocketFactory(sslSecurityProperties, SSL)));
        Thread.currentThread().setContextClassLoader(ManagedSSLSocketFactory.class.getClassLoader());
        DirContext context = null;
        try {
            context = new InitialDirContext(env);
            if (!isDn && shouldUseDirectoryUserToFindUserDN()) {
                String userDn = findLDAPUserDn(name, context);
                if (userDn == null) {
                    return Optional.empty();
                }
                return authenticateSSL(userDn, password, urls, sslSecurityProperties, true, userName);
            }
            return findUser(userName);
        } catch (NumberFormatException | NamingException e) {
            LOGGER.severe("AUTH: SSL authetication failed\n");
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
            if (urls.size() > 1) {
                urls.remove(0);
                return authenticateSSL(userName, password, urls, sslSecurityProperties, false, userName);
            } else {
                return Optional.empty();
            }
        } finally {
            if(context != null) {
                try {
                    context.close();
                } catch (NamingException e) {
                    LOGGER.severe(e.getMessage());
                }
            }
        }
    }

    private Optional<User> authenticateTLS(String name, String password, List<String> urls,
                                           SslSecurityProperties sslSecurityProperties, boolean isDn, String userName) {
        LOGGER.info("AUTH: TLS applied\n");
        Hashtable<String, Object> env = new Hashtable<>();
        env.putAll(commonEnvLDAP);
        env.put(Context.PROVIDER_URL, urls.get(0));
        StartTlsResponse tls = null;
        LdapContext ctxs = null;
        try {
            ctxs = new InitialLdapContext(env, null);
            ExtendedRequest tlsRequest = new StartTlsRequest();
            ExtendedResponse tlsResponse = ctxs.extendedOperation(tlsRequest);
            tls = (StartTlsResponse) tlsResponse;
            tls.negotiate(getSocketFactory(sslSecurityProperties, TLS));
            putSecurityPrincipal(name, password, isDn, env);
            env.put(Context.SECURITY_CREDENTIALS, password);
            if (!isDn && shouldUseDirectoryUserToFindUserDN()) {
                String userDn = findLDAPUserDn(name, ctxs);
                if (userDn == null) {
                    return Optional.empty();
                }
                return authenticateTLS(userDn, password, urls, sslSecurityProperties, true, userName);
            }
            return findUser(userName);
        } catch (NumberFormatException | IOException | NamingException e) {
            LOGGER.severe("AUTH: TLS authetication failed\n");
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
            if (urls.size() > 1) {
                urls.remove(0);
                return authenticateTLS(userName, password, urls, sslSecurityProperties, false, userName);
            } else {
                return Optional.empty();
            }
        } finally {
            if(ctxs != null) {
                try {
                    ctxs.close();
                } catch (NamingException ex) {
                    LOGGER.severe(ex.getMessage());
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

    @Override
    public List<LdapUser> getLdapUsers() {
        return getSomethingFromLdap(this::doGetLdapUsers);
    }

    @Override
    public boolean getLdapUserStatus(String userName) {
        return getSomethingFromLdap(this::doGetLdapUserStatus, userName);
    }

    private Boolean doGetLdapUserStatus(DirContext ctx, Object... args) throws NamingException {
        String user = (String) args[0];
        SearchControls controls = new SearchControls();
        NamingEnumeration<SearchResult> results;
        if (getGroupName() == null) {
            // search in user base DN which contains users as direct children
            controls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
            results = ctx.search(getBaseUser(), "(uid=" + user + ")", controls);
        } else {
            // search in whole tree
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            results = ctx.search("", "(&(objectClass=person)(uid=" + user + "))", controls);
        }
        while (results.hasMore()) {
            SearchResult searchResult = results.next();
            Attributes attributes = searchResult.getAttributes();
            if (attributes.get("uid") != null) {
                if (attributes.get("uid").toString().equals("uid: " + user)) {
                    if (attributes.get("pwdAccountLockedTime") != null) {
                        return !"000001010000Z".equals(attributes.get("pwdAccountLockedTime").get().toString());
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    private List<LdapUser> doGetLdapUsers(DirContext ctx, Object... args) throws NamingException {
        if (getGroupName() != null) {
            return doGetLdapUsersFromGroupName(ctx);
        }
        return doGetLdapUsersFromBaseUser(ctx);
    }

    private List<LdapUser> doGetLdapUsersFromGroupName(DirContext ctx) throws NamingException {
        // https://issues.apache.org/jira/browse/DIRSERVER-1844 ApacheDS does not support memberOf virtual attribute
        // so we read group attributes "member" and find users one by one
        List<LdapUser> ldapUsers = new ArrayList<>();
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.OBJECT_SCOPE);
        NamingEnumeration<SearchResult> groupEnumeration = ctx.search(getGroupName(), OBJECT_CLASS_GROUP_OF_NAMES,
                controls);
        if (groupEnumeration.hasMore()) {
            SearchResult groupSearchResult = groupEnumeration.next();
            Attributes groupAttributes = groupSearchResult.getAttributes();
            Attribute memberAttribute = groupAttributes.get(MEMBER);
            @SuppressWarnings("unchecked")
            NamingEnumeration<String> membersEnumeration = (NamingEnumeration<String>) memberAttribute.getAll();
            while (membersEnumeration.hasMore()) {
                String userDN = membersEnumeration.next();
                findAndAddUser(userDN, ldapUsers, ctx);
            }
        }
        return ldapUsers;
    }

    private List<LdapUser> doGetLdapUsersFromBaseUser(DirContext ctx) throws NamingException {
        List<LdapUser> ldapUsers = new ArrayList<>();
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration<SearchResult> results = ctx.search(getBaseUser(), "(objectclass=person)", controls);
        while (results.hasMore()) {
            addUser(results, ldapUsers);
        }
        return ldapUsers;
    }

    private void addUser(NamingEnumeration<SearchResult> results, List<LdapUser> ldapUsers) throws NamingException {
        SearchResult searchResult = results.next();
        Attributes attributes = searchResult.getAttributes();
        if (attributes.get("uid") != null) {
            LdapUserImpl ldapUser = new LdapUserImpl();
            String userName = attributes.get("uid").get().toString();
            ldapUser.setUsername(userName);
            ldapUser.setDN(searchResult.getNameInNamespace());
            ldapUser.setStatus(true);
            if (attributes.get("pwdAccountLockedTime") != null
                    && "000001010000Z".equals(attributes.get("pwdAccountLockedTime").get().toString())) {
                ldapUser.setStatus(false);
            }
            ldapUsers.add(ldapUser);
        }
    }

    private void findAndAddUser(String memberUser, List<LdapUser> ldapUsers, DirContext ctx) throws NamingException {
        SearchControls userControls = new SearchControls();
        String start;
        String filter;
        int indexOfComma = memberUser.indexOf(',');
        if (indexOfComma > 2) {
            // uid=John123,dc=example,dc=com
            userControls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
            start = memberUser.substring(indexOfComma + 1); // dc=example,dc=com
            filter = "(&(objectClass=person)(" + memberUser.substring(0, indexOfComma) + "))"; // "(&(objectClass=person)(uid=John123))"
        } else {
            // uid=John123
            // WARNING: roles will be managed internally even if Connexo property ldap.roles=true
            // because member is not in expected form "uid=userName,..." where "..." is parent DN like "dc=example,dc=com"
            userControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            start = "";
            filter = "(&(objectClass=person)(" + memberUser + "))";
        }
        NamingEnumeration<SearchResult> userEnumeration = ctx.search(start, filter, userControls);
        if (userEnumeration.hasMore()) {
            addUser(userEnumeration, ldapUsers);
        }
    }

    private void putSecurityPrincipal(Hashtable<String, Object> env) {
        putSecurityPrincipal(getDirectoryUser(), env, getBaseUser() == null);
    }

    private void putSecurityPrincipal(String name, Hashtable<String, Object> env, boolean useNameAsIs) {
        String principal;
        if (!useNameAsIs && getGroupName() == null) {
            String baseUser = getBaseUser();
            if (baseUser == null) {
                principal = getDirectoryUser();
                setBaseUser(principal);
            } else {
                principal = "uid=" + name + "," + baseUser;
            }
        } else {
            principal = name;
            setBaseUser(name);
        }
        env.put(Context.SECURITY_PRINCIPAL, principal);
    }

    @Override
    protected DirContext createDirContextSimple(String url) throws NamingException {
        Hashtable<String, Object> env = new Hashtable<>();
        env.putAll(commonEnvLDAP);
        env.put(Context.PROVIDER_URL, url);
        putSecurityPrincipal(env);
        env.put(Context.SECURITY_CREDENTIALS, getPasswordDecrypt());
        return new InitialDirContext(env);
    }

    @Override
    protected DirContext createDirContextSsl(String url, SslSecurityProperties sslSecurityProperties)
            throws NamingException {
        Hashtable<String, Object> env = new Hashtable<>();
        env.putAll(commonEnvLDAP);
        env.put(Context.PROVIDER_URL, url);
        putSecurityPrincipal(env);
        env.put(Context.SECURITY_CREDENTIALS, getPasswordDecrypt());
        env.put(Context.SECURITY_PROTOCOL, LOWERCASE_SSL);
        env.put("java.naming.ldap.factory.socket", ManagedSSLSocketFactory.class.getName());
        ManagedSSLSocketFactory
                .setSocketFactory(new ManagedSSLSocketFactory(getSocketFactory(sslSecurityProperties, SSL)));
        Thread.currentThread().setContextClassLoader(ManagedSSLSocketFactory.class.getClassLoader());
        return new InitialDirContext(env);
    }

    @Override
    protected Pair<LdapContext, StartTlsResponse> createDirContextTls(String url,
                                                                      SslSecurityProperties sslSecurityProperties) throws NamingException, IOException {
        Hashtable<String, Object> env = new Hashtable<>();
        env.putAll(commonEnvLDAP);
        env.put(Context.PROVIDER_URL, url);
        LdapContext ctx = new InitialLdapContext(env, null);
        ExtendedRequest tlsRequest = new StartTlsRequest();
        ExtendedResponse tlsResponse = ctx.extendedOperation(tlsRequest);
        StartTlsResponse tls = (StartTlsResponse) tlsResponse;
        try {
            tls.negotiate(getSocketFactory(sslSecurityProperties, TLS));
            putSecurityPrincipal(env);
            env.put(Context.SECURITY_CREDENTIALS, getPasswordDecrypt());
        } catch (IOException | RuntimeException e) {
            tls.close();
            throw e;
        }
        return Pair.of(ctx, tls);
    }
}
