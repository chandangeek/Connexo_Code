package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;

import java.util.List;

public class DeviceProtocolPluggableClassInfo extends LinkInfo<Long> {
    public String name;
    public String version;
    public String javaClassName;
    public List<LinkInfo> authenticationAccessLevels;
    public List<LinkInfo> encryptionAccessLevels;
}
