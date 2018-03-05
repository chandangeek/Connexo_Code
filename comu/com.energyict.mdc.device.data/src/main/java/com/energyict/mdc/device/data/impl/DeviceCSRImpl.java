/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceCSR;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;

public class DeviceCSRImpl implements DeviceCSR {

    public enum Fields {
        CSR("csr"),
        DEVICE("device"),;

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @SuppressWarnings("unused")
    private long id;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private byte[] csr;
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private Reference<Device> device = ValueReference.absent();

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    private final DataModel dataModel;

    @Inject
    public DeviceCSRImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public DeviceCSR init(Device device, byte[] encodedCSR) {
        this.csr = encodedCSR;
        this.device.set(device);
        return this;
    }

    public static DeviceCSR from(DataModel dataModel, Device device, byte[] encodedCSR) {
        return dataModel.query(DeviceCSR.class)
                .select(where("device").isEqualTo(device))
                .stream()
                .map(DeviceCSRImpl.class::cast)
                .findFirst()
                .orElseGet(() -> dataModel.getInstance(DeviceCSRImpl.class))
                .init(device, encodedCSR);
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public Device getDevice() {
        return this.device.get();
    }

    @Override
    public Optional<PKCS10CertificationRequest> getCSR() {
        try {
            return Optional.of(new PKCS10CertificationRequest(this.csr));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public void save() {
        if (getId() == 0) {
            doPersist();
            return;
        }
        doUpdate();
    }

    private void doPersist() {
        Save.CREATE.validate(dataModel, this);
        dataModel.persist(this);
    }

    private void doUpdate() {
        Save.UPDATE.save(dataModel, this);
    }


}