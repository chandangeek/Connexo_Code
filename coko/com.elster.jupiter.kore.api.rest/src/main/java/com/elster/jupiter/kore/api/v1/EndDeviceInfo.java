package com.elster.jupiter.kore.api.v1;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;

public class EndDeviceInfo extends LinkInfo<Long> {
    public String mRID;
    public String serialNumber;
    public String name;
    public String lifecycleState;
}
