package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.google.common.base.Optional;

import javax.inject.Inject;
import java.util.List;

public class InternalDirectoryImpl extends AbstractUserDirectoryImpl {

    static String TYPE_IDENTIFIER = "INT";

    @Inject
    public InternalDirectoryImpl(DataModel dataModel, UserService userService) {
        super(dataModel, userService);
    }

    static InternalDirectoryImpl from(DataModel dataModel, String domain) {
        return dataModel.getInstance(InternalDirectoryImpl.class).init(domain);
    }

    InternalDirectoryImpl init(String domain) {
        setDomain(domain);
        return this;
    }

    @Override
    public List<Group> getGroups(User user) {
        return ((UserImpl) user).doGetGroups();
    }

    @Override
    public Optional<User> authenticate(String name, String password) {
        Optional<User> found = userService.findUser(name);
        if(!found.isPresent()){
            return found;
        }

        if(checkPassword(found.get(), name, password)){
            return found;
        }

        return Optional.absent();
    }

    private boolean checkPassword(User user, String name, String password) {
        return new DigestHa1Util().createHa1(getDomain(), name, password).equals(user.getDigestHa1());
    }

    @Override
    public boolean isManageGroupsInternal() {
        return true;
    }

}
