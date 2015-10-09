package com.energyict.mdc.device.data.impl.configchange;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.DeviceConfiguration;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 08.10.15
 * Time: 11:43
 */
public class DeviceConfigChangeRequestImpl implements DeviceConfigChangeRequest {

    public enum Fields {
        DEVICE_CONFIG_REFERENCE("destinationDeviceConfiguration"),
        DEVICE_CONFIG_CHANGE_IN_ACTION("deviceConfigChangeInActions")
        ;
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private final DataModel dataModel;
    private Reference<DeviceConfiguration> destinationDeviceConfiguration = ValueReference.absent();
    private List<DeviceConfigChangeInAction> deviceConfigChangeInActions = new ArrayList<>();

    private long id;

    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    @Inject
    public DeviceConfigChangeRequestImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public DeviceConfigChangeRequestImpl init(DeviceConfiguration destinationDeviceConfiguration) {
        this.destinationDeviceConfiguration.set(destinationDeviceConfiguration);
        return this;
    }


    @Override
    public long getId() {
        return id;
    }

    public void save() {
        if (getId() > 0) {
            Save.UPDATE.save(dataModel, this);
        } else {
            Save.CREATE.save(dataModel, this);
        }
    }


    @Override
    public void remove() {
        this.dataModel.remove(this);
    }
}
