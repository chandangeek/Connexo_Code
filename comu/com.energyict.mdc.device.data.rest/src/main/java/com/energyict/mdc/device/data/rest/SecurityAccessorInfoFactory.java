/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest;

import com.elster.jupiter.pki.CryptographicType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.KeyAccessor;
import com.energyict.mdc.device.data.rest.impl.SecurityAccessorInfo;

import javax.ws.rs.core.UriInfo;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class SecurityAccessorInfoFactory {

    public List<SecurityAccessorInfo> asInfo(Device device, UriInfo uriInfo) {
        return device.getKeyAccessors().stream()
            .map(keyAccessor -> asInfo(device, uriInfo, keyAccessor))
            .sorted((p1, p2) -> p1.name.compareToIgnoreCase(p2.name))
            .collect(toList());
    }

    public List<SecurityAccessorInfo> asInfo(Device device, UriInfo uriInfo, CryptographicType.MetaType metaType) {
        return device.getKeyAccessors().stream()
            .filter(keyAccessor -> keyAccessor.getKeyAccessorType().getKeyType().getCryptographicType().getMetaType().equals(metaType))
            .map(keyAccessor -> asInfo(device, uriInfo, keyAccessor))
            .sorted((p1, p2) -> p1.name.compareToIgnoreCase(p2.name))
            .collect(toList());
    }

    public SecurityAccessorInfo asInfo(Device device, UriInfo uriInfo, KeyAccessor keyAccessor) {
        SecurityAccessorInfo info = new SecurityAccessorInfo();
        info.id = keyAccessor.getKeyAccessorType().getId();
        info.name = keyAccessor.getKeyAccessorType().getName();
        return info;
    }
}