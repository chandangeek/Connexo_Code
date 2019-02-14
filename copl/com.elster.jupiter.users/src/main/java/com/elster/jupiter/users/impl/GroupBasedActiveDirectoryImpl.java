/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;

/**
 * Implementation of Active Directory which searches users by group.
 */
final class GroupBasedActiveDirectoryImpl extends AbstractActiveDirectoryImpl {

    @Inject
    GroupBasedActiveDirectoryImpl(DataModel dataModel, UserService userService) {
        super(dataModel, userService);
        setType(TYPE_IDENTIFIER);
    }

    static GroupBasedActiveDirectoryImpl from(DataModel dataModel, String domain) {
        return dataModel.getInstance(GroupBasedActiveDirectoryImpl.class).init(domain);
    }

    @Override
    GroupBasedActiveDirectoryImpl init(String domain) {
        setDomain(domain);
        return this;
    }

    @Override
    protected NamingEnumeration getNamingEnumerationForLdapUsersFromContext(DirContext ctx) throws NamingException {
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        // TODO if it works this way, it will be possible to override just getBase and getBaseForUsers
        NamingEnumeration results = ctx.search(getGroupName(), "(objectclass=person)", controls);
        return results;
    }

    @Override
    protected NamingEnumeration getNamingEnumerationForUserFromContext(String user, DirContext context)
            throws NamingException {
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration results = context.search(getBaseForUsers(), "(sAMAccountName=" + user + ")", controls);
        return results;
    }

    @Override
    protected String getBase() {
        return getGroupName();
    }

    @Override
    protected String getBaseForUsers() {
        // we don't know where users are stored so we search whole tree
        // TODO check it really works
        return getGroupName().substring(getGroupName().indexOf("dc="));
    }

}