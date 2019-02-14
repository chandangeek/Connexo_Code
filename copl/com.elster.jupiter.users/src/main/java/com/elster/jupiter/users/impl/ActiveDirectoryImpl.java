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

final class ActiveDirectoryImpl extends AbstractActiveDirectoryImpl {

    @Inject
    ActiveDirectoryImpl(DataModel dataModel, UserService userService) {
        super(dataModel, userService);
    }

    static ActiveDirectoryImpl from(DataModel dataModel, String domain) {
        return dataModel.getInstance(ActiveDirectoryImpl.class).init(domain);
    }

    @Override
    ActiveDirectoryImpl init(String domain) {
        setDomain(domain);
        return this;
    }

    @Override
    protected NamingEnumeration getNamingEnumerationForLdapUsersFromContext(DirContext ctx) throws NamingException {
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration results = ctx.search(getBaseUser(), "(objectclass=person)", controls);
        return results;
    }

    @Override
    protected NamingEnumeration getNamingEnumerationForUserFromContext(String user, DirContext context)
            throws NamingException {
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration results = context.search(getBaseUser(), "(sAMAccountName=" + user + ")", controls);
        return results;
    }

}