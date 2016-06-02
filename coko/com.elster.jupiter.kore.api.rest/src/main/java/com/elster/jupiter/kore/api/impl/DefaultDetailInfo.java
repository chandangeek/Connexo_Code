package com.elster.jupiter.kore.api.impl;

import com.elster.jupiter.rest.util.hypermedia.LinkInfo;
import com.elster.jupiter.util.YesNoAnswer;

import javax.ws.rs.core.Link;
import java.time.Instant;
import java.util.List;

/**
 * Created by bvn on 6/2/16.
 */
public class DefaultDetailInfo extends LinkInfo<Instant> {
    public List<Link> link;
    public YesNoAnswer grounded;
    public YesNoAnswer collar;

}
