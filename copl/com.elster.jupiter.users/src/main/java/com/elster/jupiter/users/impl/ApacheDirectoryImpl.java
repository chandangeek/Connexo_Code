package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class ApacheDirectoryImpl extends AbstractLdapDirectoryImpl {
    static String TYPE_IDENTIFIER = "APD";

    @Inject
    public ApacheDirectoryImpl(DataModel dataModel, UserService userService) {
        super(dataModel, userService);
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

        Hashtable<String, Object> env = new Hashtable();
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
            throw new RuntimeException(e);
        }
        return groupList;
    }

    @Override
    public Optional<User> authenticate(String name, String password) {
        Hashtable<String, Object> env = new Hashtable();
        env.putAll(commonEnvLDAP);
        env.put(Context.PROVIDER_URL, getUrl());
        env.put(Context.SECURITY_PRINCIPAL,"uid=" + name + "," + getBaseUser());
        env.put(Context.SECURITY_CREDENTIALS, password);
        try {
            new InitialDirContext(env);
            return Optional.of(userService.findOrCreateUser(name, this.getDomain(), TYPE_IDENTIFIER));
        } catch (NamingException e) {
            return Optional.absent();
        }
    }

    private String getRealDomain(String rdn) {
        return rdn.substring(rdn.contains("dc=") ? rdn.indexOf("dc=") : 0);
    }
}
