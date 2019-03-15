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

final class ApacheDirectoryImpl extends AbstractSecurableLdapDirectoryImpl {
    private static final String CN = "cn";
    private static final String[] CN_ARRAY = { CN };
    static final String TYPE_IDENTIFIER = "APD";
    private static final Logger LOGGER = Logger.getLogger(ApacheDirectoryImpl.class.getSimpleName());

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
    public List<Group> getGroups(User user) {
        if (isManageGroupsInternal()) {
            return ((UserImpl) user).doGetGroups();
        }

        return getSomethingFromLdap(this::doGetGroups, user);
    }

    private List<Group> doGetGroups(DirContext context, Object... args) throws NamingException {
        // https://issues.apache.org/jira/browse/DIRSERVER-1844 ApacheDS does not support memberOf virtual attribute
        // so we cannot find user and check their groups. Instead we find groups which have the user as member
        User user = (User) args[0];
        List<Group> groupList = new ArrayList<>();
        String name;
        int scope;
        if (getBaseGroup() == null) {
            // search all groups
            name = "";
            scope = SearchControls.SUBTREE_SCOPE;
        } else {
            // search all groups which are direct children of baseGroup
            name = getBaseGroup();
            scope = SearchControls.ONELEVEL_SCOPE;
        }
        SearchControls controls = new SearchControls(scope, 0, 0, CN_ARRAY, true, true);
        NamingEnumeration<SearchResult> answer = context.search(name,
                "(&(objectClass=groupOfNames)(member=uid=" + user.getName() + "," + getBase() + "))", controls);
        while (answer.hasMore()) {
            userService.findGroup(answer.next().getAttributes().get(CN).get().toString()).ifPresent(groupList::add);
        }
        return groupList;
    }

    @Override
    public Optional<User> authenticate(String name, String password) {
        List<String> urls = getUrls();
        LOGGER.info("AUTH: Autheticating LDAP user\n");
        if (getSecurity() == null || getSecurity().toUpperCase().contains("NONE")) {
            return authenticateSimple(name, password, urls);
        } else if (getSecurity().toUpperCase().contains("SSL")) {
            return authenticateSSL(name, password, urls, getSslSecurityProperties());
        } else if (getSecurity().toUpperCase().contains("TLS")) {
            return authenticateTLS(name, password, urls, getSslSecurityProperties());
        } else {
            return Optional.empty();
        }
    }

    private Optional<User> authenticateSimple(String name, String password, List<String> urls) {
        LOGGER.info("AUTH: No security applied\n");
        Hashtable<String, Object> env = new Hashtable<>();
        env.putAll(commonEnvLDAP);
        env.put(Context.PROVIDER_URL, urls.get(0));
        putSecurityPrincipal(name, env);
        env.put(Context.SECURITY_CREDENTIALS, password);
        try {
            new InitialDirContext(env);
            return findUser(name);
        } catch (NumberFormatException | NamingException e) {
            LOGGER.severe("AUTH: Simple authetication failed\n");
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
            e.printStackTrace();
            if (urls.size() > 1) {
                urls.remove(0);
                return authenticateSimple(name, password, urls);
            } else {
                return Optional.empty();
            }
        }
    }

    private Optional<User> authenticateSSL(String name, String password, List<String> urls,
            SslSecurityProperties sslSecurityProperties) {
        LOGGER.info("AUTH: SSL applied\n");
        Hashtable<String, Object> env = new Hashtable<>();
        env.putAll(commonEnvLDAP);
        env.put(Context.PROVIDER_URL, urls.get(0));
        putSecurityPrincipal(name, env);
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put(Context.SECURITY_PROTOCOL, "ssl");
        env.put("java.naming.ldap.factory.socket", ManagedSSLSocketFactory.class.getName());
        ManagedSSLSocketFactory
                .setSocketFactory(new ManagedSSLSocketFactory(getSocketFactory(sslSecurityProperties, "SSL")));
        Thread.currentThread().setContextClassLoader(ManagedSSLSocketFactory.class.getClassLoader());
        try {
            new InitialDirContext(env);
            return findUser(name);
        } catch (NumberFormatException | NamingException e) {
            LOGGER.severe("AUTH: SSL authetication failed\n");
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
            if (urls.size() > 1) {
                urls.remove(0);
                return authenticateSSL(name, password, urls, sslSecurityProperties);
            } else {
                return Optional.empty();
            }
        }
    }

