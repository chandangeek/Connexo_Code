package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.DeviceProtocolProperty;
import com.energyict.mdc.device.data.exception.DeviceProtocolPropertyException;
import com.energyict.mdc.device.data.exception.MessageSeeds;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * Represents a <i>typed</i> property of a Device
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/14/14
 * Time: 9:03 AM
 */
public class DeviceProtocolPropertyImpl implements DeviceProtocolProperty, Serializable {

    private final DataModel dataModel;
    private final DeviceDataService deviceDataService;
    private final Thesaurus thesaurus;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Constants.VALUE_IS_REQUIRED_KEY + "}")
    @Size(min = 1, groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Constants.VALUE_IS_REQUIRED_KEY + "}")
    private String propertyValue;
    private long infoTypeId = 0;
    private Reference<Device> device = ValueReference.absent();

    @Inject
    public DeviceProtocolPropertyImpl(DataModel dataModel, DeviceDataService deviceDataService, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.deviceDataService = deviceDataService;
        this.thesaurus = thesaurus;
    }

    DeviceProtocolPropertyImpl initialize(Device device, InfoType infoType, String stringValue) {
        this.device.set(device);
        if(infoType != null){
            this.infoTypeId = infoType.getId();
        } else {
            throw DeviceProtocolPropertyException.infoTypeDoesNotExist(thesaurus, stringValue);
        }
        this.propertyValue = stringValue;
        return this;
    }

    @Override
    public String getName() {
        InfoType infoType = this.deviceDataService.findInfoTypeById(infoTypeId);
        if (infoType != null) {
            return infoType.getName();
        }
        return "";
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
