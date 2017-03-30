/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.ComServerBuilder;
import com.energyict.mdc.engine.config.ComServer;

public enum ComServerTpl implements Template<ComServer, ComServerBuilder> {
    USER_COMSERVER(null, true),
    DEITVS_099("Deitvs099", false),
    ;

    private String name;
    private boolean status;

    ComServerTpl(String name, boolean status) {
        this.name = name;
        this.status = status;
    }

    @Override
    public Class<ComServerBuilder> getBuilderClass() {
        return ComServerBuilder.class;
    }

    @Override
    public ComServerBuilder get(ComServerBuilder builder) {
        return builder.withName(this.name).withActiveStatus(this.status);
    }
}
