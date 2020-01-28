/*
 *
 *  * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 *
 *
 */

package com.elster.jupiter.users.blacklist.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.users.MessageSeeds;
import com.elster.jupiter.users.blacklist.BlackListToken;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;

/**
 * Insert your comments here.
 *
 * @author E492165 (M R)
 * @since 12/26/2019 (12:40)
 */
public final class BlackListTokenImpl implements BlackListToken {

    private long id;
    private long userId;

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String token;
    private Instant createTime;
    private final DataModel dataModel;

    @Inject
    public BlackListTokenImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public BlackListTokenImpl init(long userId, String token) {
        this.userId = userId;
        this.token = token;
        return this;
    }

    public long getId() {
        return this.id;
    }

    @Override
    public void setUserId(long userId) {
        this.userId = userId;
    }

    @Override
    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public long getUserId() {
        return userId;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public Instant getCreateTime() {
        return createTime;
    }

    @Override
    public void save() {
        Save.CREATE.save(dataModel, this);
        dataModel.update(this);
    }

    @Override
    public void update() {
        dataModel.mapper(BlackListTokenImpl.class).update(this);
    }

    @Override
    public void delete() {
        dataModel.mapper(BlackListTokenImpl.class).remove(this);
    }
}