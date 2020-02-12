/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreplacement;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractSapMessage;

import java.util.ArrayList;
import java.util.List;

public class MeterRegisterChangeMessage extends AbstractSapMessage {
    private static final String DEVICE_ID_XML_NAME = "UtilitiesDevice.ID";
    private static final String REGISTER_XML_NAME = "Register";

    private String id;
    private String uuid;
    private String deviceId;
    private List<RegisterChangeMessage> registers = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public List<RegisterChangeMessage> getRegisters() {
        return registers;
    }

    public void validate(Thesaurus thesaurus) {
        if (id == null && uuid == null) {
            addAtLeastOneMissingField(thesaurus, REQUEST_ID_XML_NAME, UUID_XML_NAME);
        }
        if (deviceId == null) {
            addMissingField(DEVICE_ID_XML_NAME);
        }
        if (registers.isEmpty()) {
            addMissingField(REGISTER_XML_NAME);
        }
    }
}