    private Optional<User> authenticateTLS(String name, String password, List<String> urls,
            SslSecurityProperties sslSecurityProperties) {
        LOGGER.info("AUTH: TLS applied\n");
        Hashtable<String, Object> env = new Hashtable<>();
        env.putAll(commonEnvLDAP);
        env.put(Context.PROVIDER_URL, urls.get(0));
        StartTlsResponse tls = null;
        try {
            LdapContext ctx = new InitialLdapContext(env, null);
            ExtendedRequest tlsRequest = new StartTlsRequest();
            ExtendedResponse tlsResponse = ctx.extendedOperation(tlsRequest);
            tls = (StartTlsResponse) tlsResponse;
            tls.negotiate(getSocketFactory(sslSecurityProperties, "TLS"));
            putSecurityPrincipal(name, env);
            env.put(Context.SECURITY_CREDENTIALS, password);
            return findUser(name);
        } catch (NumberFormatException | IOException | NamingException e) {
            LOGGER.severe("AUTH: TLS authetication failed\n");
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
            if (urls.size() > 1) {
                urls.remove(0);
                return authenticateTLS(name, password, urls, sslSecurityProperties);
            } else {
                return Optional.empty();
            }
        } finally {
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
            // search in groupName which contains users as direct children
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
        NamingEnumeration<SearchResult> groupEnumeration = ctx.search(getGroupName(), "(objectClass=groupOfNames)",
                controls);
        if (groupEnumeration.hasMore()) {
            SearchResult groupSearchResult = groupEnumeration.next();
            Attributes groupAttributes = groupSearchResult.getAttributes();
            Attribute memberAttribute = groupAttributes.get(MEMBER);
            NamingEnumeration<?> membersEnumeration = memberAttribute.getAll();
            while (membersEnumeration.hasMore()) {
                String userDN = (String) membersEnumeration.next();
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
            LdapUser ldapUser = new LdapUserImpl();
            String userName = attributes.get("uid").get().toString();
            ldapUser.setUsername(userName);
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
        if (memberUser.indexOf(',') > 2) {
            // cn=John Doe,dc=example,dc=com
            userControls.setSearchScope(SearchControls.OBJECT_SCOPE);
            start = memberUser; //
            filter = "(objectClass=person)";
        } else {
            // uid=Join123
            userControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            start = "";
            filter = "(&(objectClass=person)(" + memberUser + "))";
        }

        NamingEnumeration<SearchResult> userEnumeration = ctx.search(start, filter, userControls);
        if (userEnumeration.hasMore()) {
            addUser(userEnumeration, ldapUsers);
        }
    }

    private void putSecurityPrincipal(String name, Hashtable<String, Object> env) {
        String principal;
        if (getGroupName() == null) {
            principal = "uid=" + name + "," + getBaseUser();
        } else {
            principal = name;
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

    private void putSecurityPrincipal(Hashtable<String, Object> env) {
        putSecurityPrincipal(getDirectoryUser(), env);
    }

    @Override
    protected DirContext createDirContextSsl(String url, SslSecurityProperties sslSecurityProperties)
            throws NamingException {
        Hashtable<String, Object> env = new Hashtable<>();
        env.putAll(commonEnvLDAP);
        env.put(Context.PROVIDER_URL, url);
        putSecurityPrincipal(env);
        env.put(Context.SECURITY_CREDENTIALS, getPasswordDecrypt());
        env.put(Context.SECURITY_PROTOCOL, "ssl");
        env.put("java.naming.ldap.factory.socket", ManagedSSLSocketFactory.class.getName());
        ManagedSSLSocketFactory
                .setSocketFactory(new ManagedSSLSocketFactory(getSocketFactory(sslSecurityProperties, "SSL")));
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
            tls.negotiate(getSocketFactory(sslSecurityProperties, "TLS"));
            putSecurityPrincipal(env);
            env.put(Context.SECURITY_CREDENTIALS, getPasswordDecrypt());
        } catch (IOException | RuntimeException e) {
            tls.close();
            throw e;
        }
        return Pair.of(ctx, tls);
    }
}
