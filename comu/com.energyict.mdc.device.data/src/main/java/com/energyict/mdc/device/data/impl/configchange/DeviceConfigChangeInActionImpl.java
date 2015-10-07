package com.energyict.mdc.device.data.impl.configchange;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Device;

import javax.inject.Inject;
import java.time.Instant;

/**
 * Copyrights EnergyICT
 * Date: 06.10.15
 * Time: 16:18
 */
public class DeviceConfigChangeInActionImpl implements DeviceConfigChangeInAction {

    public enum Fields {
        DEVICE_REFERENCE("device"),
        DEVICE_CONFIG_REFERENCE("destinationDeviceConfiguration");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private final DataModel dataModel;

    private Reference<Device> device = ValueReference.absent();
    private Reference<DeviceConfiguration> destinationDeviceConfiguration = ValueReference.absent();

    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    @Inject
    public DeviceConfigChangeInActionImpl(DataModel dataModel){
        this.dataModel = dataModel;
    }

    public DeviceConfigChangeInActionImpl init(Device device, DeviceConfiguration destinationDeviceConfiguration){
        this.device.set(device);
        this.destinationDeviceConfiguration.set(destinationDeviceConfiguration);
        return this;
    }

    @Override
    public void remove() {
        this.dataModel.remove(this);
    }
}
