package com.energyict.mdc.masterdata.rest.impl;

import com.elster.jupiter.metering.ReadingTypeFilter;

import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import com.energyict.obis.ObisCode;

class SearchObisUtil {

    private final ObisCode obisCode;

    SearchObisUtil(String obisCodeString){
        this.obisCode = (obisCodeString == null || obisCodeString.isEmpty()) ? null : ObisCode.fromString(obisCodeString);
    }

    ReadingTypeFilter getFilter(MdcReadingTypeUtilService mdcReadingTypeUtilService) {
        String mRID = mdcReadingTypeUtilService.getReadingTypeFilterFrom(this.obisCode);
        ReadingTypeFilter filter = new ReadingTypeFilter();
        filter.addCondition(ReadingTypeConditionUtil.mridFromObisMatch(mRID));
        return filter;
    }

    boolean hasValidObis() {
        return obisCode != null && !obisCode.isInvalid();
    }
}
