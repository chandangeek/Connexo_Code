package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.dcmulticast;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 25/03/2016 - 16:30
 */
@XmlRootElement
public class MulticastProtocolConfiguration {

    /* zero for IDIS/DSMR, one for Linky AS330L; Linky not supported yet */
    private int multicastProtocol = 0;
    /* zero for PLC_G3 */
    private int communicationInterface = 0;
    /* List of protocol properties */
    private List<MulticastProperty> multicastProperties = new ArrayList<>();
    /* List of meter to be upgraded with their individual configuration */
    private List<MulticastMeterConfig> multicastMeterConfigs = new ArrayList<>();

    //JSon constructor
    private MulticastProtocolConfiguration() {
    }

    public MulticastProtocolConfiguration(int multicastProtocol, int communicationInterface, List<MulticastProperty> multicastProperties, List<MulticastMeterConfig> multicastMeterConfigs) {
        this.multicastProtocol = multicastProtocol;
        this.communicationInterface = communicationInterface;
        this.multicastProperties = multicastProperties;
        this.multicastMeterConfigs = multicastMeterConfigs;
    }

    public Structure toStructure() {
        Structure result = new Structure();
        result.addDataType(new TypeEnum(getMulticastProtocol()));
        result.addDataType(new TypeEnum(getCommunicationInterface()));

        Array multicastPropertiesArray = new Array();
        for (MulticastProperty multicastProperty : getMulticastProperties()) {
            multicastPropertiesArray.addDataType(multicastProperty.toStructure());
        }
        result.addDataType(multicastPropertiesArray);

        Array metersArray = new Array();
        for (MulticastMeterConfig multicastMeterConfig : getMulticastMeterConfigs()) {
            metersArray.addDataType(multicastMeterConfig.toStructure());
        }
        result.addDataType(metersArray);
        return result;
    }

    @XmlAttribute
    public int getMulticastProtocol() {
        return multicastProtocol;
    }

    @XmlAttribute
    public int getCommunicationInterface() {
        return communicationInterface;
    }

    @XmlAttribute
    public List<MulticastProperty> getMulticastProperties() {
        return multicastProperties;
    }

    @XmlAttribute
    public List<MulticastMeterConfig> getMulticastMeterConfigs() {
        return multicastMeterConfigs;
    }
}