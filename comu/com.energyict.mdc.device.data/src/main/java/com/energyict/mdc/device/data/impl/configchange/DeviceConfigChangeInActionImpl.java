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
        DEVICE_CONFIG_REQUEST_REFERENCE("deviceConfigChangeRequest");
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
    private Reference<DeviceConfigChangeRequest> deviceConfigChangeRequest = ValueReference.absent();
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
    public DeviceConfigChangeInActionImpl(DataModel dataModel){
        this.dataModel = dataModel;
    }

    public DeviceConfigChangeInActionImpl init(Device device, DeviceConfigChangeRequest deviceConfigChangeRequest){
        this.device.set(device);
        this.deviceConfigChangeRequest.set(deviceConfigChangeRequest);
        return this;
    }

    @Override
    public void remove() {
        ((DeviceConfigChangeRequestImpl) deviceConfigChangeRequest.get()).removeDeviceConfigChangeInAction(this);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceConfigChangeInActionImpl that = (DeviceConfigChangeInActionImpl) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
