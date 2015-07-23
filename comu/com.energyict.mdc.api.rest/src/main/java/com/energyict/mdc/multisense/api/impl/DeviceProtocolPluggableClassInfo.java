package com.energyict.mdc.multisense.api.impl;

import java.util.List;

public class DeviceProtocolPluggableClassInfo extends LinkInfo {
    public String name;
    public String version;
    public String javaClassName;
    public List<LinkInfo> authenticationAccessLevels;
    public List<LinkInfo> encryptionAccessLevels;
}
