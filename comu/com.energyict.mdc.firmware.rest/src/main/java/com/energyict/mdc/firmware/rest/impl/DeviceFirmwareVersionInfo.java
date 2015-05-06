package com.energyict.mdc.firmware.rest.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@JsonSerialize(using = DeviceFirmwareVersionInfo.Serializer.class)
public class DeviceFirmwareVersionInfo {
    public FirmwareTypeInfo firmwareType;
    public ActiveVersion activeVersion;
    public Map<String, Map<String, Object>> upgradeVersions = new HashMap<>();

    public static class ActiveVersion {
        public String firmwareVersion;
        public FirmwareStatusInfo firmwareVersionStatus;
        public Long lastCheckedDate;

        public ActiveVersion() {}
    }

    public DeviceFirmwareVersionInfo() {}


    public static class Serializer extends JsonSerializer<DeviceFirmwareVersionInfo> {

        @Override
        public void serialize(DeviceFirmwareVersionInfo value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeStartObject();
            jgen.writeObjectField("firmwareType", value.firmwareType);
            jgen.writeObjectField("activeVersion", value.activeVersion);
            if (value.upgradeVersions != null) {
                for (Map.Entry<String, Map<String, Object>> upgradeVersion : value.upgradeVersions.entrySet()) {
                    if (upgradeVersion.getKey() != null) {
                        jgen.writeObjectField(upgradeVersion.getKey(), upgradeVersion.getValue());
                    }
                }
            }
            jgen.writeEndObject();
        }
    }
}
