package com.energyict.protocols.mdc.inbound.general;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.protocolimplv2.common.BasicDynamicPropertySupport;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import javax.inject.Inject;
import java.util.List;

/**
 * In the case of DialHomeIdRequestDiscover, a meter starts an inbound session and pushes its unique Call Home ID and additional meter data.
 * There are no extra requests sent by the comserver.
 * <p/>
 * Copyrights EnergyICT
 * Date: 21/06/12
 * Time: 14:50
 */
public class DialHomeIdRequestDiscover extends RequestDiscover {

    @Inject
    public DialHomeIdRequestDiscover(PropertySpecService propertySpecService, IssueService issueService, MdcReadingTypeUtilService readingTypeUtilService, Thesaurus thesaurus, IdentificationService identificationService, CollectedDataFactory collectedDataFactory) {
        super(propertySpecService, issueService, readingTypeUtilService, thesaurus, identificationService, collectedDataFactory);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        final List<PropertySpec> requiredProperties = super.getPropertySpecs();
        requiredProperties.add(this.getPropertySpecService().stringPropertySpec(this.getThesaurus().getString(MessageSeeds.DEVICEDIALHOMEID.getKey(), "Device call home ID"), true, null));
        return requiredProperties;
    }

    @Override
    protected void setSerialNumber(String callHomeId) {
        // The 'SerialId' field contains the unique devices Call Home Id.
        setDeviceIdentifier(getIdentificationService().createDeviceIdentifierByProperty(BasicDynamicPropertySupport.CALL_HOME_ID_PROPERTY_NAME, callHomeId));
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-05-14 15:29:42 +0200 $";
    }

}