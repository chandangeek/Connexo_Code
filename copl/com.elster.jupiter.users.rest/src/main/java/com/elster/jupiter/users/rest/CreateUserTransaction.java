package com.elster.jupiter.users.rest;

import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.User;


public class CreateUserTransaction implements Transaction<User> {

    private final UserInfo info;

    public CreateUserTransaction(UserInfo info) {
        this.info = info;
    }

    @Override
    public User perform() {
        User user = Bus.getUserService().newUser(info.authenticationName);
        user.setDescription(info.description);

        user.save();

        return user;
    }
}