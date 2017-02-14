/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterType;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

public class LoadProfileTypeBuilder extends NamedBuilder<LoadProfileType, LoadProfileTypeBuilder> {
    private final MasterDataService masterDataService;

    private String obisCode;
    private TimeDuration timeDuration;
    private List<RegisterType> registerTypes;

    @Inject
    public LoadProfileTypeBuilder(MasterDataService masterDataService) {
        super(LoadProfileTypeBuilder.class);
        this.masterDataService = masterDataService;
    }

    public LoadProfileTypeBuilder withObisCode(String obisCode){
        this.obisCode = obisCode;
        return this;
    }

    public LoadProfileTypeBuilder withTimeDuration(TimeDuration timeDuration){
        this.timeDuration = timeDuration;
        return this;
    }

    public LoadProfileTypeBuilder withRegisters(List<RegisterType> registerTypes){
        this.registerTypes = registerTypes;
        return this;
    }

    @Override
    public Optional<LoadProfileType> find(){
        return masterDataService.findLoadProfileTypesByName(getName()).stream().findFirst();
    }

    @Override
    public LoadProfileType create(){
        Log.write(this);
        LoadProfileType loadProfileType = masterDataService.newLoadProfileType(getName(), ObisCode.fromString(obisCode), timeDuration, registerTypes);
        loadProfileType.save();
        return loadProfileType;
    }
}
