package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.LdapUser;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class InternalDirectoryImpl extends AbstractUserDirectoryImpl {

    static String TYPE_IDENTIFIER = "INT";

    @Inject
    public InternalDirectoryImpl(DataModel dataModel, UserService userService) {
        super(dataModel, userService);
        setType(TYPE_IDENTIFIER);
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
    public List<LdapUser> getLdapUsers() {
        return null;
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

        return Optional.empty();
    }

    private boolean checkPassword(User user, String name, String password) {
        return new DigestHa1Util().createHa1(getDomain(), name, password).equals(user.getDigestHa1());
    }

    @Override
    public boolean isManageGroupsInternal() {
        return true;
    }

    @Override
    public void setManageGroupsInternal(boolean manageGroupsInternal){
    }

    @Override
    public boolean getLdapUserStatus(String user){
        return false;
    }


}
