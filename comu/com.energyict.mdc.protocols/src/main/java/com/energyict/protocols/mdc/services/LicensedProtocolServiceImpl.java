package com.energyict.protocols.mdc.services;

import com.energyict.license.LicensedProtocolRule;
import com.energyict.mdc.services.LicensedProtocolService;
import com.energyict.mdw.core.LicensedProtocol;
import com.energyict.mdw.core.MeteringWarehouse;
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
    public List<LicensedProtocol> getAllLicensedProtocols() {
        List<LicensedProtocol> allLicensedProtocols = new ArrayList<>();
        for (LicensedProtocolRule licensedProtocolRule : LicensedProtocolRule.values()) {
            if(MeteringWarehouse.getCurrent().getLicense().hasProtocol(licensedProtocolRule.getName())){
                allLicensedProtocols.add(licensedProtocolRule);
            }
        }
        return allLicensedProtocols;
    }
}
