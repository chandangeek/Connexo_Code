/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.LdapServerException;
import com.elster.jupiter.users.LdapUser;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
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
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

final class ApacheDirectoryImpl extends AbstractLdapDirectoryImpl {
    static String TYPE_IDENTIFIER = "APD";
    private static final Logger LOGGER = Logger.getLogger(ApacheDirectoryImpl.class.getSimpleName());

    private StartTlsResponse tls = null;

    @Inject
    ApacheDirectoryImpl(DataModel dataModel, UserService userService) {
        super(dataModel, userService);
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

        Hashtable<String, Object> env = new Hashtable<>();
        env.putAll(commonEnvLDAP);
        env.put(Context.PROVIDER_URL, getUrl());
        env.put(Context.SECURITY_PRINCIPAL, getDirectoryUser());
        env.put(Context.SECURITY_CREDENTIALS, getPasswordDecrypt());

        List<Group> groupList = new ArrayList<>();
        try {
            DirContext context = new InitialDirContext(env);
            String[] attrIDs = {"cn"};
            SearchControls controls = new SearchControls(SearchControls.ONELEVEL_SCOPE, 0, 0, attrIDs, true, true);
            NamingEnumeration<SearchResult> answer = context.search(getBaseGroup(), "(&(objectClass=groupOfNames)(member=uid=" + user.getName() + "," + getBase() + "))", controls);
            while (answer.hasMore()) {
                Group group = userService.findOrCreateGroup(answer.next().getAttributes().get("cn").get().toString());
                groupList.add(group);
            }
        } catch (NamingException e) {
            return ((UserImpl) user).doGetGroups();
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

    private Optional<User> authenticateSSL(String name, String password, List<String> urls, SslSecurityProperties sslSecurityProperties) {
        LOGGER.info("AUTH: SSL applied\n");
        Hashtable<String, Object> env = new Hashtable<>();
        env.putAll(commonEnvLDAP);
        env.put(Context.PROVIDER_URL, urls.get(0));
        putSecurityPrincipal(name, env);
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put(Context.SECURITY_PROTOCOL, "ssl");
        env.put("java.naming.ldap.factory.socket", ManagedSSLSocketFactory.class.getName());
        ManagedSSLSocketFactory.setSocketFactory(new ManagedSSLSocketFactory(getSocketFactory(sslSecurityProperties, "SSL")));
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

    private Optional<User> authenticateTLS(String name, String password, List<String> urls, SslSecurityProperties sslSecurityProperties) {
        LOGGER.info("AUTH: TLS applied\n");
        Hashtable<String, Object> env = new Hashtable<>();
        env.putAll(commonEnvLDAP);
        env.put(Context.PROVIDER_URL, urls.get(0));
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

    private List<String> getUrls() {
        List<String> urls = new ArrayList<>();
        urls.add(getUrl().trim());
        if (getBackupUrl() != null) {
            String[] backupUrls = getBackupUrl().split(";");
            Arrays.stream(backupUrls).forEach(s -> urls.add(s.trim()));
        }
        return urls;
    }

    @Override
    public List<LdapUser> getLdapUsers() {
        List<String> urls = getUrls();
        if (getSecurity() == null || getSecurity().toUpperCase().contains("NONE")) {
            return getLdapUsersSimple(urls);
        } else if (getSecurity().toUpperCase().contains("SSL")) {
            return getLdapUsersSSL(urls, getSslSecurityProperties());
        } else if (getSecurity().toUpperCase().contains("TLS")) {
            return getLdapUsersTLS(urls, getSslSecurityProperties());
        } else {
            return null;
        }
    }

    private List<LdapUser> getLdapUsersSimple(List<String> urls) {
        Hashtable<String, Object> env = new Hashtable<>();
        List<LdapUser> ldapUsers = new ArrayList<>();
        env.putAll(commonEnvLDAP);
        env.put(Context.PROVIDER_URL, urls.get(0));
        putSecurityPrincipal(env);
        env.put(Context.SECURITY_CREDENTIALS, getPasswordDecrypt());
        try {
            String userName;
            DirContext ctx = new InitialDirContext(env);
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration results = ctx.search(getBase(), "(objectclass=person)", controls);
            while (results.hasMore()) {
                LdapUser ldapUser = new LdapUserImpl();
                SearchResult searchResult = (SearchResult) results.next();
                Attributes attributes = searchResult.getAttributes();
                if (attributes.get("uid") != null) {
                    userName = attributes.get("uid").get().toString();
                    ldapUser.setUsername(userName);
                    ldapUser.setStatus(true);
                    if (attributes.get("pwdAccountLockedTime") != null) {
                        if ("000001010000Z".equals(attributes.get("pwdAccountLockedTime").get().toString())) {
                            ldapUser.setStatus(false);
                        }
                    }
                    ldapUsers.add(ldapUser);
                }

            }
            return ldapUsers;
        } catch (NumberFormatException | NamingException e) {
            if (urls.size() > 1) {
                urls.remove(0);
                return getLdapUsersSimple(urls);
            } else {
                throw new LdapServerException(userService.getThesaurus());
            }
        }
    }


    private List<LdapUser> getLdapUsersSSL(List<String> urls, SslSecurityProperties sslSecurityProperties) {
        Hashtable<String, Object> env = new Hashtable<>();
        env.putAll(commonEnvLDAP);
        List<LdapUser> ldapUsers = new ArrayList<>();
        env.put(Context.PROVIDER_URL, urls.get(0));
        putSecurityPrincipal(env);
        env.put(Context.SECURITY_CREDENTIALS, getPasswordDecrypt());
        env.put(Context.SECURITY_PROTOCOL, "ssl");
        env.put("java.naming.ldap.factory.socket", ManagedSSLSocketFactory.class.getName());
        ManagedSSLSocketFactory.setSocketFactory(new ManagedSSLSocketFactory(getSocketFactory(sslSecurityProperties, "SSL")));
        Thread.currentThread().setContextClassLoader(ManagedSSLSocketFactory.class.getClassLoader());
        try {
            String userName;
            DirContext ctx = new InitialDirContext(env);
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration results = ctx.search(getBase(), "(objectclass=person)", controls);
            while (results.hasMore()) {
                LdapUser ldapUser = new LdapUserImpl();
                SearchResult searchResult = (SearchResult) results.next();
                Attributes attributes = searchResult.getAttributes();
                if (attributes.get("uid") != null) {
                    userName = attributes.get("uid").get().toString();
                    ldapUser.setUsername(userName);
                    ldapUser.setStatus(true);
                    if (attributes.get("pwdAccountLockedTime") != null) {
                        if ("000001010000Z".equals(attributes.get("pwdAccountLockedTime").get().toString())) {
                            ldapUser.setStatus(false);
                        }
                    }
                    ldapUsers.add(ldapUser);
                }
            }
            return ldapUsers;
        } catch (NumberFormatException | NamingException e) {
            if (urls.size() > 1) {
                urls.remove(0);
                return getLdapUsersSSL(urls, sslSecurityProperties);
            } else {
                throw new LdapServerException(userService.getThesaurus());
            }
        }
    }

    private List<LdapUser> getLdapUsersTLS(List<String> urls, SslSecurityProperties sslSecurityProperties) {
        Hashtable<String, Object> env = new Hashtable<>();
        List<LdapUser> ldapUsers = new ArrayList<>();
        env.putAll(commonEnvLDAP);
        env.put(Context.PROVIDER_URL, urls.get(0));
        try {
            String userName;
            LdapContext ctx = new InitialLdapContext(env, null);
            ExtendedRequest tlsRequest = new StartTlsRequest();
            ExtendedResponse tlsResponse = ctx.extendedOperation(tlsRequest);
            tls = (StartTlsResponse) tlsResponse;
            tls.negotiate(getSocketFactory(sslSecurityProperties, "TLS"));
            putSecurityPrincipal(env);
            env.put(Context.SECURITY_CREDENTIALS, getPasswordDecrypt());
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration results = ctx.search(getBase(), "(objectclass=person)", controls);
            while (results.hasMore()) {
                LdapUser ldapUser = new LdapUserImpl();
                SearchResult searchResult = (SearchResult) results.next();
                Attributes attributes = searchResult.getAttributes();
                if (attributes.get("uid") != null) {
                    userName = attributes.get("uid").get().toString();
                    ldapUser.setUsername(userName);
                    ldapUser.setStatus(true);
                    if (attributes.get("pwdAccountLockedTime") != null) {
                        if ("000001010000Z".equals(attributes.get("pwdAccountLockedTime").get().toString())) {
                            ldapUser.setStatus(false);
                        }
                    }
                    ldapUsers.add(ldapUser);
                }
            }
            return ldapUsers;
        } catch (NumberFormatException | IOException | NamingException e) {
            if (urls.size() > 1) {
                urls.remove(0);
                return getLdapUsersTLS(urls, sslSecurityProperties);
            } else {
                throw new LdapServerException(userService.getThesaurus());
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


    private boolean getLdapUserStatusSimple(String user, List<String> urls) {
        Hashtable<String, Object> env = new Hashtable<>();
        env.putAll(commonEnvLDAP);
        env.put(Context.PROVIDER_URL, urls.get(0));
        putSecurityPrincipal(env);
        env.put(Context.SECURITY_CREDENTIALS, getPasswordDecrypt());
        try {
            DirContext ctx = new InitialDirContext(env);
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
            NamingEnumeration results = ctx.search(getBase(), "(uid=" + user + ")", controls);
            while (results.hasMore()) {
                SearchResult searchResult = (SearchResult) results.next();
                Attributes attributes = searchResult.getAttributes();
                if(attributes.get("uid")!=null) {
                    if(attributes.get("uid").toString().equals("uid: "+user)) {
                        if (attributes.get("pwdAccountLockedTime") != null) {
                            return !"000001010000Z".equals(attributes.get("pwdAccountLockedTime").get().toString());
                        }
                    }else{
                        return false;
                    }
                }else{
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException | NamingException e) {
            if (urls.size() > 1) {
                urls.remove(0);
                return getLdapUserStatusSimple(user, urls);
            } else {
                throw new LdapServerException(userService.getThesaurus());
            }
        }
    }

    private boolean getLdapUserStatusSSL(String user, List<String> urls, SslSecurityProperties sslSecurityProperties) {
        Hashtable<String, Object> env = new Hashtable<>();
        env.putAll(commonEnvLDAP);
        env.put(Context.PROVIDER_URL, urls.get(0));
        putSecurityPrincipal(env);
        env.put(Context.SECURITY_CREDENTIALS, getPasswordDecrypt());
        env.put(Context.SECURITY_PROTOCOL, "ssl");
        env.put("java.naming.ldap.factory.socket", ManagedSSLSocketFactory.class.getName());
        ManagedSSLSocketFactory.setSocketFactory(new ManagedSSLSocketFactory(getSocketFactory(sslSecurityProperties, "SSL")));
        Thread.currentThread().setContextClassLoader(ManagedSSLSocketFactory.class.getClassLoader());
        try {
            DirContext ctx = new InitialDirContext(env);
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration results = ctx.search(getBase(), "(uid=" + user + ")", controls);
            while (results.hasMore()) {
                SearchResult searchResult = (SearchResult) results.next();
                Attributes attributes = searchResult.getAttributes();
                if(attributes.get("uid")!=null) {
                    if(attributes.get("uid").toString().equals("uid: "+user)) {
                        if (attributes.get("pwdAccountLockedTime") != null) {
                            return !"000001010000Z".equals(attributes.get("pwdAccountLockedTime").get().toString());
                        }
                    }else{
                        return false;
                    }
                }else{
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException | NamingException e) {
            if (urls.size() > 1) {
                urls.remove(0);
                return getLdapUserStatusSSL(user, urls, sslSecurityProperties);
            } else {
                throw new LdapServerException(userService.getThesaurus());
            }
        }
    }

    private boolean getLdapUserStatusTLS(String user, List<String> urls, SslSecurityProperties sslSecurityProperties) {
        Hashtable<String, Object> env = new Hashtable<>();
        env.putAll(commonEnvLDAP);
        env.put(Context.PROVIDER_URL, urls.get(0));
        try {
            LdapContext ctx = new InitialLdapContext(env, null);
            ExtendedRequest tlsRequest = new StartTlsRequest();
            ExtendedResponse tlsResponse = ctx.extendedOperation(tlsRequest);
            tls = (StartTlsResponse) tlsResponse;
            tls.negotiate(getSocketFactory(sslSecurityProperties, "TLS"));
            putSecurityPrincipal(env);
            env.put(Context.SECURITY_CREDENTIALS, getPasswordDecrypt());
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration results = ctx.search(getBase(), "(uid=" + user + ")", controls);
            while (results.hasMore()) {
                SearchResult searchResult = (SearchResult) results.next();
                Attributes attributes = searchResult.getAttributes();
                if(attributes.get("uid")!=null) {
                    if(attributes.get("uid").toString().equals("uid: "+user)) {
                        if (attributes.get("pwdAccountLockedTime") != null) {
                            return !"000001010000Z".equals(attributes.get("pwdAccountLockedTime").get().toString());
                        }
                    }else{
                        return false;
                    }
                }else{
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException | IOException | NamingException e) {
            if (urls.size() > 1) {
                urls.remove(0);
                return getLdapUserStatusTLS(user, urls, sslSecurityProperties);
            } else {
                throw new LdapServerException(userService.getThesaurus());
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
    public boolean getLdapUserStatus(String userName) {
        List<String> urls = getUrls();
        if (getSecurity() == null || getSecurity().toUpperCase().contains("NONE")) {
            return getLdapUserStatusSimple(userName, urls);
        } else if (getSecurity().toUpperCase().contains("SSL")) {
            return getLdapUserStatusSSL(userName, urls, getSslSecurityProperties());
        } else if (getSecurity().toUpperCase().contains("TLS")) {
            return getLdapUserStatusTLS(userName, urls, getSslSecurityProperties());
        } else {
            return false;
        }
    }

    private void putSecurityPrincipal(Hashtable<String, Object> env) {
        putSecurityPrincipal(getDirectoryUser(), env);
    }

    private void putSecurityPrincipal(String name, Hashtable<String, Object> env) {
        env.put(Context.SECURITY_PRINCIPAL, "uid=" + name + "," + getBase());
    }

}
