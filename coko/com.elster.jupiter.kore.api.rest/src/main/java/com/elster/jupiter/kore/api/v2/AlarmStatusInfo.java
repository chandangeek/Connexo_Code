package com.elster.jupiter.kore.api.v2;


import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import javax.ws.rs.core.Link;

public class AlarmStatusInfo extends LinkInfo<Long> {
    public String id;
    public String name;
    public Boolean clearedStatus;
}
