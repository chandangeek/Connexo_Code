package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.google.common.base.Optional;

import javax.inject.Inject;
import java.util.List;

public class ActiveDirectoryImpl extends AbstractLdapDirectoryImpl {

    static String TYPE_IDENTIFIER = "ACD";

    @Inject
    public ActiveDirectoryImpl(DataModel dataModel, UserService userService) {
        super(dataModel, userService);
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
        //TODO get the groups
        return null;
    }

    @Override
    public Optional<User> authenticate(String name, String password) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
