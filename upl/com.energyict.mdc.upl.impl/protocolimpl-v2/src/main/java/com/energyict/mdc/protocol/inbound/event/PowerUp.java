package com.energyict.mdc.protocol.inbound.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;

@XmlRootElement
public class PowerUp {

    private static ObjectMapper mapper = new ObjectMapper();

    @JsonProperty("address")
    public String address;

    @JsonProperty("port")
    public Integer port;

    @JsonProperty("transport")
    public Integer transport;

    public static PowerUp fromJson(String json) throws IOException {
        return mapper.readValue(json, PowerUp.class);
    }

}
