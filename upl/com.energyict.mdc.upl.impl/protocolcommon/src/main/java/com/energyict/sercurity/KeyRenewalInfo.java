package com.energyict.sercurity;

import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.security.KeyAccessorType;

import com.energyict.protocol.exception.DataParseException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;

@XmlRootElement
public class KeyRenewalInfo {

    private static ObjectMapper mapper = new ObjectMapper();

    @JsonProperty("keyValue")
    public String keyValue;

    @JsonProperty("wrapingKeyValue")
    public String wrapingKeyValue;

    @JsonProperty("wrappedKeyValue")
    public String wrappedKeyValue;

    public KeyRenewalInfo() {
        //used for serialization
    }

    public KeyRenewalInfo(KeyAccessorTypeExtractor keyAccessorTypeExtractor, KeyAccessorType keyAccessorType) {
        keyValue = keyAccessorTypeExtractor.passiveValueContent(keyAccessorType);
        wrapingKeyValue = keyAccessorTypeExtractor.getWrapperKeyActualValue(keyAccessorType);
        wrappedKeyValue = ProtocolTools.getHexStringFromBytes(ProtocolTools.aesWrap(
                ProtocolTools.getBytesFromHexString(keyValue, ""), ProtocolTools.getBytesFromHexString(wrapingKeyValue, "")), "");
    }

    public static KeyRenewalInfo fromJson(String json) {
        try {
            return mapper.readValue(json, KeyRenewalInfo.class);
        } catch (IOException e) {
            throw DataParseException.generalParseException(e);
        }
    }

    public String toJson() {
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw DataParseException.generalParseException(e);
        }
    }

}
