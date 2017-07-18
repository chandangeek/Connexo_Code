/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;

import javax.inject.Inject;
import java.util.Optional;

public class ReadingTypeBuilder implements Builder<ReadingType> {
    private final MeteringService meteringService;

    private String mrid;
    private String aliasName;

    @Inject
    public ReadingTypeBuilder(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    public ReadingTypeBuilder withMrid(String mrid){
        this.mrid = mrid;
        return this;
    }

    public ReadingTypeBuilder withAlias(String alias){
        this.aliasName = alias;
        return this;
    }

    @Override
    public Optional<ReadingType> find(){
        return meteringService.getReadingType(this.mrid);
    }

    @Override
    public ReadingType create(){
        Log.write(this);
        if (this.aliasName == null) {
            throw new UnableToCreate("Alias name can't be null");
        }
        return meteringService.createReadingType(this.mrid, this.aliasName);
    }
}
