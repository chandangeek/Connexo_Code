package com.elster.jupiter.kore.api.impl;

import com.elster.jupiter.rest.util.hypermedia.LinkInfo;

public class EndDeviceInfo extends LinkInfo<Long> {
    public String mRID;
    public String serialNumber;
    public String name;
    public String lifecycleState;
}
