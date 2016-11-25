package com.energyict.protocols.mdc.protocoltasks;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;

import com.energyict.protocolimplv2.DeviceProtocolDialectName;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.util.Optional;


/**
 * Models a {@link DeviceProtocolDialect} for a TCP connection type to a Beacon 3100 device.
 * Note that, using this dialect, the protocol will read out the mirrored (cached) meter data from the Beacon DC.
 * No communication is done to the actual meter.
 *
 * @author: khe
 */
public class MirrorTcpDeviceProtocolDialect extends AbstractDeviceProtocolDialect {

    public MirrorTcpDeviceProtocolDialect(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectName.BEACON_MIRROR_TCP_DLMS_PROTOCOL.getName();
    }

    @Override
    public String getDisplayName() {
        return this.getThesaurus().getFormat(DeviceProtocolDialectName.BEACON_MIRROR_TCP_DLMS_PROTOCOL).format();
    }

    @Override
    public Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> getCustomPropertySet() {
        return Optional.of(new MirrorTcpDeviceProtocolDialectCustomPropertySet(this.getThesaurus(), this.getPropertySpecService()));
    }
}
