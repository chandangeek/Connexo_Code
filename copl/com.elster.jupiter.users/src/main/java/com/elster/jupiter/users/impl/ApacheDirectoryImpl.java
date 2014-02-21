package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.google.common.base.Optional;

import javax.inject.Inject;
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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Optional<User> authenticate(String name, String password) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
