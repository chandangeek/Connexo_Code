package com.energyict.protocolimplv2.nta.esmr50.common.registers.enums;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;

import java.util.StringTokenizer;

/**
 * Created by avrancea on 12/8/2016.
 */
public class MBusConfigurationObject {
    private final static int METEROLOGICAL_FW_POSITION= 4;
    private final static int OPERATIONAL_FW_POSITION= 6;
    private final static int ADDITIONAL_FW_POSITION= 7;
    private boolean decoded;
    private String content;
    private String meterologicalFirmware;
    private String operationalFirmware;
    private String additionalFirmware;

    private String errorMessage;

    public MBusConfigurationObject(AbstractDataType abstractDataType) {
        try {
            if (abstractDataType.isOctetString()){
                OctetString octetString = abstractDataType.getOctetString();
                content = octetString.stringValue();
                StringBuilder decodedContent = new StringBuilder();
                if(content != null){
                    decoded = true;
                    StringTokenizer stringTokenizer = new StringTokenizer(content, "\r\n");
                    int i = 1;
                    while (stringTokenizer.hasMoreTokens()){
                        String reverseString = reverseString(stringTokenizer.nextToken());
                        switch (i){
                            case METEROLOGICAL_FW_POSITION:
                                setMeterologicalFirmware(extractValueAfterEqual(reverseString));
                                break;
                            case OPERATIONAL_FW_POSITION:
                                setOperationalFirmware(extractValueAfterEqual(reverseString));
                                break;
                            case ADDITIONAL_FW_POSITION:
                                setAdditionalFirmware(extractValueAfterEqual(reverseString));
                                break;
                        }
                        decodedContent.append(reverseString);
                        decodedContent.append("\r\n");
                        i++;
                    }
                    content = decodedContent.toString();
                }
            }
        } catch (Exception ex){
            decoded = false;
            errorMessage = ex.getMessage();
        }
    }

    private String extractValueAfterEqual(String reverseString) {
        StringTokenizer value = new StringTokenizer(reverseString, "=");
        if(value.nextToken() != null) {
            return value.nextToken();
        }
        return null;
    }

    private String reverseString(String string) {
        StringBuilder reversedString = new StringBuilder();
        for(int i = string.length() - 1; i>=0; i--){
            reversedString.append(string.charAt(i));
        }
        return reversedString.toString();
    }

    public boolean isDecoded() {
        return decoded;
    }

    public String getContent() {
        return content;
    }

    public String getErrorMessage() {
        return errorMessage;
    }


    public String getMeterologicalFirmware() {
        return meterologicalFirmware;
    }

    public void setMeterologicalFirmware(String meteriologicalFirmware) {
        this.meterologicalFirmware = meteriologicalFirmware;
    }

    public String getOperationalFirmware() {
        return operationalFirmware;
    }

    public void setOperationalFirmware(String operationalFirmware) {
        this.operationalFirmware = operationalFirmware;
    }

    public String getAdditionalFirmware() {
        return additionalFirmware;
    }

    public void setAdditionalFirmware(String additionalFirmware) {
        this.additionalFirmware = additionalFirmware;
    }
}
