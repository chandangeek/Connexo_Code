package com.energyict.mdc.protocol.pluggable.mocks;

import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.upl.messages.DeviceMessageCategory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpec;

import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 19/12/2016 - 17:29
 */
public class TestDeviceMessageSpecImpl implements DeviceMessageSpec {

    private final long id;

    public TestDeviceMessageSpecImpl(DeviceMessageId id) {
        this.id = id.dbValue();
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return null;
    }

    @Override
    public String getName() {
        return String.valueOf(id);
    }

    @Override
    public TranslationKey getNameTranslationKey() {
        return null;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return null;
    }

    @Override
    public long getId() {
        return id;
    }
}