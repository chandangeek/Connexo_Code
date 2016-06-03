package com.elster.jupiter.kore.api.impl;

import com.elster.jupiter.kore.api.impl.utils.RangeInfo;
import com.elster.jupiter.rest.util.hypermedia.LinkInfo;
import com.elster.jupiter.util.YesNoAnswer;

import javax.ws.rs.core.Link;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;
import java.util.List;

/**
 * Created by bvn on 6/2/16.
 */
public class DefaultDetailInfo extends LinkInfo<Instant> {
    @XmlJavaTypeAdapter(Link.JaxbAdapter.class)
    public List<Link> link;
    public YesNoAnswer grounded;
    public YesNoAnswer collar;
    public RangeInfo effectivity;
    public Boolean current;
}
