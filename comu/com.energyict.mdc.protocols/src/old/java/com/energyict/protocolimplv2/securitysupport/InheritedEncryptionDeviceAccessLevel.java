package com.energyict.protocolimplv2.securitysupport;


import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.security.DeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.protocols.mdc.services.impl.TranslationKeys;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

/**
 * If a protocol has this access level in its list of supported access levels,
 * it means that it's a slave device and it can inherit the security properties of its master device.
 * <p/>
 * Note that there's a copy of this class in comserver-core-impl, so it can be used by the adapters
 * <p/>
 * <p/>
 * Copyrights EnergyICT
 * Date: 16/05/14
 * Time: 15:44
 * Author: khe
 */
public class InheritedEncryptionDeviceAccessLevel implements EncryptionDeviceAccessLevel {

    private final Thesaurus thesaurus;

    @Inject
    public InheritedEncryptionDeviceAccessLevel(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public int getId() {
        return DeviceAccessLevel.CAN_INHERIT_PROPERTIES_FROM_MASTER_ID;
    }

    @Override
    public String getTranslation() {
        return thesaurus.getFormat(TranslationKeys.INHERITED_ACCESSLEVEL).format();
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Collections.emptyList();
    }
}