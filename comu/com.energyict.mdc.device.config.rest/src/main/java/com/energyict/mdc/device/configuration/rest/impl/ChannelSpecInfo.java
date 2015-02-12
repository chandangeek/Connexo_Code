package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@XmlRootElement
public class ChannelSpecInfo {
    public long id;
    public String name;

    public static List<ChannelSpecInfo> from(List<ChannelSpec> channelSpecList, MdcReadingTypeUtilService mdcReadingTypeUtilService) {
        if (channelSpecList == null) {
            return Collections.emptyList();
        }
        List<ChannelSpecInfo> infos = new ArrayList<>(channelSpecList.size());
        for (ChannelSpec channelSpec : channelSpecList) {
            infos.add(ChannelSpecInfo.from(channelSpec, mdcReadingTypeUtilService));
        }
        return infos;
    }

    public static ChannelSpecInfo from(ChannelSpec channelSpec, MdcReadingTypeUtilService mdcReadingTypeUtilService) {
        ChannelSpecInfo info = new ChannelSpecInfo();
        info.id = channelSpec.getId();
        info.name = mdcReadingTypeUtilService.getFullAlias(channelSpec.getReadingType());
        return info;
    }
}
