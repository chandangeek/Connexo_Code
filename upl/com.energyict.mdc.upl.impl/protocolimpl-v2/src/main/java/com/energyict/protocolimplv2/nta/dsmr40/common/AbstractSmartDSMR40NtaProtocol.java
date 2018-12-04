package com.energyict.protocolimplv2.nta.dsmr40.common;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.tasks.support.DeviceRegisterSupport;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractSmartNtaProtocol;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;
import com.energyict.protocolimplv2.nta.dsmr40.Dsmr40Properties;
import com.energyict.protocolimplv2.nta.dsmr40.registers.Dsmr40RegisterFactory;


/**
 * Abstract class to group functionality for all <b>DSMR4.0</b> protocols
 */
public abstract class AbstractSmartDSMR40NtaProtocol extends AbstractSmartNtaProtocol {


    public AbstractSmartDSMR40NtaProtocol(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(propertySpecService, collectedDataFactory, issueFactory);
    }


    /**
     * Getter for the {@link com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties}
     *
     * @return the requested Properties
     */
    @Override
    public DlmsProperties getProperties() {
        if (this.properties == null) {
            this.properties = new Dsmr40Properties();
        }
        return this.properties;
    }

    @Override
    public DeviceRegisterSupport getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new Dsmr40RegisterFactory(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return this.registerFactory;
    }

}
