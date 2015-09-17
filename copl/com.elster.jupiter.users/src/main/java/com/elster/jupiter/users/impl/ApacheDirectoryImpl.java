package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.LdapUser;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.naming.ldap.*;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import java.io.IOException;
import java.security.KeyStore;
import java.util.*;

public class ApacheDirectoryImpl extends AbstractLdapDirectoryImpl {
    static String TYPE_IDENTIFIER = "APD";
    StartTlsResponse tls = null;
    @Inject
    public ApacheDirectoryImpl(DataModel dataModel, UserService userService) {
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
        if (isManageGroupsInternal()){
            return ((UserImpl) user).doGetGroups();
        }

        Hashtable<String, Object> env = new Hashtable<>();
        env.putAll(commonEnvLDAP);
        env.put(Context.PROVIDER_URL, getUrl());
        env.put(Context.SECURITY_PRINCIPAL, getDirectoryUser());
        env.put(Context.SECURITY_CREDENTIALS, getPassword());

        List<Group> groupList = new ArrayList<>();
        try {
            DirContext context = new InitialDirContext(env);
            String[] attrIDs = {"cn"};
            SearchControls controls = new SearchControls(SearchControls.ONELEVEL_SCOPE, 0, 0, attrIDs, true, true);
            NamingEnumeration<SearchResult> answer = context.search(getBaseGroup(),"(&(objectClass=groupOfNames)(member=uid=" + user.getName() + "," + getBaseUser()+"))",controls);
            while(answer.hasMore()) {
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
        if(getSecurity()==null||getSecurity().toUpperCase().contains("NONE")){
            return authenticateSimple(name, password, urls);
        }else if(getSecurity().toUpperCase().contains("SSL")) {
            return authenticateSSL(name, password,urls);
        }else if(getSecurity().toUpperCase().contains("TLS")){
            return authenticateTLS(name,password,urls);
        }else{
            return Optional.empty();
        }
    }

    private Optional<User> authenticateSimple(String name, String password,List<String> urls){
        Hashtable<String, Object> env = new Hashtable<>();
        env.putAll(commonEnvLDAP);
        env.put(Context.PROVIDER_URL, urls.get(0));
        env.put(Context.SECURITY_PRINCIPAL,"uid=" + name + "," + getBaseUser());
        env.put(Context.SECURITY_CREDENTIALS, password);
        try {
            new InitialDirContext(env);
            return Optional.of(userService.findOrCreateUser(name, this.getDomain(), TYPE_IDENTIFIER));
        } catch (NamingException e) {
            if((urls.size()>1)&&(e.toString().contains("CommunicationException")||e.toString().contains("ServiceUnavailableException"))){
                urls.remove(0);
                return authenticateSimple(name,password,urls);
            }else {
                return Optional.empty();
            }
        }
    }

    private Optional<User> authenticateSSL(String name, String password,List<String> urls){
        Hashtable<String, Object> env = new Hashtable<>();
        env.putAll(commonEnvLDAP);
        env.put(Context.PROVIDER_URL, urls.get(0));
        env.put(Context.SECURITY_PRINCIPAL,"uid=" + name + "," + getBaseUser());
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put(Context.SECURITY_PROTOCOL,"ssl");
        try {
            new InitialDirContext(env);
            return Optional.of(userService.findOrCreateUser(name, this.getDomain(), TYPE_IDENTIFIER));
        } catch (NamingException e) {
            if((urls.size()>1)&&(e.toString().contains("CommunicationException")||e.toString().contains("ServiceUnavailableException"))){
                urls.remove(0);
                return authenticateSSL(name, password, urls);
            }else {
                return Optional.empty();
            }
        }
    }

    private Optional<User> authenticateTLS(String name, String password,List<String> urls){
        Hashtable<String, Object> env = new Hashtable<>();
        env.putAll(commonEnvLDAP);
        env.put(Context.PROVIDER_URL, urls.get(0));
        try{
            LdapContext ctx = new InitialLdapContext(env, null);
            ExtendedRequest tlsRequest = new StartTlsRequest();
            ExtendedResponse tlsResponse = ctx.extendedOperation(tlsRequest);
            tls = (StartTlsResponse)tlsResponse;
            tls.negotiate();
            env.put(Context.SECURITY_PRINCIPAL,"uid=" + name + "," + getBaseUser());
            env.put(Context.SECURITY_CREDENTIALS, password);
            Optional<User> user = Optional.of(userService.findOrCreateUser(name, this.getDomain(), TYPE_IDENTIFIER));
            return user;
        }catch(IOException | NamingException e){
            if((urls.size()>1)&&(e.toString().contains("CommunicationException")||e.toString().contains("ServiceUnavailableException"))){
                urls.remove(0);
                return authenticateTLS(name, password, urls);
            }else {
                return Optional.empty();
            }
        }finally {
            if(tls != null){
                try {
                    tls.close();
                }catch (IOException e){

                }
            }
        }

    }

    private List<String> getUrls(){
        List<String> urls = new ArrayList<String>();
        urls.add(getUrl());
        if(getBackupUrl() != null) {
            String[] backupUrls = getBackupUrl().split(";");
            Arrays.stream(backupUrls).forEach(s -> urls.add(s));
        }
        return urls;
    }

    @Override
    public List<LdapUser> getLdapUsers() {
        List<String> urls = getUrls();
        if(getSecurity()==null||getSecurity().toUpperCase().contains("NONE")){
            return getLdapUsersSimple(urls);
        }else if(getSecurity().toUpperCase().contains("SSL")) {
            return getLdapUsersSSL(urls);
        }else if(getSecurity().toUpperCase().contains("TLS")){
            return getLdapUsersTLS(urls);
        }else{
            return null;
        }
    }

    private List<LdapUser> getLdapUsersSimple(List<String> urls){
        Hashtable<String, Object> env = new Hashtable<>();
        List<LdapUser> ldapUsers = new ArrayList<>();
        env.putAll(commonEnvLDAP);
        NamingEnumeration results = null;
        env.put(Context.PROVIDER_URL, urls.get(0));
        env.put(Context.SECURITY_PRINCIPAL,"uid=" + getDirectoryUser() + "," + getBaseUser());
        env.put(Context.SECURITY_CREDENTIALS, getPassword());
        try {
            String userName;
            DirContext ctx = new InitialDirContext(env);
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            results = ctx.search(getBaseUser(),"(objectclass=person)", controls);
            while (results.hasMore()) {
                LdapUser ldapUser = new LdapUserImpl();
                SearchResult searchResult = (SearchResult) results.next();
                Attributes attributes = searchResult.getAttributes();
                if (attributes.get("uid")!=null) {
                    userName = attributes.get("uid").get().toString();
                    ldapUser.setUsername(userName);
                    ldapUser.setStatus(true);
                    ldapUsers.add(ldapUser);
                }

            }
            return ldapUsers;
        } catch (NamingException e) {
            if((urls.size()>1)&&(e.toString().contains("CommunicationException")||e.toString().contains("ServiceUnavailableException"))){
                urls.remove(0);
                return getLdapUsersSimple(urls);
            }else {
                return ldapUsers;
            }
        }
    }


    private List<LdapUser> getLdapUsersSSL(List<String> urls){
        return null;
    }

    private List<LdapUser> getLdapUsersTLS(List<String> urls){
        return null;
    }

}
