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
        Hashtable<String, Object> env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, getUrl());
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, "uid="+getDirectoryUser()+",ou=users,"+getDomain());
        env.put(Context.SECURITY_CREDENTIALS, getPassword());

        List<Group> groupList = new ArrayList<>();

        try {
            DirContext context = new InitialDirContext(env);
            SearchControls controls = new SearchControls(SearchControls.SUBTREE_SCOPE,0,0,null,true,true);

            NamingEnumeration<SearchResult> answer = context.search("ou=groups,"+getDomain(),"(&(objectClass=groupOfNames)(member=uid="+user.getName()+",ou=users,"+getDomain()+"))",controls);
            while(answer.hasMore()) {
                System.out.println(answer.next());
                // TODO: get each group name
                // search group name in database
                // add to the list
            }

            //return userService.findOrCreateUser(name, this.getDomain());
        } catch (NamingException e) {
            // TODO: add exception handling
            e.printStackTrace();
        }

        return groupList;
    }

    @Override
    public Optional<User> authenticate(String name, String password) {
        Hashtable<String, Object> env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, getUrl());
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, "uid="+name+",ou=users,"+getDomain());
        env.put(Context.SECURITY_CREDENTIALS, password);

        try {
            new InitialDirContext(env);

            return userService.findOrCreateUser(name, this.getDomain());
        } catch (NamingException e) {
            // TODO: add exception handling
            e.printStackTrace();
        }

        return Optional.absent();
    }
}
