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
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.elster.jupiter.util.Checks.is;

final class ActiveDirectoryImpl extends AbstractLdapDirectoryImpl {

    static final String TYPE_IDENTIFIER = "ACD";

    @Inject
    ActiveDirectoryImpl(DataModel dataModel, UserService userService) {
        super(dataModel, userService);
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
    public List<Group> getGroups(User user) {
        if (isManageGroupsInternal()) {
            return ((UserImpl) user).doGetGroups();
        }

        List<Group> groupList = new ArrayList<>();
        try {
            DirContext context = new InitialDirContext(createEnvironment(getUrl(), getDirectoryUser(), getPasswordDecrypt()));
            String attrIDs[] = {"memberOf"};
            SearchControls controls = new SearchControls(SearchControls.SUBTREE_SCOPE, 0, 0, attrIDs, true, true);
            NamingEnumeration<SearchResult> answer = context.search(getBaseUser(), "(&(objectClass=person)(userPrincipalName=" + user.getName() + "@" + getRealDomain(getBaseUser()) + "))", controls);
            while (answer.hasMoreElements()) {
                Attributes attrs = answer.nextElement().getAttributes();
                NamingEnumeration<? extends Attribute> e = attrs.getAll();
                while (e.hasMoreElements()) {
                    Attribute attr = e.nextElement();
                    for (int i = 0; i < attr.size(); ++i) {
                        Group group = userService.findOrCreateGroup(getRealGroupName(attr.get(i).toString()));
                        groupList.add(group);
                    }
                }
            }
        } catch (NamingException e) {
            return ((UserImpl) user).doGetGroups();
        }
        return groupList;
    }

    @Override
    public Optional<User> authenticate(String name, String password, KeyStore keyStore) {
        List<String> urls = getUrls();
        if (getSecurity() == null || getSecurity().toUpperCase().contains("NONE")) {
            return authenticateSimple(name, password, urls);
        } else if (getSecurity().toUpperCase().contains("SSL")) {
            return authenticateSSL(name, password, urls, keyStore);
        } else if (getSecurity().toUpperCase().contains("TLS")) {
            return authenticateTLS(name, password, urls, keyStore);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> authenticate(String name, String password) {
        return authenticate(name, password, null);
    }

    private Optional<User> authenticateSimple(String name, String password, List<String> urls) {
        try {
            new InitialDirContext(createEnvironment(urls.get(0), name + "@" + getRealDomain(getBaseUser()), password));
            return findUser(name);
        } catch (NumberFormatException | NamingException e) {
            if (urls.size() > 1) {
                return authenticateSimple(name, password, urls.subList(1, urls.size() - 1));
            } else {
                return Optional.empty();
            }
        }
    }

    private Optional<User> authenticateSSL(String name, String password, List<String> urls, KeyStore keyStore) {
        try {
            new InitialDirContext(createEnvironment(urls.get(0), name + "@" + getRealDomain(getBaseUser()), password, "ssl", keyStore));
            return findUser(name);
        } catch (NumberFormatException | NamingException e) {
            if (urls.size() > 1) {
                return authenticateSSL(name, password, urls.subList(1, urls.size() - 1), keyStore);
            } else {
                return Optional.empty();
            }
        }
    }

    private Optional<User> authenticateTLS(String name, String password, List<String> urls, KeyStore keyStore) {
        StartTlsResponse tls = null;
        try {
            LdapContext ctx = new InitialLdapContext(createEnvironment(urls.get(0), name + "@" + getRealDomain(getBaseUser()), password), null);
            ExtendedRequest tlsRequest = new StartTlsRequest();
            ExtendedResponse tlsResponse = ctx.extendedOperation(tlsRequest);
            tls = (StartTlsResponse) tlsResponse;
            tls.negotiate(getSocketFactory(keyStore, "TLS"));
            return findUser(name);
        } catch (NumberFormatException | IOException | NamingException e) {
            if (urls.size() > 1) {
                return authenticateTLS(name, password, urls.subList(1, urls.size() - 1), keyStore);
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

    private String getRealDomain(String baseDN) {
        String normalizedDN = baseDN.toLowerCase();
        return normalizedDN.substring(normalizedDN.indexOf("dc=")).replace("dc=", "").replace(",", ".");
    }

    private String getRealGroupName(String rdn) {
        String result = rdn;
        Pattern pattern = Pattern.compile("=(.*?),");
        Matcher matcher = pattern.matcher(rdn);
        if (matcher.find()) {
            result = matcher.group(1);
        }
        return result;
    }

    @Override
    public List<LdapUser> getLdapUsers(KeyStore keyStore) {
        List<String> urls = getUrls();
        if (getSecurity() == null || getSecurity().toUpperCase().contains("NONE")) {
            return getLdapUsersSimple(urls);
        } else if (getSecurity().toUpperCase().contains("SSL")) {
            return getLdapUsersSSL(urls, keyStore);
        } else if (getSecurity().toUpperCase().contains("TLS")) {
            return getLdapUsersTLS(urls, keyStore);
        } else {
            return null;
        }
    }

    @Override
    public List<LdapUser> getLdapUsers() {
        return getLdapUsers(null);
    }

    private List<LdapUser> getLdapUsersSimple(List<String> urls) {
        try {
            return getLdapUsersFromContext(new InitialDirContext(createEnvironment(urls.get(0), getDirectoryUser(), getPasswordDecrypt())));
        } catch (NumberFormatException | NamingException e) {
            if (urls.size() > 1) {
                return getLdapUsersSimple(urls.subList(1, urls.size() - 1));
            } else {
                throw new LdapServerException(userService.getThesaurus());
            }
        }
    }


    private List<LdapUser> getLdapUsersSSL(List<String> urls, KeyStore keyStore) {
        try {
            return getLdapUsersFromContext(new InitialDirContext(createEnvironment(urls.get(0), getDirectoryUser(), getPasswordDecrypt(), "ssl", keyStore)));
        } catch (NumberFormatException | NamingException e) {
            if (urls.size() > 1) {
                return getLdapUsersSSL(urls.subList(1, urls.size() - 1), keyStore);
            } else {
                throw new LdapServerException(userService.getThesaurus());
            }
        }
    }

    private List<LdapUser> getLdapUsersTLS(List<String> urls, KeyStore keyStore) {
        StartTlsResponse tls = null;
        try {
            LdapContext ctx = new InitialLdapContext(createEnvironment(urls.get(0), getDirectoryUser(), getPasswordDecrypt()), null);
            ExtendedRequest tlsRequest = new StartTlsRequest();
            ExtendedResponse tlsResponse = ctx.extendedOperation(tlsRequest);
            tls = (StartTlsResponse) tlsResponse;
            tls.negotiate(getSocketFactory(keyStore, "TLS"));
            return getLdapUsersFromContext(ctx);
        } catch (NumberFormatException | IOException | NamingException e) {
            if (urls.size() > 1) {
                return getLdapUsersTLS(urls.subList(1, urls.size() - 1), keyStore);
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

    private List<LdapUser> getLdapUsersFromContext(DirContext ctx) throws NamingException {
        List<LdapUser> ldapUsers = new ArrayList<>();
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration results = ctx.search(getBaseUser(), "(objectclass=person)", controls);
        while (results.hasMore()) {
            LdapUser ldapUser = new LdapUserImpl();
            SearchResult searchResult = (SearchResult) results.next();
            Attributes attributes = searchResult.getAttributes();
            if (attributes.get("sAMAccountName") != null) {
                ldapUser.setUsername(attributes.get("sAMAccountName").get().toString());
                ldapUser.setStatus(true);
                if (attributes.get("useraccountcontrol") != null) {
                    ldapUser.setStatus(isUserActive(attributes.get("useraccountcontrol")));
                } else {
                    ldapUser.setStatus(true);
                }
                ldapUsers.add(ldapUser);
            }

        }
        return ldapUsers;
    }


    private boolean getLdapUserStatusSimple(String user, List<String> urls) {
        try {
            return getUserStatusFromContext(user, new InitialDirContext(createEnvironment(urls.get(0), getDirectoryUser(), getPasswordDecrypt())));
        } catch (NumberFormatException | NamingException e) {
            if (urls.size() > 1) {
                return getLdapUserStatusSimple(user, urls.subList(1, urls.size() - 1));
            } else {
                throw new LdapServerException(userService.getThesaurus());
            }
        }
    }

    private boolean getLdapUserStatusSSL(String user, List<String> urls, KeyStore keyStore) {
        try {
            return getUserStatusFromContext(user, new InitialDirContext(createEnvironment(urls.get(0), getDirectoryUser(), getPasswordDecrypt(), "ssl", keyStore)));
        } catch (NumberFormatException | NamingException e) {
            if (urls.size() > 1) {
                return getLdapUserStatusSSL(user, urls.subList(1, urls.size() - 1), keyStore);
            } else {
                throw new LdapServerException(userService.getThesaurus());
            }
        }
    }

    private boolean getLdapUserStatusTLS(String user, List<String> urls, KeyStore keyStore) {
        StartTlsResponse tls = null;
        try {
            LdapContext ctx = new InitialLdapContext(createEnvironment(urls.get(0), getDirectoryUser(), getPasswordDecrypt()), null);
            ExtendedRequest tlsRequest = new StartTlsRequest();
            ExtendedResponse tlsResponse = ctx.extendedOperation(tlsRequest);
            tls = (StartTlsResponse) tlsResponse;
            tls.negotiate(getSocketFactory(keyStore, "TLS"));
            return getUserStatusFromContext(user, ctx);
        } catch (NumberFormatException | IOException | NamingException e) {
            if (urls.size() > 1) {
                return getLdapUserStatusTLS(user, urls.subList(1, urls.size() - 1), keyStore);
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

    private boolean getUserStatusFromContext(String user, DirContext context) throws NamingException {
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration results = context.search(getBaseUser(), "(sAMAccountName=" + user + ")", controls);
        while (results.hasMore()) {
            SearchResult searchResult = (SearchResult) results.next();
            Attributes attributes = searchResult.getAttributes();
            if (attributes.get("useraccountcontrol") != null) {
                return isUserActive(attributes.get("useraccountcontrol"));
            }
        }
        return false;
    }

    private boolean isUserActive(Attribute userAccountControl) throws NamingException {
        return (Integer.parseInt(userAccountControl.get().toString()) & 2) == 0;
    }

    @Override
    public boolean getLdapUserStatus(String userName, KeyStore keyStore) {
        List<String> urls = getUrls();
        if (getSecurity() == null || getSecurity().toUpperCase().contains("NONE")) {
            return getLdapUserStatusSimple(userName, urls);
        } else if (getSecurity().toUpperCase().contains("SSL")) {
            return getLdapUserStatusSSL(userName, urls, keyStore);
        } else if (getSecurity().toUpperCase().contains("TLS")) {
            return getLdapUserStatusTLS(userName, urls, keyStore);
        } else {
            return false;
        }
    }

    @Override
    public boolean getLdapUserStatus(String userName) {
        return getLdapUserStatus(userName, null);
    }

    private Hashtable<String, Object> createEnvironment(String url, String username, String password) {
        return createEnvironment(url, username, password, null, null);
    }

    private Hashtable<String, Object> createEnvironment(String url, String username, String password, String securityProtocol, KeyStore keyStore) {
        Hashtable<String, Object> env = new Hashtable<>();
        env.putAll(commonEnvLDAP);
        env.put(Context.PROVIDER_URL, url);
        env.put(Context.SECURITY_PRINCIPAL, username);
        env.put(Context.SECURITY_CREDENTIALS, password);
        if (keyStore != null && !is(securityProtocol).emptyOrOnlyWhiteSpace() && securityProtocol.toLowerCase().contains("ssl")) {
            env.put("java.naming.ldap.factory.socket", ManagedSSLSocketFactory.class.getName());
            ManagedSSLSocketFactory.setSocketFactory(new ManagedSSLSocketFactory(getSocketFactory(keyStore, securityProtocol)));
            Thread.currentThread().setContextClassLoader(ManagedSSLSocketFactory.class.getClassLoader());
        }
        if (!is(securityProtocol).emptyOrOnlyWhiteSpace()) {
            env.put(Context.SECURITY_PROTOCOL, "ssl");
        }
        return env;
    }


}