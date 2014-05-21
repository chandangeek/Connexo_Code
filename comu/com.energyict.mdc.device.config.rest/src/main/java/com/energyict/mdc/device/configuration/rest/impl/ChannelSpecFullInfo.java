package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.masterdata.rest.PhenomenonInfo;
import com.energyict.mdc.masterdata.rest.RegisterMappingInfo;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChannelSpecFullInfo extends ChannelSpecInfo {
    @JsonProperty("measurementType")
    public RegisterMappingInfo registerMapping;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode overruledObisCode;
    public BigDecimal multiplier;
    public BigDecimal overflowValue;
    public PhenomenonInfo unitOfMeasure;
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
        info.name = channelSpec.getName();
        info.overruledObisCode = channelSpec.getDeviceObisCode();
        info.multiplier = channelSpec.getMultiplier();
        info.overflowValue = channelSpec.getOverflow();
        // TODO check that it is truth (true for isLinkedByDeviceType)
        info.registerMapping = new RegisterMappingInfo(channelSpec.getRegisterMapping(), true);
        info.unitOfMeasure = PhenomenonInfo.from(channelSpec.getPhenomenon());
        return info;
    }

    public static ChannelSpecFullInfo from(ChannelSpec channelSpec, boolean isLinkedByActiveDeviceConfiguration){
        ChannelSpecFullInfo info = ChannelSpecFullInfo.from(channelSpec);
        info.isLinkedByActiveDeviceConfiguration = isLinkedByActiveDeviceConfiguration;
        return info;
    }
}
