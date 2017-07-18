/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.ImplField;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.G3DeviceAddressInformation;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Clock;
import java.time.Instant;

/**
 * Provides an implementation for the {@link G3DeviceAddressInformation} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-17 (09:40)
 */
@ValidIPv6Address(groups = { Save.Create.class, Save.Update.class })
public class G3DeviceAddressInformationImpl implements G3DeviceAddressInformation, PersistenceAware {

    public enum Field implements ImplField {
        DEVICE("device"),
        IPV6_ADDRESS("ipv6StringAddress"),
        IPV6_SHORT_ADDRESS("ipv6ShortAddress"),
        LOGICAL_DEVICE_ID("logicalDeviceId"),
        CREATION_TIME("interval.start");

        private final String javaFieldName;

        private Field(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        @Override
        public String fieldName() {
            return javaFieldName;
        }

    }
    private final Clock clock;
    private final DataModel dataModel;

    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

    @IsPresent(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.VALUE_IS_REQUIRED_KEY + "}")
    private Reference<Device> device = ValueReference.absent();
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.VALUE_IS_REQUIRED_KEY + "}")
    private Interval interval;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.VALUE_IS_REQUIRED_KEY + "}")
    @Size(min = 1, max = Table.SHORT_DESCRIPTION_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String ipv6StringAddress;
    private InetAddress ipv6Address;
    private int ipv6ShortAddress;
    private int logicalDeviceId;

    @Inject
    public G3DeviceAddressInformationImpl(Clock clock, DataModel dataModel) {
        super();
        this.clock = clock;
        this.dataModel = dataModel;
    }

    G3DeviceAddressInformationImpl createFrom(Device device, String ipv6StringAddress, int ipv6ShortAddress, int logicalDeviceId) {
        this.interval = Interval.startAt(this.clock.instant());
        this.device.set(device);
        this.ipv6StringAddress = ipv6StringAddress;
        this.ipv6ShortAddress = ipv6ShortAddress;
        this.logicalDeviceId = logicalDeviceId;
        return this;
    }

    @Override
    public void postLoad() {
        try {
            this.ipv6Address = Inet6Address.getByName(this.ipv6StringAddress);
        }
        catch (UnknownHostException e) {
            throw new IllegalStateException("Validation of ipv6Address failed before or data was changed manually in the database", e);
        }
    }

    @Override
    public Device getDevice() {
        return device.get();
    }

    String getIpv6StringAddress() {
        return ipv6StringAddress;
    }

    public InetAddress getIPv6Address() {
        return ipv6Address;
    }

    void setIpv6Address(InetAddress ipv6Address) {
        this.ipv6Address = ipv6Address;
    }

    public int getIpv6ShortAddress() {
        return ipv6ShortAddress;
    }

    @Override
    public int getLogicalDeviceId() {
        return logicalDeviceId;
    }

    boolean differentFrom(String ipv6Address, int ipv6ShortAddress, int logicalDeviceId) {
        return !this.ipv6StringAddress.equals(ipv6Address)
            || this.ipv6ShortAddress != ipv6ShortAddress
            || this.logicalDeviceId != logicalDeviceId;
    }

    @Override
    public Interval getInterval() {
        return this.interval;
    }

    Instant getEffectiveStart() {
        return this.interval.getStart();
    }

    void save () {
        Save.action(this.version).save(this.dataModel, this);
    }

    void terminate(Instant when) {
        if (!isEffectiveAt(when)) {
            throw new IllegalArgumentException();
        }
        this.interval = this.interval.withEnd(when);
    }

}