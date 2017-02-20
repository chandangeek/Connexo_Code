/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2.issue;


import com.elster.jupiter.users.User;

public class UserInfoFactory {
    public UserInfo asInfo(User user) {
        UserInfo info = new UserInfo();
        info.id = user.getId();
        info.name = user.getName();
        return info;
    }

}
