package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceProtocolProperty;
import com.energyict.mdc.device.data.exceptions.DeviceProtocolPropertyException;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;

/**
 * Represents a <i>typed</i> property of a Device.
 * <p>
 * Copyrights EnergyICT
 * Date: 3/14/14
 * Time: 9:03 AM
 */
public class DeviceProtocolPropertyImpl implements DeviceProtocolProperty, Serializable {

    private final DataModel dataModel;
    private final Thesaurus thesaurus;
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.VALUE_IS_REQUIRED + "}")
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String propertyValue;
    private String propertySpec;
    private Reference<Device> device = ValueReference.absent();
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

    @Inject
    public DeviceProtocolPropertyImpl(DataModel dataModel, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
    }

    DeviceProtocolPropertyImpl initialize(Device device, String propertySpecName, String stringValue) {
        this.device.set(device);
        if (propertySpecName != null) {
            this.propertySpec = propertySpecName;
        } else {
            throw DeviceProtocolPropertyException.propertySpecTypeDoesNotExist(stringValue, thesaurus, MessageSeeds.DEVICE_PROPERTY_HAS_NO_SPEC);
        }
        this.propertyValue = stringValue;
        return this;
    }

    @Override
    public String getName() {
        return propertySpec;
    }

    @Override
    public String getPropertyValue() {
        return propertyValue;
    }

    @Override
    public void setValue(String value) {
        this.propertyValue = value;
    }

    @Override
    public void update() {
        Save.UPDATE.save(dataModel, this);
    }
}
