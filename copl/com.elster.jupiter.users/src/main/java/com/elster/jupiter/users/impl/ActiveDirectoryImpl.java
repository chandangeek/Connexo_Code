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

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActiveDirectoryImpl extends AbstractLdapDirectoryImpl {

    static String TYPE_IDENTIFIER = "ACD";
    StartTlsResponse tls = null;

    @Inject
    public ActiveDirectoryImpl(DataModel dataModel, UserService userService) {
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
            String attrIDs[] = {"memberOf"};
            SearchControls controls = new SearchControls(SearchControls.SUBTREE_SCOPE, 0, 0, attrIDs, true, true);
            NamingEnumeration<SearchResult> answer = context.search(getBaseUser(), "(&(objectClass=person)(userPrincipalName="+user.getName()+"@"+getRealDomain(getBaseUser())+"))", controls);
            while (answer.hasMoreElements()) {
                Attributes attrs = answer.nextElement().getAttributes();
                NamingEnumeration<? extends Attribute> e = attrs.getAll();
                while (e.hasMoreElements()) {
                    Attribute attr = e.nextElement();
                    for (int i = 0; i < attr.size(); ++i){
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
        env.put(Context.SECURITY_PRINCIPAL,  name + "@" + getRealDomain(getBaseUser()));
        env.put(Context.SECURITY_CREDENTIALS, password);
        try {
            new InitialDirContext(env);
            return userService.findUser(name);
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
        env.put(Context.SECURITY_PRINCIPAL,  name + "@" + getRealDomain(getBaseUser()));
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put(Context.SECURITY_PROTOCOL, "ssl");
        try {
            new InitialDirContext(env);
            return userService.findUser(name);
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
            env.put(Context.SECURITY_PRINCIPAL,  name + "@" + getRealDomain(getBaseUser()));
            env.put(Context.SECURITY_CREDENTIALS, password);
            return userService.findUser(name);
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
        List<String> urls = new ArrayList<>();
        urls.add(getUrl());
        if(getBackupUrl() != null) {
            String[] backupUrls = getBackupUrl().split(";");
            Arrays.stream(backupUrls).forEach(s -> urls.add(s));
        }
        return urls;
    }

    private String getRealDomain(String baseDN) {
        return baseDN.toLowerCase().replace("dc=","").replace(",",".");
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

        return null;
    }

}
