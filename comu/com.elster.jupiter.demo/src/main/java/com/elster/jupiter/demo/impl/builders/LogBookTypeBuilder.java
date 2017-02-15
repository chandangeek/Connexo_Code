/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MasterDataService;

import javax.inject.Inject;
import java.util.Optional;

public class LogBookTypeBuilder extends NamedBuilder<LogBookType, LogBookTypeBuilder> {
    private final MasterDataService masterDataService;

    private String obisCode;

    @Inject
    public LogBookTypeBuilder(MasterDataService masterDataService) {
        super(LogBookTypeBuilder.class);
        this.masterDataService = masterDataService;
    }

    public LogBookTypeBuilder withObisCode(String obisCode){
        this.obisCode = obisCode;
        return this;
    }

    @Override
    public Optional<LogBookType> find(){
        return masterDataService.findLogBookTypeByName(getName());
    }

    @Override
    public LogBookType create(){
        Log.write(this);
        LogBookType logBookType = masterDataService.newLogBookType(getName(), ObisCode.fromString(this.obisCode));
        logBookType.save();
        return logBookType;
    }
}
