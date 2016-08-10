package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.ChannelSpec;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@XmlRootElement
public class ChannelSpecInfo {
    public long id;
    public String name;
    public boolean useMultiplier;

    public static List<ChannelSpecInfo> from(List<ChannelSpec> channelSpecList) {
        if (channelSpecList == null) {
            return Collections.emptyList();
        }
        List<ChannelSpecInfo> infos = new ArrayList<>(channelSpecList.size());
        for (ChannelSpec channelSpec : channelSpecList) {
            infos.add(ChannelSpecInfo.from(channelSpec));
        }
        return infos;
    }

    public static ChannelSpecInfo from(ChannelSpec channelSpec) {
        ChannelSpecInfo info = new ChannelSpecInfo();
        info.id = channelSpec.getId();
        info.name = channelSpec.getReadingType().getFullAliasName();
        info.useMultiplier = channelSpec.isUseMultiplier();
        return info;
    }
}
