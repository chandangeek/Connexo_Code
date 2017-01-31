/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterType;

import javax.inject.Inject;
import java.util.Optional;

public class RegisterTypeBuilder implements Builder<RegisterType>  {
    private final MasterDataService masterDataService;

    private ReadingType readingType;
    private String obisCode;

    @Inject
    public RegisterTypeBuilder(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    public RegisterTypeBuilder withObisCode(String obisCode){
        this.obisCode = obisCode;
        return this;
    }

    public RegisterTypeBuilder withReadingType(ReadingType readingType){
        this.readingType = readingType;
        return this;
    }

    @Override
    public Optional<RegisterType> find(){
        return masterDataService.findRegisterTypeByReadingType(this.readingType);
    }

    @Override
    public RegisterType create(){
        Log.write(this);
        if (this.obisCode == null){
            throw new UnableToCreate("Obis code can't be null");
        }
        String multiplier = this.readingType.getMultiplier().getSymbol();
        String symbol = this.readingType.getUnit().getUnit().getSymbol();
        Unit unit = Unit.get(multiplier + symbol);
        if (unit == null){
            // try unit in lower case
            unit = Unit.get(multiplier + symbol.toLowerCase());
            if (unit == null){
                unit = Unit.get(BaseUnit.UNITLESS);
            }
        }
        RegisterType registerType = masterDataService.newRegisterType(
                this.readingType,
                ObisCode.fromString(obisCode)
        );
        registerType.save();
        return registerType;
    }
}
