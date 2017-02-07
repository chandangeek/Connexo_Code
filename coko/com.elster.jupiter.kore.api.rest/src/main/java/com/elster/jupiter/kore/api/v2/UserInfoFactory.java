package com.elster.jupiter.kore.api.v2;


import com.elster.jupiter.users.User;

public class UserInfoFactory {
    public UserInfo asInfo(User user) {
        UserInfo info = new UserInfo();
        info.id = user.getId();
        info.name = user.getName();
        return info;
    }

}
