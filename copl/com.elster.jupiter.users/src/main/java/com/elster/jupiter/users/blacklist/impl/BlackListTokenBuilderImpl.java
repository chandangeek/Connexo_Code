/*
 *
 *  * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 *
 *
 */

package com.elster.jupiter.users.blacklist.impl;

import com.elster.jupiter.users.blacklist.BlackListTokenService;


/**
 * Insert your comments here.
 *
 * @author E492165 (M R)
 * @since 1/3/2020 (16:51)
 */
public class BlackListTokenBuilderImpl implements BlackListTokenService.BlackListTokenBuilder {

    private BlackListTokenImpl blackListToken;

    public BlackListTokenBuilderImpl(BlackListTokenImpl blackListTokenToken) {
        this.blackListToken = blackListTokenToken;
    }

    @Override
    public BlackListTokenService.BlackListTokenBuilder setUerId(long userId) {
        blackListToken.setUserId(userId);
        return this;
    }

    @Override
    public BlackListTokenService.BlackListTokenBuilder setToken(String token) {
        blackListToken.setToken(token);
        return this;
    }

    @Override
    public BlackListTokenService.BlackListTokenBuilder save() {
        blackListToken.save();
        return this;
    }

    @Override
    public BlackListTokenService.BlackListTokenBuilder update() {
        blackListToken.update();
        return this;
    }

    @Override
    public BlackListTokenService.BlackListTokenBuilder delete() {
        blackListToken.delete();
        return this;
    }
}