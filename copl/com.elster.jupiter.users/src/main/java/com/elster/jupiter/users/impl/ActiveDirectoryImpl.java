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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        Hashtable<String, Object> env = new Hashtable<>();
        env.putAll(commonEnvLDAP);
        env.put(Context.PROVIDER_URL, getUrl());
        env.put(Context.SECURITY_PRINCIPAL, getDirectoryUser());
        env.put(Context.SECURITY_CREDENTIALS, getPasswordDecrypt());

        List<Group> groupList = new ArrayList<>();
        try {
            DirContext context = new InitialDirContext(env);
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
    public Optional<User> authenticate(String name, String password) {
        List<String> urls = getUrls();
        if (getSecurity() == null || getSecurity().toUpperCase().contains("NONE")) {
            return authenticateSimple(name, password, urls);
        } else if (getSecurity().toUpperCase().contains("SSL")) {
            return authenticateSSL(name, password, urls);
        } else if (getSecurity().toUpperCase().contains("TLS")) {
            return authenticateTLS(name, password, urls);
        } else {
            return Optional.empty();
        }
    }

    private Optional<User> authenticateSimple(String name, String password, List<String> urls) {
        Hashtable<String, Object> env = new Hashtable<>();
        env.putAll(commonEnvLDAP);
        env.put(Context.PROVIDER_URL, urls.get(0));
        env.put(Context.SECURITY_PRINCIPAL, name + "@" + getRealDomain(getBaseUser()));
        env.put(Context.SECURITY_CREDENTIALS, password);
        try {
            new InitialDirContext(env);
            return findUser(name);
        } catch (NumberFormatException | NamingException e) {
            if (urls.size() > 1) {
                urls.remove(0);
                return authenticateSimple(name, password, urls);
            } else {
                return Optional.empty();
            }
        }
    }

    private Optional<User> authenticateSSL(String name, String password, List<String> urls) {
        Hashtable<String, Object> env = new Hashtable<>();
        env.putAll(commonEnvLDAP);
        env.put(Context.PROVIDER_URL, urls.get(0));
        env.put(Context.SECURITY_PRINCIPAL, name + "@" + getRealDomain(getBaseUser()));
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put(Context.SECURITY_PROTOCOL, "ssl");
        try {
            new InitialDirContext(env);
            return findUser(name);
        } catch (NumberFormatException | NamingException e) {
            if (urls.size() > 1) {
                urls.remove(0);
                return authenticateSSL(name, password, urls);
            } else {
                return Optional.empty();
            }
        }
    }

    private Optional<User> authenticateTLS(String name, String password, List<String> urls) {
        Hashtable<String, Object> env = new Hashtable<>();
        StartTlsResponse tls = null;
        env.putAll(commonEnvLDAP);
        env.put(Context.PROVIDER_URL, urls.get(0));
        try {
            env.put(Context.SECURITY_PRINCIPAL, name + "@" + getRealDomain(getBaseUser()));
            env.put(Context.SECURITY_CREDENTIALS, password);
            LdapContext ctx = new InitialLdapContext(env, null);
            ExtendedRequest tlsRequest = new StartTlsRequest();
            ExtendedResponse tlsResponse = ctx.extendedOperation(tlsRequest);
            tls = (StartTlsResponse) tlsResponse;
            tls.negotiate();
            return findUser(name);
        } catch (NumberFormatException | IOException | NamingException e) {
            if (urls.size() > 1) {
                urls.remove(0);
                return authenticateTLS(name, password, urls);
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
    public List<LdapUser> getLdapUsers() {
        List<String> urls = getUrls();
        if (getSecurity() == null || getSecurity().toUpperCase().contains("NONE")) {
            return getLdapUsersSimple(urls);
        } else if (getSecurity().toUpperCase().contains("SSL")) {
            return getLdapUsersSSL(urls);
        } else if (getSecurity().toUpperCase().contains("TLS")) {
            return getLdapUsersTLS(urls);
        } else {
            return null;
        }
    }

    private List<LdapUser> getLdapUsersSimple(List<String> urls) {
        Hashtable<String, Object> env = new Hashtable<>();
        List<LdapUser> ldapUsers = new ArrayList<>();
        env.putAll(commonEnvLDAP);
        env.put(Context.PROVIDER_URL, urls.get(0));
        env.put(Context.SECURITY_PRINCIPAL, getDirectoryUser());
        env.put(Context.SECURITY_CREDENTIALS, getPasswordDecrypt());
        try {
            String userName;
            DirContext ctx = new InitialDirContext(env);
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration results = ctx.search(getBaseUser(), "(objectclass=person)", controls);
            while (results.hasMore()) {
                LdapUser ldapUser = new LdapUserImpl();
                SearchResult searchResult = (SearchResult) results.next();
                Attributes attributes = searchResult.getAttributes();
                if (attributes.get("sAMAccountName") != null) {
                    userName = attributes.get("sAMAccountName").get().toString();
                    ldapUser.setUsername(userName);
                    ldapUser.setStatus(true);
                    if (attributes.get("useraccountcontrol") != null) {
                        if ((Integer.parseInt(attributes.get("useraccountcontrol").get().toString()) & 2) != 0) {
                            ldapUser.setStatus(false);
                        }
                    }
                    ldapUsers.add(ldapUser);
                }

            }
            return ldapUsers;
        } catch (NumberFormatException | NamingException e) {
            if (urls.size() > 1){
                urls.remove(0);
                return getLdapUsersSimple(urls);
            } else {
                throw new LdapServerException(userService.getThesaurus());
            }
        }
    }


    private List<LdapUser> getLdapUsersSSL(List<String> urls) {
        Hashtable<String, Object> env = new Hashtable<>();
        env.putAll(commonEnvLDAP);
        List<LdapUser> ldapUsers = new ArrayList<>();
        env.put(Context.PROVIDER_URL, urls.get(0));
        env.put(Context.SECURITY_PRINCIPAL, getDirectoryUser());
        env.put(Context.SECURITY_CREDENTIALS, getPasswordDecrypt());
        env.put(Context.SECURITY_PROTOCOL, "ssl");
        try {
            String userName;
            DirContext ctx = new InitialDirContext(env);
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration results = ctx.search(getBaseUser(), "(objectclass=person)", controls);
            while (results.hasMore()) {
                LdapUser ldapUser = new LdapUserImpl();
                SearchResult searchResult = (SearchResult) results.next();
                Attributes attributes = searchResult.getAttributes();
                if (attributes.get("sAMAccountName") != null) {
                    userName = attributes.get("sAMAccountName").get().toString();
                    ldapUser.setUsername(userName);
                    ldapUser.setStatus(true);
                    if (attributes.get("useraccountcontrol") != null) {
                        if ((Integer.parseInt(attributes.get("useraccountcontrol").get().toString()) & 2) != 0) {
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
                return getLdapUsersSSL(urls);
            } else {
                throw new LdapServerException(userService.getThesaurus());
            }
        }
    }

    private List<LdapUser> getLdapUsersTLS(List<String> urls) {
        Hashtable<String, Object> env = new Hashtable<>();
        List<LdapUser> ldapUsers = new ArrayList<>();
        StartTlsResponse tls = null;
        env.putAll(commonEnvLDAP);
        env.put(Context.PROVIDER_URL, urls.get(0));
        try {
            String userName;
            env.put(Context.SECURITY_PRINCIPAL, getDirectoryUser());
            env.put(Context.SECURITY_CREDENTIALS, getPasswordDecrypt());
            LdapContext ctx = new InitialLdapContext(env, null);
            ExtendedRequest tlsRequest = new StartTlsRequest();
            ExtendedResponse tlsResponse = ctx.extendedOperation(tlsRequest);
            tls = (StartTlsResponse) tlsResponse;
            tls.negotiate();
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration results = ctx.search(getBaseUser(), "(&(objectclass=person))", controls);
            while (results.hasMore()) {
                LdapUser ldapUser = new LdapUserImpl();
                SearchResult searchResult = (SearchResult) results.next();
                Attributes attributes = searchResult.getAttributes();
                if (attributes.get("sAMAccountName") != null) {
                    userName = attributes.get("sAMAccountName").get().toString();
                    ldapUser.setUsername(userName);
                    ldapUser.setStatus(true);
                    if (attributes.get("useraccountcontrol") != null) {
                        if ((Integer.parseInt(attributes.get("useraccountcontrol").get().toString()) & 2) != 0) {
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
                return getLdapUsersTLS(urls);
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
        env.put(Context.SECURITY_PRINCIPAL, getDirectoryUser());
        env.put(Context.SECURITY_CREDENTIALS, getPasswordDecrypt());
        try {
            DirContext ctx = new InitialDirContext(env);
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
            NamingEnumeration results = ctx.search(getBaseUser(), "(sAMAccountName=" + user + ")", controls);
            while (results.hasMore()) {
                SearchResult searchResult = (SearchResult) results.next();
                Attributes attributes = searchResult.getAttributes();
                if (attributes.get("useraccountcontrol") != null) {
                    if ((Integer.parseInt(attributes.get("useraccountcontrol").get().toString()) & 2) != 0) {
                        return false;
                    } else {
                        return true;
                    }
                }
            }
            return false;
        } catch (NumberFormatException | NamingException e) {
            if (urls.size() > 1) {
                urls.remove(0);
                return getLdapUserStatusSimple(user, urls);
            } else {
                throw new LdapServerException(userService.getThesaurus());
            }
        }
    }

    private boolean getLdapUserStatusSSL(String user, List<String> urls) {
        Hashtable<String, Object> env = new Hashtable<>();
        env.putAll(commonEnvLDAP);
        env.put(Context.PROVIDER_URL, urls.get(0));
        env.put(Context.SECURITY_PRINCIPAL, getDirectoryUser());
        env.put(Context.SECURITY_CREDENTIALS, getPasswordDecrypt());
        env.put(Context.SECURITY_PROTOCOL, "ssl");
        try {
            DirContext ctx = new InitialDirContext(env);
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration results = ctx.search(getBaseUser(), "(sAMAccountName=" + user + ")", controls);
            while (results.hasMore()) {
                SearchResult searchResult = (SearchResult) results.next();
                Attributes attributes = searchResult.getAttributes();
                if (attributes.get("useraccountcontrol") != null) {
                    if ((Integer.parseInt(attributes.get("useraccountcontrol").get().toString()) & 2) != 0) {
                        return false;
                    } else {
                        return true;
                    }
                }
            }
            return false;
        } catch (NumberFormatException | NamingException e) {
            if (urls.size() > 1) {
                urls.remove(0);
                return getLdapUserStatusSSL(user, urls);
            } else {
                throw new LdapServerException(userService.getThesaurus());
            }
        }
    }

    private boolean getLdapUserStatusTLS(String user, List<String> urls) {
        Hashtable<String, Object> env = new Hashtable<>();
        env.putAll(commonEnvLDAP);
        StartTlsResponse tls = null;
        env.put(Context.PROVIDER_URL, urls.get(0));
        try {
            env.put(Context.SECURITY_PRINCIPAL, getDirectoryUser());
            env.put(Context.SECURITY_CREDENTIALS, getPasswordDecrypt());
            LdapContext ctx = new InitialLdapContext(env, null);
            ExtendedRequest tlsRequest = new StartTlsRequest();
            ExtendedResponse tlsResponse = ctx.extendedOperation(tlsRequest);
            tls = (StartTlsResponse) tlsResponse;
            tls.negotiate();
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration results = null;
            results = ctx.search(getBaseUser(), "(sAMAccountName=" + user + ")", controls);
            while (results.hasMore()) {
                SearchResult searchResult = (SearchResult) results.next();
                Attributes attributes = searchResult.getAttributes();
                if (attributes.get("useraccountcontrol") != null) {
                    if ((Integer.parseInt(attributes.get("useraccountcontrol").get().toString()) & 2) != 0) {
                        return false;
                    } else {
                        return true;
                    }
                }
            }
            return false;
        } catch (NumberFormatException | IOException | NamingException e) {
            if (urls.size() > 1) {
                urls.remove(0);
                return getLdapUserStatusTLS(user, urls);
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
            return getLdapUserStatusSSL(userName, urls);
        } else if (getSecurity().toUpperCase().contains("TLS")) {
            return getLdapUserStatusTLS(userName, urls);
        } else {
            return false;
        }
    }

}