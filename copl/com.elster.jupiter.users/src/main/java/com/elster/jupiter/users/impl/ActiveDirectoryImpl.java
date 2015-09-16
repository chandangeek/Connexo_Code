package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActiveDirectoryImpl extends AbstractLdapDirectoryImpl {

    static String TYPE_IDENTIFIER = "ACD";

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
        Hashtable<String, Object> env = new Hashtable<>();
        env.putAll(commonEnvLDAP);
        env.put(Context.PROVIDER_URL, getUrl());
        env.put(Context.SECURITY_PRINCIPAL,  name + "@" + getRealDomain(getBaseUser()));
        env.put(Context.SECURITY_CREDENTIALS, password);
        try {
            new InitialDirContext(env);
            return Optional.of(userService.findOrCreateUser(name, this.getDomain(), TYPE_IDENTIFIER));
        } catch (NamingException e) {
            return Optional.empty();
        }
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
    public List<String> getLdapUsers() {

        return null;
    }

}
