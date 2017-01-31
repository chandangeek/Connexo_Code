/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.rest;

import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoadProfileTypeInfo {

    public long id;
    public String name;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    @XmlJavaTypeAdapter(LocalizedTimeDuration.Adapter.class)
    public TimeDuration timeDuration;
    public List<RegisterTypeInfo> registerTypes;
    public Boolean isLinkedToActiveDeviceConf;
    public long version;
    public VersionInfo<Long> parent;
}