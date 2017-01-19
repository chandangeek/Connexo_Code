package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.hypermedia.LinkInfo;
import com.energyict.mdc.ports.ComPortType;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by bvn on 7/14/15.
 */
public class ComPortPoolInfo extends LinkInfo<Long> {
    public String name;
    public Boolean active;
    public String description;
    @XmlJavaTypeAdapter(ComPortTypeAdapter.class)
    public ComPortType type;
}
