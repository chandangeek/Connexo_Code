package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;

import java.util.Collections;
import java.util.Map;

/**
 * Represents a MessageConverter placeholder for Protocols which message weren't migrated yet ...
 * Eventually this one will be deleted.
 *
 * Copyrights EnergyICT
 * Date: 28/05/13
 * Time: 14:17
 */
public class PlaceHolderMessageConverter extends AbstractMessageConverter {

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return Collections.emptyMap();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }
}
