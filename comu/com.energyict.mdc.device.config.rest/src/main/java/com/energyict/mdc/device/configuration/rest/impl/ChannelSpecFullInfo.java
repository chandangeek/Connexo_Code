package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.rest.util.VersionInfo;
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
import java.util.stream.Collectors;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChannelSpecFullInfo extends ChannelSpecInfo {
    @JsonProperty("measurementType")
    public RegisterTypeInfo registerTypeInfo;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode overruledObisCode;
    public BigDecimal overflowValue;
    public int nbrOfFractionDigits;
    public Boolean isLinkedByActiveDeviceConfiguration;
    @JsonProperty("useMultiplier")
    public Boolean useMultiplier;
    @JsonProperty("calculatedReadingType")
    public ReadingTypeInfo calculatedReadingType;
    public List<ReadingTypeInfo> possibleCalculatedReadingTypes = new ArrayList<>();
    @JsonProperty("collectedReadingType")
    public ReadingTypeInfo collectedReadingType;
    public long version;
    public VersionInfo<Long> parent;

    public static List<ChannelSpecInfo> from(List<ChannelSpec> channelSpecList) {
        return channelSpecList.stream().map(ChannelSpecFullInfo::from).collect(Collectors.toList());
    }

    public static ChannelSpecFullInfo from(ChannelSpec channelSpec, ReadingType collectedReadingType, List<ReadingType> multipliedCalculatedRegisterTypes, boolean isLinkedByActiveDeviceConfiguration) {
        ChannelSpecFullInfo info = new ChannelSpecFullInfo();
        info.id = channelSpec.getId();
        info.name = channelSpec.getReadingType().getFullAliasName();
        info.overruledObisCode = channelSpec.getDeviceObisCode();
        info.overflowValue = channelSpec.getOverflow();
        info.nbrOfFractionDigits = channelSpec.getNbrOfFractionDigits();
        // TODO check that it is truth (true for isLinkedByDeviceType)
        info.registerTypeInfo = new RegisterTypeInfo(channelSpec.getChannelType(), true, false);
        info.useMultiplier = channelSpec.isUseMultiplier();
        if (info.useMultiplier) {
            info.calculatedReadingType = new ReadingTypeInfo(channelSpec.getCalculatedReadingType().get());
        }
        info.collectedReadingType = new ReadingTypeInfo(collectedReadingType);
        multipliedCalculatedRegisterTypes.forEach(readingTypeConsumer -> info.possibleCalculatedReadingTypes.add(new ReadingTypeInfo(readingTypeConsumer)));
        info.parent = new VersionInfo<>(channelSpec.getLoadProfileSpec().getId(), channelSpec.getLoadProfileSpec().getVersion());
        info.version = channelSpec.getVersion();
        info.isLinkedByActiveDeviceConfiguration = isLinkedByActiveDeviceConfiguration;
        return info;
    }
}
