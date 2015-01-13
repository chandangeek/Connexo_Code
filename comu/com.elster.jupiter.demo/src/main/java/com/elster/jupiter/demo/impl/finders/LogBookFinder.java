package com.elster.jupiter.demo.impl.finders;

import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MasterDataService;

import javax.inject.Inject;

public class LogBookFinder extends NamedFinder<LogBookFinder, LogBookType>{
    private final MasterDataService masterDataService;

    @Inject
    public LogBookFinder(MasterDataService masterDataService) {
        super(LogBookFinder.class);
        this.masterDataService = masterDataService;
    }

    @Override
    public LogBookType find() {
        return masterDataService.findLogBookTypeByName(getName()).orElseThrow(getFindException());
    }
}
