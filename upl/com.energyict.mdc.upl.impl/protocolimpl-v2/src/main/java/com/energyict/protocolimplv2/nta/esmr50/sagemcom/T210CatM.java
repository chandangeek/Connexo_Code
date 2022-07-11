package com.energyict.protocolimplv2.nta.esmr50.sagemcom;

import com.energyict.mdc.upl.DeviceMasterDataExtractor;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.*;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.tasks.support.DeviceRegisterSupport;
import com.energyict.protocolimplv2.nta.dsmr23.profiles.LoadProfileBuilder;
import com.energyict.protocolimplv2.nta.esmr50.sagemcom.loadprofiles.T210CatMLoadProfileBuilder;
import com.energyict.protocolimplv2.nta.esmr50.sagemcom.registers.T210CatMRegisterFactory;

public class T210CatM extends T210 {

    public T210CatM(CollectedDataFactory collectedDataFactory, IssueFactory issueFactory,
                    PropertySpecService propertySpecService, NlsService nlsService, Converter converter,
                    DeviceMessageFileExtractor messageFileExtractor, TariffCalendarExtractor calendarExtractor,
                    NumberLookupExtractor numberLookupExtractor, LoadProfileExtractor loadProfileExtractor,
                    KeyAccessorTypeExtractor keyAccessorTypeExtractor, DeviceMasterDataExtractor deviceMasterDataExtractor) {
        super(collectedDataFactory, issueFactory, propertySpecService, nlsService, converter, messageFileExtractor,
                calendarExtractor, numberLookupExtractor, loadProfileExtractor, keyAccessorTypeExtractor,
                deviceMasterDataExtractor);
    }

    @Override
    public DeviceRegisterSupport getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new T210CatMRegisterFactory(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return registerFactory;
    }

    @Override
    public String getProtocolDescription() {
        return "Sagemcom T210 CatM protocol V2";
    }

    @Override
    public String getVersion() {
        return "$Date: 2022-07-04$";
    }

    @Override
    protected LoadProfileBuilder getLoadProfileBuilder(){
        if(this.loadProfileBuilder == null){
            loadProfileBuilder = new T210CatMLoadProfileBuilder(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return this.loadProfileBuilder;
    }

}
