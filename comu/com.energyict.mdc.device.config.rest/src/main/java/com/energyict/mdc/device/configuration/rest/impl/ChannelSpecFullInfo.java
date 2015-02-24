package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.masterdata.rest.RegisterTypeInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChannelSpecFullInfo extends ChannelSpecInfo {
    @JsonProperty("measurementType")
    public RegisterTypeInfo registerTypeInfo;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode overruledObisCode;
    public BigDecimal overflowValue;
    public String unitOfMeasure;
    public int nbrOfFractionDigits;
    public Boolean isLinkedByActiveDeviceConfiguration;

    public static List<ChannelSpecInfo> from(List<ChannelSpec> channelSpecList){
        List<ChannelSpecInfo> infos = new ArrayList<>(channelSpecList.size());
        for (ChannelSpec channelSpec : channelSpecList) {
            infos.add(ChannelSpecFullInfo.from(channelSpec));
        }
        return infos;
    }

    public static ChannelSpecFullInfo from(ChannelSpec channelSpec){
        ChannelSpecFullInfo info = new ChannelSpecFullInfo();
        info.id = channelSpec.getId();
        info.overruledObisCode = channelSpec.getDeviceObisCode();
        info.overflowValue = channelSpec.getOverflow();
        info.nbrOfFractionDigits = channelSpec.getNbrOfFractionDigits();
        // TODO check that it is truth (true for isLinkedByDeviceType)
        info.registerTypeInfo = new RegisterTypeInfo(channelSpec.getChannelType(), true, false);
        info.unitOfMeasure = channelSpec.getChannelType().getUnit().toString();
        return info;
    }

    public static ChannelSpecFullInfo from(ChannelSpec channelSpec, boolean isLinkedByActiveDeviceConfiguration){
        ChannelSpecFullInfo info = ChannelSpecFullInfo.from(channelSpec);
        info.isLinkedByActiveDeviceConfiguration = isLinkedByActiveDeviceConfiguration;
        return info;
    }
}
