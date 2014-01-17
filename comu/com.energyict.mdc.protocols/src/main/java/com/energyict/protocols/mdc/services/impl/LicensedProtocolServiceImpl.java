package com.energyict.protocols.mdc.services.impl;

import com.energyict.license.LicensedProtocolRule;
import com.energyict.mdc.common.license.License;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.api.LicensedProtocol;
import org.osgi.service.component.annotations.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 19/11/13
 * Time: 09:52
 */
@Component(name = "com.energyict.mdc.service.licensedprotocols", service = LicensedProtocolService.class, immediate = true)
public class LicensedProtocolServiceImpl implements LicensedProtocolService {

    @Override
    public List<LicensedProtocol> getAllLicensedProtocols(License license) {
        List<LicensedProtocol> allLicensedProtocols = new ArrayList<>();
        for (LicensedProtocolRule licensedProtocolRule : LicensedProtocolRule.values()) {
            if (license.hasProtocol(licensedProtocolRule.getName())){
                allLicensedProtocols.add(licensedProtocolRule);
            }
        }
        return allLicensedProtocols;
    }

    @Override
    public LicensedProtocol findLicensedProtocolFor(DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        for (LicensedProtocolRule licensedProtocolRule : LicensedProtocolRule.values()) {
            if(licensedProtocolRule.getClassName().equals(deviceProtocolPluggableClass.getJavaClassName())){
                return licensedProtocolRule;
            }
        }
        return null;
    }
}
