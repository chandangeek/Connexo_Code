/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;

import java.util.List;

public class DeviceProtocolPluggableClassInfo extends LinkInfo<Long> {
    public String name;
    public String version;
    public String javaClassName;
    public PropertyInfo client;
    public List<LinkInfo> securitySuites;
    public List<LinkInfo> authenticationAccessLevels;
    public List<LinkInfo> encryptionAccessLevels;
    public List<LinkInfo> requestSecurityAccessLevels;
    public List<LinkInfo> responseSecurityAccessLevels;
    public List<LinkInfo> providedConnectionFunctions;
    public List<LinkInfo> consumableConnectionFunctions;
}
