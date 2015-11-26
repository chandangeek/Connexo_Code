package com.energyict.protocolimplv2.abnt.common.dialects;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;

import com.energyict.protocolimplv2.DeviceProtocolDialectName;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.util.Optional;

/**
 * Models a DeviceProtocolDialect for a Serial connection type
 * (SioSerialConnectionType, RxTxSerialConnectionType)
 *
 * @author sva
 * @since 23/05/2014 - 14:05
 */
public class AbntSerialDeviceProtocolDialect extends AbstractDeviceProtocolDialect {

    public AbntSerialDeviceProtocolDialect(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    @Override
    public Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> getCustomPropertySet() {
        return Optional.of(new AbntDeviceProtocolDialectCustomPropertySet(this.getThesaurus(), this.getPropertySpecService()));
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectName.ABNT_SERIAL.getName();
    }

    @Override
    public String getDisplayName() {
        return this.getThesaurus().getFormat(DeviceProtocolDialectName.ABNT_SERIAL).format();
    }

}