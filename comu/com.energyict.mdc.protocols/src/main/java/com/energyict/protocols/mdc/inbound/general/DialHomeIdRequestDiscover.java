/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.mdc.inbound.general;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.DeviceProtocolProperty;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.protocols.mdc.services.impl.TranslationKeys;

import javax.inject.Inject;
import java.time.Clock;
import java.util.List;

public class DialHomeIdRequestDiscover extends RequestDiscover {

    @Inject
    public DialHomeIdRequestDiscover(Clock clock, PropertySpecService propertySpecService, IssueService issueService, MdcReadingTypeUtilService readingTypeUtilService, Thesaurus thesaurus, IdentificationService identificationService, MeteringService meteringService, CollectedDataFactory collectedDataFactory) {
        super(clock, propertySpecService, issueService, readingTypeUtilService, thesaurus, identificationService, collectedDataFactory, meteringService);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        final List<PropertySpec> requiredProperties = super.getPropertySpecs();
        requiredProperties.add(
                this.getPropertySpecService()
                        .stringSpec()
                        .named(TranslationKeys.DEVICEDIALHOMEID)
                        .fromThesaurus(this.getThesaurus())
                        .markRequired()
                        .finish());
        return requiredProperties;
    }

    @Override
    protected void setSerialNumber(String callHomeId) {
        // The 'SerialId' field contains the unique devices Call Home Id.
        setDeviceIdentifier(getIdentificationService().createDeviceIdentifierByProperty(DeviceProtocolProperty.CALL_HOME_ID.javaFieldName(), callHomeId));
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-05-14 15:29:42 +0200 $";
    }

}