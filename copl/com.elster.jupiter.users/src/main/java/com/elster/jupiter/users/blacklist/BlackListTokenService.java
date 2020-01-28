/*
 *
 *  * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 *
 *
 */

package com.elster.jupiter.users.blacklist;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

/**
 * Insert your comments here.
 *
 * @author E492165 (M R)
 * @since 12/30/2019 (13:02)
 */

@ProviderType
public interface BlackListTokenService {
    String COMPONENTNAME = "BLT";
    BlackListTokenBuilder getBlackListTokenService();
    Optional<BlackListToken> findToken(long userId, String token);
    void deleteExpiredTokens();
    @ProviderType
    interface BlackListTokenBuilder {
        BlackListTokenBuilder setUerId(long userId);
        BlackListTokenBuilder setToken(String token);

        BlackListTokenBuilder save();
        BlackListTokenBuilder update();
        BlackListTokenBuilder delete();
    }

}
