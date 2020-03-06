/*
 *
 *  * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 *
 *
 */

package com.elster.jupiter.users.blacklist;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

/**
 * Insert your comments here.
 *
 * @author E492165 (M R)
 * @since 12/26/2019 (12:32)
 */
@ProviderType
public interface BlackListToken {

    long getUserId();
    void setUserId(long userId);

    String getToken();
    void setToken(String token);

    Instant getCreateTime();

    void save();

    void update();

    void delete();

}