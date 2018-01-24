package com.elster.jupiter.cim.webservices.outbound.soap.enddeviceconfig;

import com.elster.jupiter.metering.LifecycleDates;

import ch.iec.tc57._2011.enddeviceconfig.EndDevice;
import ch.iec.tc57._2011.enddeviceconfig.EndDeviceConfig;
import ch.iec.tc57._2011.enddeviceconfig.LifecycleDate;
import ch.iec.tc57._2011.enddeviceconfig.Name;
import ch.iec.tc57._2011.enddeviceconfig.Status;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

public class EndDeviceFactory {

    private enum DefaultState {

        IN_STOCK("dlc.default.inStock", "In stock"),
        COMMISSIONING("dlc.default.commissioning", "Commissioning"),
        ACTIVE("dlc.default.active", "Active"),
        INACTIVE("dlc.default.inactive", "Inactive"),
        DECOMMISSIONED("dlc.default.decommissioned", "Decommissioned"),
        REMOVED("dlc.default.removed", "Removed");

        private final String key;
        private final String defaultFormat;

        DefaultState(String key, String defaultFormat) {
            this.key = key;
            this.defaultFormat = defaultFormat;
        }

        public String getKey() {
            return key;
        }

        public String getDefaultFormat() {
            return defaultFormat;
        }

        public static Optional<DefaultState> fromKey(String key) {
            return Stream
                    .of(DefaultState.values())
                    .filter(s -> s.getKey().equals(key))
                    .findFirst();
        }
    }

    public EndDeviceConfig asEndDevice(com.elster.jupiter.metering.EndDevice endDevice, String state, Instant effectiveDate) {
        EndDeviceConfig endDeviceConfig = new EndDeviceConfig();
        EndDevice cimEndDevice = createEndDevice(endDevice);
        cimEndDevice.setLifecycle(createLifecycleDate(endDevice));
        cimEndDevice.setStatus(createStatus(DefaultState.fromKey(state).map(DefaultState::getDefaultFormat).orElse(state), effectiveDate));
        endDeviceConfig.getEndDevice().add(cimEndDevice);
        return endDeviceConfig;
    }

    private EndDevice createEndDevice(com.elster.jupiter.metering.EndDevice endDevice) {
        EndDevice cimEndDevice = new EndDevice();
        cimEndDevice.getNames().add(createName(endDevice.getName()));
        cimEndDevice.setMRID(endDevice.getMRID());
        cimEndDevice.setSerialNumber(endDevice.getSerialNumber());
        return cimEndDevice;
    }

    private Name createName(String name) {
        Name nameBean = new Name();
        nameBean.setName(name);
        return nameBean;
    }

    private LifecycleDate createLifecycleDate(com.elster.jupiter.metering.EndDevice endDevice) {
        LifecycleDate lifecycleDate = new LifecycleDate();
        LifecycleDates lifecycleDates = endDevice.getLifecycleDates();
        lifecycleDates.getInstalledDate().ifPresent(lifecycleDate::setInstallationDate);
        lifecycleDates.getReceivedDate().ifPresent(lifecycleDate::setReceivedDate);
        return lifecycleDate;
    }

    private Status createStatus(String state, Instant effectiveDate) {
        Status status = new Status();
        status.setValue(state);
        status.setDateTime(effectiveDate);
        status.setReason("changeStatus");
        return status;
    }
}